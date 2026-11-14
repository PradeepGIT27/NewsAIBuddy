package com.support.tech.newsaibuddy.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.support.tech.newsaibuddy.domain.model.Article
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.RustSoftBg
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.utils.TtsState
import com.support.tech.newsaibuddy.utils.TtsStatus

/**
 * Data holder for Reader Mode content (either News Article or ChatBot Response).
 */
data class ReaderContent(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val sourceName: String? = null,
    val author: String? = null,
    val publishedAt: String? = null,
    val imageUrl: String? = null,
    val body: String,
    val isChatBotResponse: Boolean = false
)

/**
 * Distraction-free Reading Mode Modal Sheet with customizable font size (A- / A+),
 * font family toggle (Serif vs. Sans-Serif), copy action, and integrated TTS Play/Pause/Resume audio controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderModeSheet(
    content: ReaderContent?,
    ttsState: TtsState,
    onDismiss: () -> Unit,
    onPlayPauseTts: (id: String, text: String, title: String) -> Unit,
    onStopTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (content == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var useSerifFont by remember { mutableStateOf(!content.isChatBotResponse) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Header Bar: Title, Font controls, Copy, Close
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Reader Mode Tag & Source Name
                    Column {
                        Text(
                            text = if (content.isChatBotResponse) "AI ASSISTANT READER" else "NEWS READER MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                            color = RustPrimary
                        )

                        Text(
                            text = content.sourceName ?: if (content.isChatBotResponse) "NewsAIBuddy AI" else "Top Headline",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Right Actions: Font Size Controls, Font Toggle, Copy, Close
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Font Size Decrease (A-)
                        Surface(
                            onClick = { fontSizeSp = (fontSizeSp - 1f).coerceAtLeast(13f) },
                            shape = RoundedCornerShape(8.dp),
                            color = RustSoftBg,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("A-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        // Font Size Increase (A+)
                        Surface(
                            onClick = { fontSizeSp = (fontSizeSp + 1f).coerceAtMost(24f) },
                            shape = RoundedCornerShape(8.dp),
                            color = RustSoftBg,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("A+", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        // Font Serif/Sans Toggle
                        Surface(
                            onClick = { useSerifFont = !useSerifFont },
                            shape = RoundedCornerShape(8.dp),
                            color = if (useSerifFont) RustPrimary else RustSoftBg,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "F",
                                    fontSize = 13.sp,
                                    fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    color = if (useSerifFont) Color.White else TextPrimary
                                )
                            }
                        }

                        // Copy Text
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("${content.title}\n\n${content.body}"))
                                Toast.makeText(context, "Copied text to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Close Sheet
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Reader",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = CardBorder, thickness = 1.dp)
            }

            // Scrollable Main Article Content
            val scrollState = rememberScrollState()
            val fullSpeechText = "${content.title}. ${content.subtitle ?: ""} ${content.body}"

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // Headline / Title
                    Text(
                        text = content.title,
                        fontSize = (fontSizeSp + 6f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                        color = TextPrimary,
                        lineHeight = (fontSizeSp + 12f).sp
                    )

                    // Subtitle / Description if present
                    if (!content.subtitle.isNullOrBlank() && content.subtitle != content.body) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = content.subtitle,
                            fontSize = (fontSizeSp + 1f).sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                            color = RustPrimary,
                            lineHeight = (fontSizeSp + 6f).sp
                        )
                    }

                    // Metadata Row (Author, Published Date)
                    val metadata = listOfNotNull(content.author?.let { "By $it" }, content.publishedAt).joinToString(" • ")
                    if (metadata.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = metadata,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    // Article Hero Image if present
                    if (!content.imageUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ShimmerAsyncImage(
                            model = content.imageUrl,
                            contentDescription = content.title,
                            contentScale = ContentScale.Crop,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Article Main Body Text
                    Text(
                        text = content.body,
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                        color = TextPrimary,
                        lineHeight = (fontSizeSp * 1.55f).sp
                    )

                    Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom TTS player bar
                }

                // Floating Reader Mode TTS Listen Pill at bottom
                val isThisSpeaking = ttsState.currentId == content.id && ttsState.status == TtsStatus.SPEAKING
                val isThisPaused = ttsState.currentId == content.id && ttsState.status == TtsStatus.PAUSED

                Surface(
                    color = CardBackground,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause/Resume Button
                        Surface(
                            onClick = { onPlayPauseTts(content.id, fullSpeechText, content.title) },
                            shape = CircleShape,
                            color = RustPrimary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isThisSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isThisSpeaking) "Pause" else "Listen / Resume",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = when {
                                    isThisSpeaking -> "Reading aloud..."
                                    isThisPaused -> "Speech paused"
                                    else -> "Listen to story"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Text-to-Speech audio reader",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        if (isThisSpeaking || isThisPaused) {
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = onStopTts,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Stop",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
