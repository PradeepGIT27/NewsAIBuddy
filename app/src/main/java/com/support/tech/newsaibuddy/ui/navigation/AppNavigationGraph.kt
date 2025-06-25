package com.support.tech.newsaibuddy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.support.tech.newsaibuddy.ui.screens.chatbot.ChatBotScreen
import com.support.tech.newsaibuddy.ui.screens.news.NewsScreen
import com.support.tech.newsaibuddy.ui.screens.reference.ReferenceScreen

/**
 * Composable function that defines the navigation graph for the application.
 * It uses Jetpack Navigation Compose to manage navigation between different screens.
 */
@Composable
fun AppNavigationGraph() {

    // Create a NavController instance that will be used to navigate between screens.
    val navController = rememberNavController()

    // Define the NavHost, which is a container for all the navigation destinations.
    // The startDestination specifies the screen that will be displayed when the app is launched.
    NavHost(navController, startDestination = "newsScreen") {
        // Define a composable destination for the news screen.
        composable("newsScreen") { NewsScreen(navController) }
        // Define a composable destination for the chatbot screen.
        composable("chatBotScreen") { ChatBotScreen(navController) }
        // Define a composable destination for the reference screen, which takes a URL as an argument.
        composable("referenceScreen/{url}") { navBackStack ->
            // Retrieve the URL argument from the NavBackStackEntry.
            val url = navBackStack.arguments?.getString("url")
            // Display the ReferenceScreen with the retrieved URL.
            ReferenceScreen(navController, url.toString())
        }
    }
}
