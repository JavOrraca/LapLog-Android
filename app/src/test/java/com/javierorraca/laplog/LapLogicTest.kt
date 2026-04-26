package com.javierorraca.laplog

import com.javierorraca.laplog.data.Lap
import com.javierorraca.laplog.data.LapLogRepository
import com.javierorraca.laplog.data.LapLogSettings
import com.javierorraca.laplog.data.LapLogSnapshot
import com.javierorraca.laplog.ui.LapLogViewModel
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LapLogicTest {
    @Test
    fun sequentialArchiveIncludesInProgressLap() {
        val archived = LapLogViewModel.archiveLaps(
            laps = listOf(
                Lap(index = 1, name = "Warmup", startMs = 0, totalMs = 1_000, durationMs = 1_000),
            ),
            totalMs = 2_500,
            currentLapName = "Sprint",
            pendingStartMs = 0,
            concurrentLaps = false,
        )

        assertEquals(2, archived.size)
        assertEquals("Sprint", archived[1].name)
        assertEquals(1_000, archived[1].startMs)
        assertEquals(2_500, archived[1].totalMs)
        assertEquals(1_500, archived[1].durationMs)
    }

    @Test
    fun parallelArchiveFreezesAllLapsAndAddsPendingLap() {
        val archived = LapLogViewModel.archiveLaps(
            laps = listOf(
                Lap(index = 1, name = "Steak", startMs = 0, totalMs = 0, durationMs = 1_000),
            ),
            totalMs = 4_000,
            currentLapName = "Veg",
            pendingStartMs = 1_500,
            concurrentLaps = true,
        )

        assertEquals(2, archived.size)
        assertEquals(4_000, archived[0].durationMs)
        assertEquals("Veg", archived[1].name)
        assertEquals(1_500, archived[1].totalMs)
        assertEquals(2_500, archived[1].durationMs)
    }

    @Test
    fun snapshotRoundTripsThroughJson() {
        val snapshot = LapLogSnapshot(
            elapsedMs = 123,
            history = emptyList(),
            settings = LapLogSettings(accentHex = 0xc2410c),
        )
        val encoded = LapLogRepository.json.encodeToString(LapLogSnapshot.serializer(), snapshot)
        val decoded = Json.decodeFromString(LapLogSnapshot.serializer(), encoded)

        assertNotNull(decoded)
        assertEquals(123, decoded.elapsedMs)
        assertEquals(0xc2410c, decoded.settings.accentHex)
    }
}
