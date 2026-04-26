package com.javierorraca.laplog.ui

import java.util.Locale

fun formatTime(ms: Long): String {
    val safe = ms.coerceAtLeast(0)
    val cs = (safe / 10) % 100
    val s = (safe / 1_000) % 60
    val mins = (safe / 60_000) % 60
    val h = safe / 3_600_000
    return if (h > 0) {
        String.format(Locale.US, "%d:%02d:%02d.%02d", h, mins, s, cs)
    } else {
        String.format(Locale.US, "%02d:%02d.%02d", mins, s, cs)
    }
}

fun splitTime(text: String): Pair<String, String> {
    val dot = text.lastIndexOf('.')
    return if (dot >= 0) text.substring(0, dot) to text.substring(dot) else text to ""
}
