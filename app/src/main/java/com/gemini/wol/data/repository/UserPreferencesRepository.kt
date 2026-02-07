package com.gemini.wol.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppTheme {
    DARK,
    AMOLED,
    SYSTEM
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val ACCENT_COLOR = androidx.datastore.preferences.core.intPreferencesKey("accent_color")
    }

    val appTheme: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM
            }
        }

    val accentColor: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ACCENT_COLOR] ?: 0xFF6200EE.toInt() // Default Purple
        }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    suspend fun setAccentColor(color: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCENT_COLOR] = color
        }
    }
}
