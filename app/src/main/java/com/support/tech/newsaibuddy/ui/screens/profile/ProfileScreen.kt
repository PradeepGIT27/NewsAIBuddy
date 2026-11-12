package com.support.tech.newsaibuddy.ui.screens.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.support.tech.newsaibuddy.BuildConfig
import com.support.tech.newsaibuddy.data.local.ChatHistoryManager
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.RustSoftBg
import com.support.tech.newsaibuddy.ui.theme.ScreenBackground
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.viewmodel.auth.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    savedArticleCount: Int,
    onClearSavedArticles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by authViewModel.userProfile.collectAsState()
    val darkModeSetting by authViewModel.darkModeSetting.collectAsState()

    val chatHistoryManager = remember { ChatHistoryManager(context) }
    val savedConversations by chatHistoryManager.conversations.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Title
        Text(
            text = "Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Profile Header (Avatar + Name & Email displayed ONCE)
        Surface(
            shape = CircleShape,
            color = RustSoftBg,
            border = BorderStroke(3.dp, RustPrimary),
            shadowElevation = 6.dp,
            modifier = Modifier.size(96.dp)
        ) {
            if (userProfile.photoUrl.isNotBlank()) {
                com.support.tech.newsaibuddy.ui.components.ShimmerAsyncImage(
                    model = userProfile.photoUrl,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Avatar",
                        tint = RustPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Display Name (Primary Header instance)
        Text(
            text = userProfile.displayName.ifBlank { "NewsAI Reader" },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Email (Primary Header instance)
        if (userProfile.email.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userProfile.email,
                fontSize = 14.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Verified Google Account Status Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFE8F5E9),
            border = BorderStroke(1.dp, Color(0xFFA5D6A7))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Google Account Verified",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 1. APPEARANCE CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "APPEARANCE & THEME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = RustPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                ThemeModeSelector(
                    selectedMode = darkModeSetting,
                    onModeSelected = { authViewModel.setDarkModeSetting(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. ACTIVITY & USAGE STATS CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "ACTIVITY & USAGE STATS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = RustPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Saved Articles Count
                ProfileInfoRow(
                    icon = Icons.Default.Bookmark,
                    label = "Saved News Articles",
                    value = if (savedArticleCount == 1) "1 article saved" else "$savedArticleCount articles saved"
                )

                HorizontalDivider(
                    color = CardBorder,
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                // AI Chat History Threads
                ProfileInfoRow(
                    icon = Icons.Default.ChatBubble,
                    label = "AI Chat History",
                    value = if (savedConversations.size == 1) "1 active chat thread" else "${savedConversations.size} chat threads saved"
                )

                HorizontalDivider(
                    color = CardBorder,
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                // Active AI Engine
                ProfileInfoRow(
                    icon = Icons.Default.SmartToy,
                    label = "AI Chatbot Engine",
                    value = "Google LiteRT-LM (On-Device)"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. SECURITY & PRIVACY CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "SECURITY & ON-DEVICE PRIVACY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = RustPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Privacy Guarantee
                ProfileInfoRow(
                    icon = Icons.Default.Security,
                    label = "Data Privacy",
                    value = "100% On-Device • Zero Cloud Logging"
                )

                HorizontalDivider(
                    color = CardBorder,
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                // Storage Mechanism
                ProfileInfoRow(
                    icon = Icons.Default.Storage,
                    label = "Local Data Storage",
                    value = "Encrypted Android DataStore"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. ABOUT APP CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "ABOUT NEWSAIBUDDY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = RustPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // App Version
                ProfileInfoRow(
                    icon = Icons.Default.Info,
                    label = "Application Version",
                    value = "v${BuildConfig.VERSION_NAME}"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Clear Saved Articles Action
        if (savedArticleCount > 0) {
            OutlinedButton(
                onClick = onClearSavedArticles,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, RustPrimary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RustPrimary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkRemove,
                    contentDescription = "Clear Saved",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Clear All Saved Articles",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sign Out Button
        Button(
            onClick = { authViewModel.signOut() },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDC2626)
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Sign Out",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sign Out",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Footer
        Text(
            text = "NewsAI Buddy • Always Informed, Always On-Device",
            fontSize = 12.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
    }
}

/**
 * Segmented control for choosing between System, Light, and Dark mode preferences.
 */
@Composable
private fun ThemeModeSelector(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    val modes = listOf(
        Triple("system", "System", Icons.Default.SettingsBrightness),
        Triple("light", "Light", Icons.Default.LightMode),
        Triple("dark", "Dark", Icons.Default.DarkMode)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RustSoftBg.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        modes.forEach { (modeKey, label, icon) ->
            val isSelected = selectedMode == modeKey
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) RustPrimary else Color.Transparent,
                label = "ThemeButtonBg"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else TextSecondary,
                label = "ThemeButtonFg"
            )

            Surface(
                onClick = { onModeSelected(modeKey) },
                shape = RoundedCornerShape(10.dp),
                color = containerColor,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = RustSoftBg,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = RustPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 15.sp,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
