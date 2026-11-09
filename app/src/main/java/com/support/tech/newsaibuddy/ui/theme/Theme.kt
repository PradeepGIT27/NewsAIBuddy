package com.support.tech.newsaibuddy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Define the dark color scheme for the application.
private val DarkColorScheme = darkColorScheme(
    primary = RustPrimary,
    secondary = RustAccent,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onBackground = Color(0xFFE5E7EB),
    onSurface = Color(0xFFE5E7EB)
)

// Define the light color scheme for the application.
private val LightColorScheme = lightColorScheme(
    primary = RustPrimary,
    secondary = RustAccent,
    background = ScreenBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

// Composable function to apply the NewsAIBuddy theme.
@Composable
fun NewsAIBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Determine the color scheme to use based on the system settings and app preferences.
    val colorScheme = when {
        // If dynamic color is enabled and the Android version is 12 or higher, use dynamic colors.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // Use dynamic dark or light color scheme based on the system's dark theme setting.
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // If dynamic color is not available or disabled, use the predefined dark color scheme if dark theme is enabled.
        darkTheme -> DarkColorScheme
        // Otherwise, use the predefined light color scheme.
        else -> LightColorScheme
    }
    // Apply the MaterialTheme with the selected color scheme and typography.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}