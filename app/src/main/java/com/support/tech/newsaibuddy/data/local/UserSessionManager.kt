package com.support.tech.newsaibuddy.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

/**
 * Manages user session data and preferences using DataStore Preferences.
 * Handles login state and dark mode theme preferences so they survive app restarts.
 */
class UserSessionManager(private val context: Context) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PHOTO_URL = stringPreferencesKey("photo_url")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_DARK_MODE_SETTING = stringPreferencesKey("dark_mode_setting")
    }

    val isLoggedIn: Flow<Boolean> = context.userDataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val displayName: Flow<String> = context.userDataStore.data.map { prefs ->
        prefs[KEY_DISPLAY_NAME] ?: ""
    }

    val email: Flow<String> = context.userDataStore.data.map { prefs ->
        prefs[KEY_EMAIL] ?: ""
    }

    val photoUrl: Flow<String> = context.userDataStore.data.map { prefs ->
        prefs[KEY_PHOTO_URL] ?: ""
    }

    val userId: Flow<String> = context.userDataStore.data.map { prefs ->
        prefs[KEY_USER_ID] ?: ""
    }

    /**
     * Dark mode setting: "system" (default), "light", or "dark".
     */
    val darkModeSetting: Flow<String> = context.userDataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE_SETTING] ?: "system"
    }

    suspend fun setDarkModeSetting(setting: String) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_DARK_MODE_SETTING] = setting
        }
    }

    suspend fun saveSession(
        displayName: String,
        email: String,
        photoUrl: String,
        userId: String
    ) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_DISPLAY_NAME] = displayName
            prefs[KEY_EMAIL] = email
            prefs[KEY_PHOTO_URL] = photoUrl
            prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        context.userDataStore.edit { prefs ->
            // Clear user credentials but preserve app appearance preferences
            val currentDarkMode = prefs[KEY_DARK_MODE_SETTING]
            prefs.clear()
            if (currentDarkMode != null) {
                prefs[KEY_DARK_MODE_SETTING] = currentDarkMode
            }
        }
    }
}
