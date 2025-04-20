package com.support.tech.newsaibuddy.ui.navigation

import ReferenceScreen
import androidx.compose.runtime.Composable

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.support.tech.newsaibuddy.ui.screens.news.NewsScreen

@Composable
fun AppNavigationGraph() {


    val navController = rememberNavController()
    NavHost(navController, startDestination = "newsScreen") {
        composable("newsScreen") { NewsScreen(navController) }
        composable("referenceScreen/{url}") { navBackStack ->
            val url = navBackStack.arguments?.getString("url")
            ReferenceScreen(navController, url.toString())
        }
    }
}
