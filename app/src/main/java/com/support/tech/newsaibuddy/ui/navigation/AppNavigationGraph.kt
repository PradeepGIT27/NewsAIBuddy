package com.support.tech.newsaibuddy.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.support.tech.newsaibuddy.ui.components.AppLoader
import com.support.tech.newsaibuddy.ui.screens.chatbot.ChatBotScreen
import com.support.tech.newsaibuddy.ui.screens.login.LoginScreen
import com.support.tech.newsaibuddy.ui.screens.news.NewsScreen
import com.support.tech.newsaibuddy.ui.screens.reference.ReferenceScreen
import com.support.tech.newsaibuddy.ui.theme.ScreenBackground
import com.support.tech.newsaibuddy.ui.viewmodel.auth.AuthViewModel

/**
 * Composable function that defines the navigation graph for the application.
 * Handles authentication gating: shows Splash while loading session,
 * LoginScreen if not signed in, and NewsScreen if signed in.
 */
@Composable
fun AppNavigationGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        val loggedInState = isLoggedIn ?: return@LaunchedEffect
        val currentRoute = navController.currentDestination?.route
        if (loggedInState && currentRoute == "loginScreen") {
            navController.navigate("newsScreen") {
                popUpTo("loginScreen") { inclusive = true }
            }
        } else if (!loggedInState && currentRoute != "loginScreen" && currentRoute != null) {
            navController.navigate("loginScreen") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    if (isLoggedIn == null) {
        // Splash / Loading container while session state is loaded asynchronously from DataStore
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground),
            contentAlignment = Alignment.Center
        ) {
            AppLoader()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn == true) "newsScreen" else "loginScreen"
        ) {
            composable("loginScreen") {
                LoginScreen(authViewModel = authViewModel)
            }

            composable("newsScreen") {
                NewsScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable("chatBotScreen") {
                ChatBotScreen(navController)
            }

            composable("referenceScreen/{url}") { navBackStack ->
                val rawArg = navBackStack.arguments?.getString("url").orEmpty()
                val resolvedUrl = try {
                    val decodedBytes = android.util.Base64.decode(rawArg, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                    val decodedString = String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8)
                    if (decodedString.startsWith("http://") || decodedString.startsWith("https://")) {
                        decodedString
                    } else {
                        java.net.URLDecoder.decode(rawArg, java.nio.charset.StandardCharsets.UTF_8.toString())
                    }
                } catch (_: Exception) {
                    try {
                        java.net.URLDecoder.decode(rawArg, java.nio.charset.StandardCharsets.UTF_8.toString())
                    } catch (_: Exception) {
                        rawArg
                    }
                }
                ReferenceScreen(navController, resolvedUrl)
            }
        }
    }
}
