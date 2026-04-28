@file:OptIn(ExperimentalFoundationApi::class)

package com.javierorraca.laplog.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.javierorraca.laplog.data.AppTheme
import com.javierorraca.laplog.data.Lap
import com.javierorraca.laplog.data.Session
import com.javierorraca.laplog.data.quickPicks

@Composable
fun LapLogApp(viewModel: LapLogViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = state.palette
    val colors = if (palette.dark) {
        darkColorScheme(
            background = palette.bg,
            surface = palette.bg,
            primary = colorFromHex(state.accentHex),
            onSurface = palette.text,
        )
    } else {
        lightColorScheme(
            background = palette.bg,
            surface = palette.bg,
            primary = colorFromHex(state.accentHex),
            onSurface = palette.text,
        )
    }

    MaterialTheme(colorScheme = colors) {
        Surface(color = palette.bg, modifier = Modifier.fillMaxSize()) {
            LapLogScreen(state = state, viewModel = viewModel)
            LapLogSheets(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun LapLogScreen(state: LapLogUiState, viewModel: LapLogViewModel) {
    val palette = state.palette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .padding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top).asPaddingValues()),
    ) {
        TopBar(state = state, viewModel = viewModel)
        TimerArea(state = state)
        Controls(state = state, viewModel = viewModel)
        LapsList(
            state = state,
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun TopBar(state: LapLogUiState, viewModel: LapLogViewModel) {
    val palette = state.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditableSessionTitle(
            title = state.sessionTitle,
            palette = palette,
            accent = colorFromHex(state.accentHex),
            onCommit = viewModel::setSessionTitle,
            modifier = Modifier.weight(1f),
        )
        CircleIconButton(
            label = "History",
            palette = palette,
            onClick = { viewModel.showSheet(ActiveSheet.History) },
        ) {
            Icon(Icons.Rounded.History, contentDescription = null, tint = palette.text, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(10.dp))
        CircleIconButton(
            label = "Session menu",
            palette = palette,
            onClick = { viewModel.showSheet(ActiveSheet.Menu) },
        ) {
            Icon(Icons.Rounded.Menu, contentDescription = null, tint = palette.text, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun EditableSessionTitle(
    title: String,
    palette: Palette,
    accent: Color,
    onCommit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(title) { mutableStateOf(title) }
    val focusRequester = remember { FocusRequester() }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "SESSION ·",
            color = palette.muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp,
        )
        Spacer(Modifier.width(6.dp))
        if (editing) {
            BasicTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = palette.text,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                ),
                modifier = Modifier
                    .widthIn(min = 72.dp, max = 160.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (!it.isFocused && editing) {
                            onCommit(draft)
                            editing = false
                        }
                    },
                decorationBox = { inner ->
                    Column {
                        inner()
                        Box(Modifier.fillMaxWidth().height(1.5.dp).background(accent))
                    }
                },
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        } else {
            Text(
                text = title.uppercase(),
                color = palette.text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .combinedClickable(onClick = {
                        draft = title
                        editing = true
                    }),
            )
        }
    }
}

@Composable
private fun TimerArea(state: LapLogUiState) {
    val palette = state.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center,
    ) {
        SecondHandRing(
            ms = state.elapsedMs,
            accent = colorFromHex(state.accentHex),
            track = palette.text,
            modifier = Modifier.size(300.dp),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TimeReadout(
                ms = state.elapsedMs,
                palette = palette,
                big = state.settings.bigNumerals,
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RunningDot(running = state.running, color = colorFromHex(state.accentHex), muted = palette.faint)
                Text(
                    text = statusText(state),
                    color = palette.muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                )
            }
        }
    }
}

private fun statusText(state: LapLogUiState): String {
    if (state.running) return "RUNNING"
    if (state.elapsedMs == 0L) return "READY"
    if (state.laps.isEmpty()) return "PAUSED"
    val anchor = if (state.settings.concurrentLaps) state.pendingStartMs else state.laps.last().totalMs
    val currentLapMs = (state.elapsedMs - anchor).coerceAtLeast(0)
    return "PAUSED · CURRENT LAP ${formatTime(currentLapMs).uppercase()}"
}

@Composable
private fun SecondHandRing(ms: Long, accent: Color, track: Color, modifier: Modifier = Modifier) {
    val frac = ((ms / 1000.0) % 60.0 / 60.0).toFloat()
    Canvas(modifier = modifier.padding(8.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = track.copy(alpha = 0.15f), style = Stroke(width = 2.dp.toPx()))
        drawArc(
            color = accent,
            startAngle = -90f,
            sweepAngle = frac * 360f,
            useCenter = false,
            style = stroke,
        )
    }
}

@Composable
private fun TimeReadout(ms: Long, palette: Palette, big: Boolean) {
    val (main, cs) = splitTime(formatTime(ms))
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = main,
            color = palette.text,
            fontSize = if (big) 72.sp else 60.sp,
            fontWeight = FontWeight.Thin,
            letterSpacing = 0.sp,
            maxLines = 1,
        )
        Text(
            text = cs,
            color = palette.muted,
            fontSize = if (big) 30.sp else 24.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(bottom = 6.dp),
        )
    }
}

@Composable
private fun RunningDot(running: Boolean, color: Color, muted: Color) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "pulseAlpha",
    )
    Box(
        Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(if (running) color.copy(alpha = alpha) else muted),
    )
}

@Composable
private fun Controls(state: LapLogUiState, viewModel: LapLogViewModel) {
    val canLeft = state.running || state.elapsedMs > 0
    val leftLabel = if (state.running) "Lap" else if (state.elapsedMs > 0) "Reset" else "Lap"
    val rightLabel = if (state.running) "Stop" else "Start"
    val rightHex = if (state.running) 0xc92a2aL else state.accentHex
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .padding(top = 10.dp, bottom = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundButton(
            label = leftLabel,
            palette = state.palette,
            accentHex = state.accentHex,
            primary = false,
            enabled = canLeft,
            onClick = viewModel::leftControl,
        )
        Spacer(Modifier.weight(1f))
        RoundButton(
            label = rightLabel,
            palette = state.palette,
            accentHex = rightHex,
            primary = true,
            enabled = true,
            icon = if (state.running) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
            onClick = viewModel::toggleRunning,
        )
    }
}

@Composable
private fun RoundButton(
    label: String,
    palette: Palette,
    accentHex: Long,
    primary: Boolean,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    val accent = colorFromHex(accentHex)
    val bg = if (primary) accent else palette.chipBg
    val fg = if (primary) {
        if (isLightHex(accentHex)) Color(0xff1a1916) else Color.White
    } else {
        palette.text
    }
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = fg, disabledContentColor = fg.copy(alpha = 0.35f)),
        modifier = Modifier
            .size(84.dp)
            .shadow(if (primary) 12.dp else 0.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(bg.copy(alpha = if (enabled) 1f else 0.35f))
            .semantics { contentDescription = label },
    ) {
        if (icon == null) {
            Text(text = label, color = fg, fontSize = 17.sp, fontWeight = FontWeight.Medium)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
                Text(text = label, color = fg, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CircleIconButton(label: String, palette: Palette, onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(palette.chipBg)
            .semantics { contentDescription = label },
    ) {
        content()
    }
}

@Composable
private fun LapsList(state: LapLogUiState, viewModel: LapLogViewModel, modifier: Modifier = Modifier) {
    val palette = state.palette
    val showsCurrent = state.elapsedMs > 0 && if (state.settings.concurrentLaps) {
        state.elapsedMs > state.pendingStartMs
    } else {
        state.laps.isEmpty() || state.elapsedMs > (state.laps.lastOrNull()?.totalMs ?: 0)
    }
    LazyColumn(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(palette.card),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Laps", color = palette.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                if (state.laps.isNotEmpty()) {
                    Text(" · ${state.laps.size}", color = palette.muted, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.weight(1f))
                Text("TAP TO RENAME", color = palette.faint, fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.2.sp)
            }
        }
        if (showsCurrent) {
            item { CurrentLapRow(state = state, viewModel = viewModel) }
        }
        if (state.laps.isEmpty() && state.elapsedMs == 0L) {
            item { EmptyState(state) }
        } else {
            itemsIndexed(state.laps.asReversed(), key = { _, lap -> lap.id }) { reversedIndex, lap ->
                val index = state.laps.lastIndex - reversedIndex
                LapRow(
                    lap = lap,
                    index = index,
                    isLatest = index == state.laps.lastIndex,
                    state = state,
                    viewModel = viewModel,
                )
                if (index > 0) Separator(palette)
            }
        }
        item { Spacer(Modifier.height(50.dp)) }
    }
}

@Composable
private fun CurrentLapRow(state: LapLogUiState, viewModel: LapLogViewModel) {
    val palette = state.palette
    val idx = state.laps.size + 1
    val anchor = if (state.settings.concurrentLaps) state.pendingStartMs else state.laps.lastOrNull()?.totalMs ?: 0
    val curr = (state.elapsedMs - anchor).coerceAtLeast(0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.chipBg)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("%02d".format(idx), color = colorFromHex(state.accentHex), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, modifier = Modifier.width(28.dp))
        BasicTextField(
            value = state.currentLapName,
            onValueChange = viewModel::setCurrentLapName,
            singleLine = true,
            textStyle = TextStyle(color = palette.text, fontSize = 17.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (state.currentLapName.isEmpty()) Text("Name this lap...", color = palette.faint, fontSize = 17.sp)
                inner()
            },
        )
        Spacer(Modifier.width(8.dp))
        if (state.running) RunningDot(true, colorFromHex(state.accentHex), palette.faint) else Text("PAUSED", color = palette.muted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(10.dp))
        Text(formatTime(curr), color = palette.text, fontSize = 20.sp, fontWeight = FontWeight.Normal)
    }
    Separator(palette)
}

@Composable
private fun LapRow(lap: Lap, index: Int, isLatest: Boolean, state: LapLogUiState, viewModel: LapLogViewModel) {
    val palette = state.palette
    var editing by remember(lap.id) { mutableStateOf(false) }
    var draft by remember(lap.id, lap.name) { mutableStateOf(lap.name) }
    val focusRequester = remember { FocusRequester() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                role = Role.Button,
                onClick = { editing = true },
                onLongClick = { viewModel.openQuickPick(index) },
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("%02d".format(lap.index), color = palette.faint, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(28.dp).padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            if (editing) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    textStyle = TextStyle(color = palette.text, fontSize = 17.sp, fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Medium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            if (!it.isFocused && editing) {
                                viewModel.renameLap(index, draft)
                                editing = false
                            }
                        },
                    decorationBox = { inner ->
                        Column {
                            inner()
                            Box(Modifier.fillMaxWidth().height(1.5.dp).background(colorFromHex(state.accentHex)))
                        }
                    },
                )
                LaunchedEffect(editing) { focusRequester.requestFocus() }
            } else {
                Text(lap.name, color = palette.text, fontSize = 17.sp, fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("at ${formatTime(lap.totalMs)}", color = palette.muted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        Text(formatTime(lap.durationMs), color = palette.text, fontSize = 20.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun EmptyState(state: LapLogUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Start the timer and tap Lap each time you want to mark a moment. Then tap any lap to rename it: mile 1, Ribeye flip, stage 2, whatever you're tracking.",
            color = state.palette.muted,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Text(
            text = "Tip: tap Session · ${state.sessionTitle.uppercase()} in the top-left to rename this session.",
            color = state.palette.faint,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Separator(palette: Palette) {
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(palette.sep))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LapLogSheets(state: LapLogUiState, viewModel: LapLogViewModel) {
    if (state.activeSheet == ActiveSheet.None) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = viewModel::closeSheet,
        sheetState = sheetState,
        containerColor = state.palette.bg,
        contentColor = state.palette.text,
    ) {
        when (state.activeSheet) {
            ActiveSheet.History -> HistoryPanel(state, viewModel)
            ActiveSheet.Menu -> MenuSheet(state, viewModel)
            ActiveSheet.Settings -> SettingsPanel(state, viewModel)
            ActiveSheet.QuickPick -> QuickPickSheet(state, viewModel)
            ActiveSheet.None -> Unit
        }
    }
}

@Composable
private fun SheetHeader(title: String, state: LapLogUiState, onClose: () -> Unit, leading: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) leading()
        Text(title, color = state.palette.text, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        TextButton(onClick = onClose) { Text("Done", color = state.palette.muted) }
    }
}

@Composable
private fun HistoryPanel(state: LapLogUiState, viewModel: LapLogViewModel) {
    val session = state.selectedSession
    if (session != null) {
        SessionDetail(state = state, session = session, viewModel = viewModel)
        return
    }
    SheetHeader("History", state, viewModel::closeSheet)
    LazyColumn(Modifier.height(520.dp)) {
        if (state.history.isEmpty()) {
            item {
                Text(
                    "Sessions will appear here once you reset.",
                    color = state.palette.muted,
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            itemsIndexed(state.history, key = { _, session -> session.id }) { i, s ->
                HistoryRow(state, s, onClick = { viewModel.selectSession(s.id) }, onRename = { viewModel.renameSession(s.id, it) })
                if (i < state.history.lastIndex) Separator(state.palette)
            }
        }
    }
}

@Composable
private fun HistoryRow(state: LapLogUiState, session: Session, onClick: () -> Unit, onRename: (String) -> Unit) {
    var editing by remember(session.id) { mutableStateOf(false) }
    var draft by remember(session.id, session.title) { mutableStateOf(session.title) }
    val focusRequester = remember { FocusRequester() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = { if (!editing) onClick() }, onLongClick = { editing = true })
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(session.dayBadge, color = state.palette.muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center, modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(state.palette.chipBg).padding(top = 11.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            if (editing) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    textStyle = TextStyle(color = state.palette.text, fontSize = 16.sp),
                    modifier = Modifier.focusRequester(focusRequester).onFocusChanged {
                        if (!it.isFocused && editing) {
                            onRename(draft)
                            editing = false
                        }
                    },
                )
                LaunchedEffect(editing) { focusRequester.requestFocus() }
            } else {
                Text(session.title, color = state.palette.text, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("${session.shortDate} · ${session.lapCount} lap${if (session.lapCount == 1) "" else "s"}", color = state.palette.muted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
        Text(formatTime(session.totalMs), color = state.palette.text, fontSize = 17.sp)
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = state.palette.faint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SessionDetail(state: LapLogUiState, session: Session, viewModel: LapLogViewModel) {
    SheetHeader(
        title = "Session details",
        state = state,
        onClose = { viewModel.selectSession(null) },
        leading = {
            IconButton(onClick = { viewModel.selectSession(null) }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = state.palette.text)
            }
        },
    )
    Column(
        Modifier
            .height(560.dp)
            .verticalScroll(rememberScrollState())
    .padding(bottom = 24.dp),
    ) {
        EditableDetailTitle(session, state, onCommit = { viewModel.renameSession(session.id, it) })
        Row(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(session.shortDate.uppercase(), color = state.palette.muted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("·", color = state.palette.muted, fontSize = 11.sp)
            Text("${session.laps.size} LAP${if (session.laps.size == 1) "" else "S"}", color = state.palette.muted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("· TOTAL ${formatTime(session.totalMs)}", color = state.palette.text, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Laps", color = state.palette.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("TAP TO RENAME", color = state.palette.faint, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        session.laps.forEachIndexed { i, lap ->
            SessionLapRow(state, lap, onRename = { viewModel.renameSessionLap(session.id, i, it) })
            if (i < session.laps.lastIndex) Separator(state.palette)
        }
    }
}

@Composable
private fun EditableDetailTitle(session: Session, state: LapLogUiState, onCommit: (String) -> Unit) {
    var editing by remember(session.id) { mutableStateOf(false) }
    var draft by remember(session.id, session.title) { mutableStateOf(session.title) }
    val focusRequester = remember { FocusRequester() }
    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
        if (editing) {
            BasicTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                textStyle = TextStyle(color = state.palette.text, fontSize = 26.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged {
                    if (!it.isFocused && editing) {
                        onCommit(draft)
                        editing = false
                    }
                },
            )
            LaunchedEffect(editing) { focusRequester.requestFocus() }
        } else {
            Text(session.title, color = state.palette.text, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.combinedClickable(onClick = { editing = true }))
        }
    }
}

@Composable
private fun SessionLapRow(state: LapLogUiState, lap: Lap, onRename: (String) -> Unit) {
    var editing by remember(lap.id) { mutableStateOf(false) }
    var draft by remember(lap.id, lap.name) { mutableStateOf(lap.name) }
    val focusRequester = remember { FocusRequester() }
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = { editing = true }).padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.Top) {
        Text("%02d".format(lap.index), color = state.palette.faint, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(28.dp).padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            if (editing) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    textStyle = TextStyle(color = state.palette.text, fontSize = 17.sp),
                    modifier = Modifier.focusRequester(focusRequester).onFocusChanged {
                        if (!it.isFocused && editing) {
                            onRename(draft)
                            editing = false
                        }
                    },
                )
                LaunchedEffect(editing) { focusRequester.requestFocus() }
            } else {
                Text(lap.name, color = state.palette.text, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("at ${formatTime(lap.totalMs)}", color = state.palette.muted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        Text(formatTime(lap.durationMs), color = state.palette.text, fontSize = 20.sp)
    }
}

@Composable
private fun MenuSheet(state: LapLogUiState, viewModel: LapLogViewModel) {
    val clipboard = LocalClipboardManager.current
    SheetHeader("Session", state, viewModel::closeSheet)
    Column(Modifier.padding(bottom = 28.dp)) {
        MenuRow(state, Icons.Rounded.PostAdd, "New session", "Archive current & reset") {
            viewModel.resetAndArchive()
            viewModel.closeSheet()
        }
        Separator(state.palette)
        MenuRow(state, Icons.Rounded.ContentCopy, "Export laps", "Copy to clipboard") {
            clipboard.setText(AnnotatedString(viewModel.exportText()))
            viewModel.closeSheet()
        }
        Separator(state.palette)
        MenuRow(state, Icons.Rounded.Settings, "Settings", "Theme, accent, display") {
            viewModel.showSheet(ActiveSheet.Settings)
        }
    }
}

@Composable
private fun MenuRow(state: LapLogUiState, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String, action: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = action).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = state.palette.text, modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(state.palette.chipBg).padding(8.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = state.palette.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(sub, color = state.palette.muted, fontSize = 12.sp)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = state.palette.faint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SettingsPanel(state: LapLogUiState, viewModel: LapLogViewModel) {
    SheetHeader("Settings", state, viewModel::closeSheet)
    Column(
        Modifier
            .height(560.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SettingsSection("THEME", state) {
            AppTheme.entries.forEach { theme ->
                SettingsClickableRow(state, title = theme.label, selected = state.settings.theme == theme, swatch = paletteFor(theme).bg) {
                    viewModel.setTheme(theme)
                }
            }
        }
        SettingsSection("ACCENT", state) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Color", color = state.palette.text, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.accentSwatches.forEach { hex ->
                        Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colorFromHex(hex))
                                .combinedClickable(onClick = { viewModel.setAccent(hex) })
                        ) {
                            if (state.accentHex == hex) {
                                Canvas(Modifier.fillMaxSize()) {
                                    drawCircle(color = state.palette.text, style = Stroke(width = 2.dp.toPx()))
                                }
                            }
                        }
                    }
                }
            }
        }
        SettingsSection("DISPLAY", state) {
            ToggleRow(state, "Big numerals", "Larger timer readout", state.settings.bigNumerals, viewModel::setBigNumerals)
            Separator(state.palette)
            ToggleRow(state, "Quick picks menu", "Long-press a lap for presets", state.settings.showQuick, viewModel::setShowQuick)
            Separator(state.palette)
            ToggleRow(state, "Parallel laps", "Each lap tracks time until session stops", state.settings.concurrentLaps, viewModel::setConcurrentLaps)
        }
    }
}

@Composable
private fun SettingsSection(title: String, state: LapLogUiState, content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(title, color = state.palette.muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, letterSpacing = 1.2.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(state.palette.card)) {
            content()
        }
    }
}

@Composable
private fun SettingsClickableRow(state: LapLogUiState, title: String, selected: Boolean, swatch: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(swatch))
        Spacer(Modifier.width(12.dp))
        Text(title, color = state.palette.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
        if (selected) Icon(Icons.Rounded.Check, contentDescription = "Selected", tint = colorFromHex(state.accentHex), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ToggleRow(state: LapLogUiState, title: String, sub: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = state.palette.text, fontSize = 15.sp)
            Text(sub, color = state.palette.muted, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickPickSheet(state: LapLogUiState, viewModel: LapLogViewModel) {
    SheetHeader("Quick pick", state, viewModel::closeSheet)
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        quickPicks.forEach { pick ->
            TextButton(
                onClick = { viewModel.applyQuickPick(pick) },
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(state.palette.chipBg),
            ) {
                Text(pick, color = state.palette.text, fontSize = 13.sp)
            }
        }
    }
    Separator(state.palette)
    TextButton(
        onClick = {
            state.quickPickLapIndex?.let(viewModel::deleteLap)
            viewModel.closeSheet()
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
    ) {
        Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xffc92a2a))
        Spacer(Modifier.width(8.dp))
        Text("Delete lap", color = Color(0xffc92a2a), fontSize = 15.sp)
    }
    Spacer(Modifier.height(24.dp))
}
