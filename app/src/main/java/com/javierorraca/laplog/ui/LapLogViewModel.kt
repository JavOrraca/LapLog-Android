package com.javierorraca.laplog.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.javierorraca.laplog.data.AppTheme
import com.javierorraca.laplog.data.Lap
import com.javierorraca.laplog.data.LapLogRepository
import com.javierorraca.laplog.data.LapLogSettings
import com.javierorraca.laplog.data.LapLogSnapshot
import com.javierorraca.laplog.data.Session
import com.javierorraca.laplog.data.trimmed
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ActiveSheet { None, History, Menu, Settings, QuickPick }

data class LapLogUiState(
    val elapsedMs: Long = 0,
    val running: Boolean = false,
    val laps: List<Lap> = emptyList(),
    val currentLapName: String = "",
    val sessionTitle: String = "Grill",
    val pendingStartMs: Long = 0,
    val history: List<Session> = LapLogSnapshot.seedHistory(),
    val settings: LapLogSettings = LapLogSettings(),
    val activeSheet: ActiveSheet = ActiveSheet.None,
    val selectedSessionId: String? = null,
    val quickPickLapIndex: Int? = null,
    val loaded: Boolean = false,
) {
    val palette: Palette get() = paletteFor(settings.theme)
    val accentHex: Long get() = settings.accentHex
    val accentSwatches: List<Long>
        get() = listOf(0xc2410c, 0x0a7c41, 0x1e5fbf, 0x8b4fbf, 0xb3142f, themeMonochromeAccent)
    val themeMonochromeAccent: Long get() = if (settings.theme == AppTheme.Dark) 0xece7d8 else 0x1a1916
    val selectedSession: Session? get() = history.firstOrNull { it.id == selectedSessionId }
}

class LapLogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LapLogRepository(application)
    private val mutableState = MutableStateFlow(LapLogUiState())
    val uiState: StateFlow<LapLogUiState> = mutableState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = mutableState.value,
    )

    private var ticker: Job? = null
    private var startedAtMs: Long = 0
    private var baseMs: Long = 0

    init {
        viewModelScope.launch {
            val snapshot = repository.snapshots.first() ?: LapLogSnapshot()
            baseMs = snapshot.elapsedMs
            mutableState.value = snapshot.toUiState().copy(loaded = true)
            persistCurrent()
        }
    }

    fun showSheet(sheet: ActiveSheet) {
        mutableState.value = mutableState.value.copy(activeSheet = sheet)
    }

    fun closeSheet() {
        mutableState.value = mutableState.value.copy(
            activeSheet = ActiveSheet.None,
            quickPickLapIndex = null,
        )
    }

    fun setSessionTitle(title: String) {
        val final = title.trimmed()
        if (final.isNotEmpty()) updateAndPersist { it.copy(sessionTitle = final) }
    }

    fun setCurrentLapName(name: String) {
        updateAndPersist { it.copy(currentLapName = name) }
    }

    fun start() {
        val state = mutableState.value
        if (state.running) return
        baseMs = state.elapsedMs
        startedAtMs = SystemClock.elapsedRealtime()
        mutableState.value = state.copy(running = true)
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (true) {
                delay(16)
                val now = baseMs + (SystemClock.elapsedRealtime() - startedAtMs)
                mutableState.value = mutableState.value.copy(
                    elapsedMs = now,
                    laps = finalizeConcurrentDurations(mutableState.value.laps, now, mutableState.value.settings.concurrentLaps),
                )
            }
        }
    }

    fun stop() {
        val state = mutableState.value
        if (!state.running) return
        ticker?.cancel()
        ticker = null
        val elapsed = baseMs + (SystemClock.elapsedRealtime() - startedAtMs)
        baseMs = elapsed
        updateAndPersist {
            it.copy(
                elapsedMs = elapsed,
                running = false,
                laps = finalizeConcurrentDurations(it.laps, elapsed, it.settings.concurrentLaps),
            )
        }
    }

    fun toggleRunning() {
        if (mutableState.value.running) stop() else start()
    }

    fun leftControl() {
        val state = mutableState.value
        when {
            state.running -> addLap()
            state.elapsedMs > 0 -> resetAndArchive()
        }
    }

    fun addLap() {
        val state = mutableState.value
        if (!state.running) return
        updateAndPersist {
            val idx = it.laps.size + 1
            val now = it.elapsedMs
            val name = it.currentLapName.trimmed().ifEmpty { "Lap $idx" }
            if (it.settings.concurrentLaps) {
                it.copy(
                    laps = it.laps + Lap(
                        index = idx,
                        name = name,
                        startMs = it.pendingStartMs,
                        totalMs = it.pendingStartMs,
                        durationMs = (now - it.pendingStartMs).coerceAtLeast(0),
                    ),
                    pendingStartMs = now,
                    currentLapName = "",
                )
            } else {
                val previousTotal = it.laps.lastOrNull()?.totalMs ?: 0
                it.copy(
                    laps = it.laps + Lap(
                        index = idx,
                        name = name,
                        startMs = previousTotal,
                        totalMs = now,
                        durationMs = (now - previousTotal).coerceAtLeast(0),
                    ),
                    currentLapName = "",
                )
            }
        }
    }

    fun renameLap(index: Int, name: String) {
        updateAndPersist {
            if (index !in it.laps.indices) return@updateAndPersist it
            val lap = it.laps[index]
            val final = name.trimmed().ifEmpty { "Lap ${lap.index}" }
            it.copy(laps = it.laps.toMutableList().also { laps -> laps[index] = lap.copy(name = final) })
        }
    }

    fun deleteLap(index: Int) {
        updateAndPersist {
            if (index !in it.laps.indices) return@updateAndPersist it
            val relabeled = it.laps.filterIndexed { i, _ -> i != index }
                .mapIndexed { i, lap -> lap.copy(index = i + 1) }
            it.copy(laps = relabeled, quickPickLapIndex = null, activeSheet = ActiveSheet.None)
        }
    }

    fun openQuickPick(index: Int) {
        val state = mutableState.value
        if (state.settings.showQuick && index in state.laps.indices) {
            mutableState.value = state.copy(activeSheet = ActiveSheet.QuickPick, quickPickLapIndex = index)
        }
    }

    fun applyQuickPick(name: String) {
        val index = mutableState.value.quickPickLapIndex ?: return
        renameLap(index, name)
        closeSheet()
    }

    fun resetAndArchive() {
        ticker?.cancel()
        ticker = null
        updateAndPersist {
            val totalMs = it.elapsedMs
            val snapshot = archiveLaps(
                laps = it.laps,
                totalMs = totalMs,
                currentLapName = it.currentLapName,
                pendingStartMs = it.pendingStartMs,
                concurrentLaps = it.settings.concurrentLaps,
            )
            val title = it.sessionTitle.trimmed().ifEmpty { "Session" }
            val history = if (totalMs > 0 || snapshot.isNotEmpty()) {
                listOf(Session(title = title, totalMs = totalMs, laps = snapshot)) + it.history
            } else {
                it.history
            }
            baseMs = 0
            it.copy(
                elapsedMs = 0,
                running = false,
                laps = emptyList(),
                currentLapName = "",
                pendingStartMs = 0,
                sessionTitle = "New",
                history = history,
            )
        }
    }

    fun selectSession(sessionId: String?) {
        mutableState.value = mutableState.value.copy(selectedSessionId = sessionId)
    }

    fun renameSession(sessionId: String, name: String) {
        val final = name.trimmed()
        if (final.isEmpty()) return
        updateAndPersist {
            it.copy(history = it.history.map { session ->
                if (session.id == sessionId) session.copy(title = final) else session
            })
        }
    }

    fun renameSessionLap(sessionId: String, lapIndex: Int, name: String) {
        val final = name.trimmed()
        if (final.isEmpty()) return
        updateAndPersist {
            it.copy(history = it.history.map { session ->
                if (session.id == sessionId && lapIndex in session.laps.indices) {
                    session.copy(laps = session.laps.toMutableList().also { laps ->
                        laps[lapIndex] = laps[lapIndex].copy(name = final)
                    })
                } else {
                    session
                }
            })
        }
    }

    fun setTheme(theme: AppTheme) {
        updateAndPersist {
            val wasDark = it.settings.theme == AppTheme.Dark
            val toDark = theme == AppTheme.Dark
            val nextAccent = when {
                toDark != wasDark && toDark && it.settings.accentHex == 0x1a1916L -> 0xece7d8L
                toDark != wasDark && !toDark && it.settings.accentHex == 0xece7d8L -> 0x1a1916L
                else -> it.settings.accentHex
            }
            it.copy(settings = it.settings.copy(theme = theme, accentHex = nextAccent))
        }
    }

    fun setAccent(hex: Long) {
        updateAndPersist { it.copy(settings = it.settings.copy(accentHex = hex)) }
    }

    fun setBigNumerals(on: Boolean) {
        updateAndPersist { it.copy(settings = it.settings.copy(bigNumerals = on)) }
    }

    fun setShowQuick(on: Boolean) {
        updateAndPersist { it.copy(settings = it.settings.copy(showQuick = on)) }
    }

    fun setConcurrentLaps(on: Boolean) {
        updateAndPersist { it.copy(settings = it.settings.copy(concurrentLaps = on)) }
    }

    fun exportText(): String {
        val laps = mutableState.value.laps
        return if (laps.isEmpty()) {
            "(no laps)"
        } else {
            laps.joinToString("\n") { lap ->
                "%02d  %s  %s  @ %s".format(
                    lap.index,
                    lap.name,
                    formatTime(lap.durationMs),
                    formatTime(lap.totalMs),
                )
            }
        }
    }

    override fun onCleared() {
        ticker?.cancel()
        super.onCleared()
    }

    private fun updateAndPersist(transform: (LapLogUiState) -> LapLogUiState) {
        mutableState.value = transform(mutableState.value)
        persistCurrent()
    }

    private fun persistCurrent() {
        val snapshot = mutableState.value.toSnapshot()
        viewModelScope.launch { repository.save(snapshot) }
    }

    private fun finalizeConcurrentDurations(laps: List<Lap>, elapsedMs: Long, concurrent: Boolean): List<Lap> =
        if (!concurrent) laps else laps.map { it.copy(durationMs = (elapsedMs - it.startMs).coerceAtLeast(0)) }

    companion object {
        fun archiveLaps(
            laps: List<Lap>,
            totalMs: Long,
            currentLapName: String,
            pendingStartMs: Long,
            concurrentLaps: Boolean,
        ): List<Lap> {
            val snapshot = if (concurrentLaps) {
                laps.map { it.copy(durationMs = (totalMs - it.startMs).coerceAtLeast(0)) }.toMutableList().also { archived ->
                    if (totalMs > pendingStartMs) {
                        val idx = archived.size + 1
                        archived += Lap(
                            index = idx,
                            name = currentLapName.trimmed().ifEmpty { "Lap $idx" },
                            startMs = pendingStartMs,
                            totalMs = pendingStartMs,
                            durationMs = (totalMs - pendingStartMs).coerceAtLeast(0),
                        )
                    }
                }
            } else {
                laps.toMutableList().also { archived ->
                    if (totalMs > 0) {
                        val previousTotal = archived.lastOrNull()?.totalMs ?: 0
                        if (totalMs > previousTotal) {
                            val idx = archived.size + 1
                            archived += Lap(
                                index = idx,
                                name = currentLapName.trimmed().ifEmpty { "Lap $idx" },
                                startMs = previousTotal,
                                totalMs = totalMs,
                                durationMs = (totalMs - previousTotal).coerceAtLeast(0),
                            )
                        }
                    }
                }
            }
            return snapshot
        }
    }
}

private fun LapLogSnapshot.toUiState(): LapLogUiState =
    LapLogUiState(
        elapsedMs = elapsedMs,
        running = false,
        laps = laps,
        currentLapName = currentLapName,
        sessionTitle = sessionTitle,
        pendingStartMs = pendingStartMs,
        history = history,
        settings = settings,
    )

private fun LapLogUiState.toSnapshot(): LapLogSnapshot =
    LapLogSnapshot(
        elapsedMs = if (running) 0 else elapsedMs,
        laps = laps,
        currentLapName = currentLapName,
        sessionTitle = sessionTitle,
        pendingStartMs = pendingStartMs,
        history = history,
        settings = settings,
    )
