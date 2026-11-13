package com.support.tech.newsaibuddy.ui.screens.login

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.support.tech.newsaibuddy.ui.theme.NewsAIBuddyTheme
import com.support.tech.newsaibuddy.ui.theme.RustDark
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "LoginScreen"

// ⚠️ IMPORTANT: Replace this with YOUR Web Client ID from the Google Cloud Console
// (APIs & Services → Credentials → OAuth 2.0 Client IDs → Web application type)
private const val WEB_CLIENT_ID = "430048298657-t1h53k0fg09qquekqra4c8ndpq8tkg0t.apps.googleusercontent.com"

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearError()
        }
    }

    LoginScreenContent(
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        onGoogleSignInClick = {
            authViewModel.setLoading(true)
            coroutineScope.launch {
                try {
                    val credentialManager = CredentialManager.create(context)
                    val nonce = java.util.UUID.randomUUID().toString()

                    // Try GetSignInWithGoogleOption first (recommended for explicit sign-in button clicks)
                    val result = try {
                        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                            serverClientId = WEB_CLIENT_ID
                        )
                            .setNonce(nonce)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(signInWithGoogleOption)
                            .build()

                        credentialManager.getCredential(
                            context = context as Activity,
                            request = request
                        )
                    } catch (_: Exception) {
                        // Fallback to GetGoogleIdOption if GetSignInWithGoogleOption fails or returns NoCredentialException
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(WEB_CLIENT_ID)
                            .setAutoSelectEnabled(false)
                            .setNonce(nonce)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        credentialManager.getCredential(
                            context = context as Activity,
                            request = request
                        )
                    }

                    val credential = result.credential
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                        authViewModel.onSignInSuccess(
                            displayName = googleIdTokenCredential.displayName ?: "User",
                            email = googleIdTokenCredential.id,
                            photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: "",
                            userId = googleIdTokenCredential.idToken
                        )
                    } else {
                        Log.e(TAG, "Unexpected credential type returned: ${credential.type}")
                        authViewModel.onSignInError("Unexpected credential format received.")
                    }
                } catch (_: GetCredentialCancellationException) {
                    authViewModel.setLoading(false)
                    Log.d(TAG, "Sign-in cancelled by user")
                } catch (e: NoCredentialException) {
                    authViewModel.setLoading(false)
                    Log.e(TAG, "No credential available on device", e)
                    authViewModel.onSignInError(
                        "Google Sign-In failed: No credential found. " +
                        "Make sure a Google Account is logged into device settings and your debug SHA-1 is registered in Google Cloud Console."
                    )
                } catch (e: Exception) {
                    authViewModel.setLoading(false)
                    Log.e(TAG, "Sign-in failed [${e.javaClass.simpleName}]: ${e.message}", e)
                    authViewModel.onSignInError("Sign-in failed: ${e.message ?: "Check SHA-1 & Google account setup"}")
                }
            }
        },
        onGuestSignInClick = {
            authViewModel.signInAsGuest()
        }
    )
}

@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onGoogleSignInClick: () -> Unit = {},
    onGuestSignInClick: () -> Unit = {}
) {
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    var animateHeader by remember { mutableStateOf(isPreview) }
    var animateFeatures by remember { mutableStateOf(isPreview) }
    var animateCta by remember { mutableStateOf(isPreview) }

    LaunchedEffect(isPreview) {
        if (!isPreview) {
            animateHeader = true
            delay(120)
            animateFeatures = true
            delay(120)
            animateCta = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Ambient subtle background glow
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .align(Alignment.TopCenter)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            RustPrimary.copy(alpha = 0.15f),
                            RustPrimary.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height * 0.35f),
                        radius = size.width * 0.75f
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // AI Pill Tag & Hero Logo
                    AnimatedVisibility(
                        visible = animateHeader,
                        enter = fadeIn(animationSpec = tween(400)) +
                                slideInVertically(
                                    initialOffsetY = { -40 },
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Pill Chip
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = RustPrimary.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, RustPrimary.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = RustPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI-POWERED NEWS COMPANION",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp,
                                        color = RustPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Hero App Logo with layered AI spark badge
                            Box(contentAlignment = Alignment.TopEnd) {
                                Surface(
                                    shape = RoundedCornerShape(26.dp),
                                    color = Color.Transparent,
                                    shadowElevation = 14.dp,
                                    modifier = Modifier
                                        .size(92.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    RustPrimary,
                                                    RustDark
                                                )
                                            ),
                                            shape = RoundedCornerShape(26.dp)
                                        )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Article,
                                            contentDescription = "App Icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }

                                // Sparkling overlay badge
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEF3C7))
                                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = RustPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            // App Brand Name
                            Text(
                                text = "NewsAI Buddy",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Serif,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Subtitle
                            Text(
                                text = "Your intelligent, personal news workspace",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Stay ahead with real-time headlines, instant summaries, and conversational AI insights.",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Feature Cards List
                    AnimatedVisibility(
                        visible = animateFeatures,
                        enter = fadeIn(animationSpec = tween(400)) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            FeatureCard(
                                icon = Icons.Default.Newspaper,
                                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                                iconTint = RustPrimary,
                                title = "Top Headlines by Category",
                                description = "Explore world, tech, business & sports news in real time."
                            )

                            FeatureCard(
                                icon = Icons.Default.Bookmark,
                                iconBg = Color(0xFFFEF3C7),
                                iconTint = Color(0xFFD97706),
                                title = "Save & Revisit Stories",
                                description = "Bookmark your favorite news items to read offline anytime."
                            )

                            FeatureCard(
                                icon = Icons.Default.AutoAwesome,
                                iconBg = Color(0xFFEDE9FE),
                                iconTint = Color(0xFF7C3AED),
                                title = "Chat & Summarize with AI",
                                description = "Ask questions, get key takeaways, and chat with your AI buddy."
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bottom Call To Action & Sign In Card
                AnimatedVisibility(
                    visible = animateCta,
                    enter = fadeIn(animationSpec = tween(400)) +
                            slideInVertically(
                                initialOffsetY = { 50 },
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                            )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Google Sign-In Card
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable(enabled = !isLoading, onClick = onGoogleSignInClick)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = RustPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Signing in...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                } else {
                                    GoogleGIcon(modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = "Continue with Google",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Guest Sign-In Option
                        TextButton(
                            onClick = onGuestSignInClick,
                            enabled = !isLoading
                        ) {
                            Text(
                                text = "Continue as Guest",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RustPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Security assurance
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Secure Sign-in • No password required",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "By continuing, you agree to our Terms & Privacy Policy",
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

/**
 * Clean, vector-drawn Google "G" brand logo.
 */
@Composable
private fun GoogleGIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stroke = width * 0.20f
        val rect = Rect(stroke / 2, stroke / 2, width - stroke / 2, height - stroke / 2)
        val style = Stroke(width = stroke, cap = StrokeCap.Butt)

        // Top arc (Red #EA4335)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 195f,
            sweepAngle = 105f,
            useCenter = false,
            style = style,
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Left arc (Yellow #FBBC05)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 125f,
            sweepAngle = 70f,
            useCenter = false,
            style = style,
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Bottom arc (Green #34A853)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 35f,
            sweepAngle = 90f,
            useCenter = false,
            style = style,
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Right arc (Blue #4285F4)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -30f,
            sweepAngle = 65f,
            useCenter = false,
            style = style,
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Blue center line
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(width / 2f, height / 2f),
            end = Offset(width - stroke / 2f, height / 2f),
            strokeWidth = stroke
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun LoginScreenPreview() {
    NewsAIBuddyTheme {
        LoginScreenContent(isLoading = false)
    }
}
