package com.support.tech.newsaibuddy.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.support.tech.newsaibuddy.data.local.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 * ViewModel managing authentication state using Google Sign-In via Credential Manager.
 * Session is persisted in DataStore so it survives app restarts.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    /** Whether the user is currently logged in (observed from DataStore). */
    val isLoggedIn: StateFlow<Boolean> = userSessionManager.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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
