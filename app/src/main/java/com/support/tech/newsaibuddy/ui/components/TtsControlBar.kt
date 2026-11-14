package com.support.tech.newsaibuddy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Animated floating bottom bar for Text-To-Speech playback control (Play, Pause, Resume, Stop, Speed).
 */
@Composable
fun TtsControlBar(
    ttsState: TtsState,
    onPlayPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = ttsState.status != TtsStatus.IDLE

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Surface(
            color = CardBackground,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, CardBorder),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Speech progress bar
                val progress = if (ttsState.totalChunks > 0) {
                    (ttsState.currentChunkIndex + 1).toFloat() / ttsState.totalChunks.toFloat()
                } else {
                    0f
                }

                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    color = RustPrimary,
                    trackColor = RustSoftBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Audio Icon & Title Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RustSoftBg,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (ttsState.status == TtsStatus.SPEAKING) {
                                    AnimatedSoundWaveIndicator()
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Audio Reading",
                                        tint = RustPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (ttsState.status == TtsStatus.SPEAKING) "READING ALOUD" else "READING PAUSED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = RustPrimary
                                )

                                if (ttsState.totalChunks > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${ttsState.currentChunkIndex + 1}/${ttsState.totalChunks}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = ttsState.currentTitle ?: "Reading content...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Controls: Speed Selector, Play/Pause, Stop
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Speed dropdown button
                        var showSpeedMenu by remember { mutableStateOf(false) }

                        Box {
                            Surface(
                                onClick = { showSpeedMenu = true },
                                shape = RoundedCornerShape(12.dp),
                                color = RustSoftBg,
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Speed",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${ttsState.speechRate}x",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false },
                                modifier = Modifier.background(CardBackground)
                            ) {
                                val rates = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                                rates.forEach { rate ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${rate}x",
                                                fontWeight = if (ttsState.speechRate == rate) FontWeight.Bold else FontWeight.Normal,
                                                color = if (ttsState.speechRate == rate) RustPrimary else TextPrimary
                                            )
                                        },
                                        onClick = {
                                            onSpeedSelected(rate)
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Play/Pause button
                        Surface(
                            onClick = onPlayPauseClick,
                            shape = CircleShape,
                            color = RustPrimary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (ttsState.status == TtsStatus.SPEAKING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (ttsState.status == TtsStatus.SPEAKING) "Pause" else "Play/Resume",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Stop/Close button
                        IconButton(
                            onClick = onStopClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stop TTS",
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

/**
 * Animated sound wave equalizer bars indicating active speech playback.
 */
@Composable
private fun AnimatedSoundWaveIndicator() {
    val transition = rememberInfiniteTransition(label = "SoundWave")

    val bar1Height by transition.animateFloat(
        initialValue = 6f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val bar2Height by transition.animateFloat(
        initialValue = 16f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3Height by transition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar1Height.dp)
                .clip(CircleShape)
                .background(RustPrimary)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar2Height.dp)
                .clip(CircleShape)
                .background(RustPrimary)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar3Height.dp)
                .clip(CircleShape)
                .background(RustPrimary)
        )
    }
}
