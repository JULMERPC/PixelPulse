package com.puma.pixelpulse.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

object UserPreferences {

    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    private val KEY_DEFAULT_VOLUME = floatPreferencesKey("default_volume")
    private val KEY_DEFAULT_SPEED = floatPreferencesKey("default_speed")
    private val KEY_DEFAULT_LOOP = booleanPreferencesKey("default_loop")
    private val KEY_DEFAULT_MUTED = booleanPreferencesKey("default_muted")

    enum class ThemeMode {
        LIGHT, DARK, AMOLED, SYSTEM
    }

    fun getThemeMode(context: Context): Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            try {
                ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    fun getDynamicColor(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_DYNAMIC_COLOR] ?: true
        }

    suspend fun setDynamicColor(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    fun getDefaultVolume(context: Context): Flow<Float> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_DEFAULT_VOLUME] ?: 0.5f
        }

    suspend fun setDefaultVolume(context: Context, volume: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_VOLUME] = volume
        }
    }

    fun getDefaultSpeed(context: Context): Flow<Float> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_DEFAULT_SPEED] ?: 1f
        }

    suspend fun setDefaultSpeed(context: Context, speed: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_SPEED] = speed
        }
    }

    fun getDefaultLoop(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_DEFAULT_LOOP] ?: true
        }

    suspend fun setDefaultLoop(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_LOOP] = enabled
        }
    }

    fun getDefaultMuted(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_DEFAULT_MUTED] ?: true
        }

    suspend fun setDefaultMuted(context: Context, muted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_MUTED] = muted
        }
    }

    suspend fun getDefaultVolumeOnce(context: Context): Float =
        context.dataStore.data.map { prefs -> prefs[KEY_DEFAULT_VOLUME] ?: 0.5f }.first()

    suspend fun getDefaultSpeedOnce(context: Context): Float =
        context.dataStore.data.map { prefs -> prefs[KEY_DEFAULT_SPEED] ?: 1f }.first()

    suspend fun getDefaultLoopOnce(context: Context): Boolean =
        context.dataStore.data.map { prefs -> prefs[KEY_DEFAULT_LOOP] ?: true }.first()

    suspend fun getDefaultMutedOnce(context: Context): Boolean =
        context.dataStore.data.map { prefs -> prefs[KEY_DEFAULT_MUTED] ?: true }.first()
}
