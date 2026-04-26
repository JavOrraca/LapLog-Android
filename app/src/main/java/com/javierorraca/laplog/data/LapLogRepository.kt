package com.javierorraca.laplog.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.lapLogDataStore by preferencesDataStore("laplog")

class LapLogRepository(context: Context) {
    private val dataStore = context.applicationContext.lapLogDataStore
    private val snapshotKey = stringPreferencesKey("snapshot")

    val snapshots: Flow<LapLogSnapshot?> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
        }
        .map { preferences ->
            preferences[snapshotKey]?.let { decodeSnapshot(it) }
        }

    suspend fun save(snapshot: LapLogSnapshot) {
        val encoded = json.encodeToString(LapLogSnapshot.serializer(), snapshot)
        dataStore.edit { it[snapshotKey] = encoded }
    }

    fun encode(snapshot: LapLogSnapshot): String =
        json.encodeToString(LapLogSnapshot.serializer(), snapshot)

    fun decodeSnapshot(raw: String): LapLogSnapshot? =
        try {
            json.decodeFromString(LapLogSnapshot.serializer(), raw)
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: SerializationException) {
            null
        }

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}
