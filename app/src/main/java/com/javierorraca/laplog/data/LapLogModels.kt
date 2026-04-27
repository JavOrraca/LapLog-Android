package com.javierorraca.laplog.data

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Serializable
data class Lap(
    val id: String = UUID.randomUUID().toString(),
    val index: Int,
    val name: String,
    val startMs: Long,
    val totalMs: Long,
    val durationMs: Long,
)

@Serializable
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dateEpochMs: Long = System.currentTimeMillis(),
    val totalMs: Long,
    val laps: List<Lap>,
) {
    val lapCount: Int get() = laps.size
    val dayBadge: String get() = dayFormatter.format(Date(dateEpochMs))
    val shortDate: String get() = shortFormatter.format(Date(dateEpochMs))

    companion object {
        private val dayFormatter = SimpleDateFormat("d", Locale.US)
        private val shortFormatter = SimpleDateFormat("MMM d", Locale.US)
    }
}

@Serializable
enum class AppTheme(val label: String) {
    WarmLight("Warm light"),
    Paper("Paper"),
    Dark("Dark"),
}

@Serializable
data class LapLogSettings(
    val theme: AppTheme = AppTheme.Paper,
    val accentHex: Long = 0x1a1916,
    val showQuick: Boolean = true,
    val bigNumerals: Boolean = true,
    val concurrentLaps: Boolean = false,
)

@Serializable
data class LapLogSnapshot(
    val elapsedMs: Long = 0,
    val laps: List<Lap> = emptyList(),
    val currentLapName: String = "",
    val sessionTitle: String = "Grill",
    val pendingStartMs: Long = 0,
    val history: List<Session> = emptyList(),
    val settings: LapLogSettings = LapLogSettings(),
)

val quickPicks = listOf(
    "Steak flip",
    "Steak rest",
    "Chicken flip",
    "Veg on",
    "Veg off",
    "Burger flip",
    "Sausages",
    "Corn",
    "Fish",
    "Coals ready",
    "Sear",
    "Indirect",
)

fun String.trimmed(): String = trim()
