package com.support.tech.newsaibuddy.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.support.tech.newsaibuddy.data.local.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Holds the current user profile data.
 */
data class UserProfile(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val userId: String = ""
)

/**
 * ViewModel managing authentication state and user preference settings (such as Dark Mode).
 * Session & preferences are persisted in DataStore so they survive app restarts.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    /** Whether the user is currently logged in (observed from DataStore). null = loading session. */
    val isLoggedIn: StateFlow<Boolean?> = userSessionManager.isLoggedIn
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Dark mode theme setting: "system" (default), "light", or "dark". */
    val darkModeSetting: StateFlow<String> = userSessionManager.darkModeSetting
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Load cached user profile from DataStore
        viewModelScope.launch {
            userSessionManager.displayName.collect { name ->
                _userProfile.value = _userProfile.value.copy(displayName = name)
            }
        }
        viewModelScope.launch {
            userSessionManager.email.collect { email ->
                _userProfile.value = _userProfile.value.copy(email = email)
            }
        }
        viewModelScope.launch {
            userSessionManager.photoUrl.collect { url ->
                _userProfile.value = _userProfile.value.copy(photoUrl = url)
            }
        }
        viewModelScope.launch {
            userSessionManager.userId.collect { id ->
                _userProfile.value = _userProfile.value.copy(userId = id)
            }
        }
    }

    /**
     * Updates dark mode setting ("system", "light", or "dark") and persists it.
     */
    fun setDarkModeSetting(setting: String) {
        viewModelScope.launch {
            userSessionManager.setDarkModeSetting(setting)
        }
    }

    /**
     * Called after a successful Google Sign-In.
     * Saves session in DataStore for persistence.
     */
    fun onSignInSuccess(displayName: String, email: String, photoUrl: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            userSessionManager.saveSession(displayName, email, photoUrl, userId)
            _userProfile.value = UserProfile(displayName, email, photoUrl, userId)
            _isLoading.value = false
        }
    }

    /**
     * Signs in as a demo/guest user (enabling Play Store reviewers to fully access app features).
     */
    fun signInAsGuest() {
        onSignInSuccess(
            displayName = "Demo Reviewer",
            email = "playconsole.reviewer@example.com",
            photoUrl = "",
            userId = "demo_reviewer_123"
        )
    }

    fun onSignInError(message: String) {
        _errorMessage.value = message
        _isLoading.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    /**
     * Signs the user out by clearing the DataStore session.
     */
    fun signOut() {
        viewModelScope.launch {
            userSessionManager.clearSession()
            _userProfile.value = UserProfile()
        }
    }
}
