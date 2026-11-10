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
 * Manages user session data using DataStore Preferences.
 * Handles login state persistence so the session survives app close/open.
 */
class UserSessionManager(private val context: Context) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PHOTO_URL = stringPreferencesKey("photo_url")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
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
            prefs.clear()
        }
    }
}
