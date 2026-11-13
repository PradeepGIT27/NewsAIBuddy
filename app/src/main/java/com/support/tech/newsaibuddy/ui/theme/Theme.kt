package com.support.tech.newsaibuddy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Define the dark color scheme for the application.
private val DarkColorScheme = darkColorScheme(
    primary = RustPrimary,
    secondary = RustAccent,
    background = Color(0xFF121214),
    surface = Color(0xFF1E1E22),
    surfaceVariant = Color(0xFF2E1A0F),
    onPrimary = Color.White,
    onBackground = Color(0xFFF9FAFB),
    onSurface = Color(0xFFF9FAFB),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF6B7280),
    outlineVariant = Color(0xFF2E2E34)
)

// Define the light color scheme for the application.
private val LightColorScheme = lightColorScheme(
    primary = RustPrimary,
    secondary = RustAccent,
    background = Color(0xFFFAF9F6),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFF3E0),
    onPrimary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFF9CA3AF),
    outlineVariant = Color(0xFFE5E7EB)
)

// Composable function to apply the NewsAIBuddy theme.
@Composable
fun NewsAIBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Determine the color scheme to use based on settings and preferences.
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
