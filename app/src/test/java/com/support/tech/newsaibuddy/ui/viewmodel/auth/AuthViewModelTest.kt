package com.support.tech.newsaibuddy.ui.viewmodel.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthViewModelTest {

    @Test
    fun defaultUserProfile_isEmpty() {
        val profile = UserProfile()
        assertEquals("", profile.displayName)
        assertEquals("", profile.email)
        assertEquals("", profile.userId)
    }

    @Test
    fun userProfile_customValues_areSetCorrectly() {
        val profile = UserProfile(
            displayName = "Alex Tester",
            email = "alex@example.com",
            userId = "user_123"
        )
        assertEquals("Alex Tester", profile.displayName)
        assertEquals("alex@example.com", profile.email)
        assertEquals("user_123", profile.userId)
    }
}
