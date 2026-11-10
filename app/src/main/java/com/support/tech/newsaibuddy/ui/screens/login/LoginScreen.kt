package com.support.tech.newsaibuddy.ui.screens.login

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.RustSoftBg
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.launch

private const val TAG = "LoginScreen"

// ⚠️  IMPORTANT: Replace this with YOUR Web Client ID from the Google Cloud Console
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            RustSoftBg,
                            Color.White,
                            RustSoftBg.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(padding)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.15f))

                // App Icon
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { -40 }
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = RustPrimary,
                        shadowElevation = 12.dp,
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Article,
                                contentDescription = "App Icon",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Brand Title
                Text(
                    text = "NewsAI Buddy",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your intelligent news companion",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tagline
                Text(
                    text = "Stay informed with AI-curated stories,\nsave what matters, chat with your news buddy.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.weight(0.2f))

                // Features preview
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FeatureRow(emoji = "📰", text = "Browse top headlines by category")
                    FeatureRow(emoji = "🔖", text = "Save & revisit stories anytime")
                    FeatureRow(emoji = "🤖", text = "Chat with AI about the news")
                }

                Spacer(modifier = Modifier.weight(0.25f))

                // Google Sign-In Button
                OutlinedButton(
                    onClick = {
                        authViewModel.setLoading(true)
                        coroutineScope.launch {
                            try {
                                val credentialManager = CredentialManager.create(context)
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(WEB_CLIENT_ID)
                                    .setAutoSelectEnabled(false)
                                    .setNonce(java.util.UUID.randomUUID().toString())
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                val result = credentialManager.getCredential(
                                    context = context as Activity,
                                    request = request
                                )

                                val credential = result.credential
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                                authViewModel.onSignInSuccess(
                                    displayName = googleIdTokenCredential.displayName ?: "User",
                                    email = googleIdTokenCredential.id,
                                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: "",
                                    userId = googleIdTokenCredential.idToken
                                )
                            } catch (_: GetCredentialCancellationException) {
                                authViewModel.setLoading(false)
                                Log.d(TAG, "Sign-in cancelled by user")
                            } catch (e: Exception) {
                                authViewModel.setLoading(false)
                                Log.e(TAG, "Sign-in failed [${e.javaClass.simpleName}]: ${e.message}", e)
                                authViewModel.onSignInError("Sign-in failed (${e.javaClass.simpleName}): ${e.message}")
                            }
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFDADADA)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = RustPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        // Google "G" icon with text
                        GoogleGIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "By continuing, you agree to our Terms & Privacy Policy",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(0.1f))
            }
        }
    }
}

@Composable
private fun FeatureRow(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Simple Google "G" icon drawn with colored Text as a fallback.
 * In production, use a proper Google icon drawable.
 */
@Composable
private fun GoogleGIcon(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "G",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4285F4)
            )
        }
    }
}
