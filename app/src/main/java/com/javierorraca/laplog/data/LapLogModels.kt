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
    val history: List<Session> = seedHistory(),
    val settings: LapLogSettings = LapLogSettings(),
) {
    companion object {
        fun seedHistory(): List<Session> = listOf(
            Session(
                title = "Sat cookout",
                dateEpochMs = dateMs(2026, 4, 19),
                totalMs = 2_745_000,
                laps = listOf(
                    Lap(index = 1, name = "Coals ready", startMs = 0, totalMs = 420_000, durationMs = 420_000),
                    Lap(index = 2, name = "Ribeye on", startMs = 420_000, totalMs = 1_020_000, durationMs = 600_000),
                    Lap(index = 3, name = "Ribeye flip", startMs = 1_020_000, totalMs = 1_860_000, durationMs = 840_000),
                    Lap(index = 4, name = "Ribeye rest", startMs = 1_860_000, totalMs = 2_745_000, durationMs = 885_000),
                ),
            ),
            Session(
                title = "Weeknight steak",
                dateEpochMs = dateMs(2026, 4, 16),
                totalMs = 1_082_000,
                laps = listOf(
                    Lap(index = 1, name = "Sear", startMs = 0, totalMs = 180_000, durationMs = 180_000),
                    Lap(index = 2, name = "Flip", startMs = 180_000, totalMs = 540_000, durationMs = 360_000),
                    Lap(index = 3, name = "Rest", startMs = 540_000, totalMs = 1_082_000, durationMs = 542_000),
                ),
            ),
            Session(
                title = "Friends over",
                dateEpochMs = dateMs(2026, 4, 12),
                totalMs = 4_210_000,
                laps = listOf(
                    Lap(index = 1, name = "Veg on", startMs = 0, totalMs = 600_000, durationMs = 600_000),
                    Lap(index = 2, name = "Burgers on", startMs = 600_000, totalMs = 1_260_000, durationMs = 660_000),
                    Lap(index = 3, name = "Burgers flip", startMs = 1_260_000, totalMs = 1_920_000, durationMs = 660_000),
                    Lap(index = 4, name = "Corn on", startMs = 1_920_000, totalMs = 2_640_000, durationMs = 720_000),
                    Lap(index = 5, name = "Sausages on", startMs = 2_640_000, totalMs = 3_420_000, durationMs = 780_000),
                    Lap(index = 6, name = "All plated", startMs = 3_420_000, totalMs = 4_210_000, durationMs = 790_000),
                ),
            ),
        )

        private fun dateMs(year: Int, month: Int, day: Int): Long {
            @Suppress("DEPRECATION")
            return Date(year - 1900, month - 1, day).time
        }
    }
}

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
