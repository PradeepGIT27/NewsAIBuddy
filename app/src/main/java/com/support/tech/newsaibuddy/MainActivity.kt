package com.support.tech.newsaibuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.support.tech.newsaibuddy.ui.navigation.AppNavigationGraph
import com.support.tech.newsaibuddy.ui.theme.NewsAIBuddyTheme
import dagger.hilt.android.AndroidEntryPoint

// Marks this class as an entry point for Hilt dependency injection.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        // Sets the content of the activity using Jetpack Compose.
        setContent {
            NewsAIBuddyTheme {
                // A Surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppEntryPoint()
                }
            }
        }
    }
}

// Composable function that serves as the entry point for the app's UI.
@Composable
fun AppEntryPoint() {
    // Sets up the navigation graph for the application.
    AppNavigationGraph()
}