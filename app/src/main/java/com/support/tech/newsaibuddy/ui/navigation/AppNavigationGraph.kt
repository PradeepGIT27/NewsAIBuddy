package com.support.tech.newsaibuddy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.support.tech.newsaibuddy.ui.screens.chatbot.ChatBotScreen
import com.support.tech.newsaibuddy.ui.screens.news.NewsScreen
import com.support.tech.newsaibuddy.ui.screens.reference.ReferenceScreen

@Composable
fun AppNavigationGraph() {

    val navController = rememberNavController()
    NavHost(navController, startDestination = "newsScreen") {
        composable("newsScreen") { NewsScreen(navController) }
        composable("chatBotScreen") { ChatBotScreen(navController) }
        composable("referenceScreen/{url}") { navBackStack ->
            val url = navBackStack.arguments?.getString("url")
            ReferenceScreen(navController, url.toString())
        }
    }
}
