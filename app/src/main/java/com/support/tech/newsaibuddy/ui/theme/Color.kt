package com.support.tech.newsaibuddy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warm terracotta / rust palette from Figma design
val RustPrimary = Color(0xFFBA4A00)
val RustDark = Color(0xFFA04000)
val RustAccent = Color(0xFFC05621)

// Dynamic theme-aware colors (automatically adapt to Light/Dark Mode)
val RustSoftBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.surfaceVariant

val ScreenBackground: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val CardBackground: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val CardBorder: Color
    @Composable
    get() = MaterialTheme.colorScheme.outlineVariant

val TextPrimary: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextMuted: Color
    @Composable
    get() = MaterialTheme.colorScheme.outline

val UserBubbleBackground: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF121214)) Color(0xFF2A2318) else Color(0xFFFFFBEB)

val UserBubbleBorder: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF121214)) Color(0xFF4A3B1F) else Color(0xFFFDE68A)

val BotBadgeBackground: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF121214)) Color(0xFF332715) else Color(0xFFFEF3C7)

val BotAvatarBg = Color(0xFFBA4A00)

// Legacy colors kept for backward compatibility
val appColor80 = Color(0xFFF5C682)
val appColor40 = Color(0xFFBA4A00)
val appColor20 = Color(0xFFFFE0B2)
val appColorBrown80 = Color(0xFFD2B48C)
