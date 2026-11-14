package com.support.tech.newsaibuddy.ui.screens.chatbot.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.support.tech.newsaibuddy.data.litertlm.DownloadState
import com.support.tech.newsaibuddy.data.litertlm.LiteRtModel
import com.support.tech.newsaibuddy.ui.theme.BotBadgeBackground
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.ScreenBackground
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.theme.UserBubbleBorder
import com.support.tech.newsaibuddy.ui.viewmodel.chatbot.ModelStatus

@Composable
fun ChatBotTopBar(
    modelStatus: ModelStatus,
    activeModel: LiteRtModel?,
    downloadState: DownloadState,
    conversationCount: Int,
    onBackClick: () -> Unit,
    onOpenModelSelector: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewChat: () -> Unit
) {
    Surface(
        color = ScreenBackground,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBackClick,
                shape = RoundedCornerShape(12.dp),
                color = CardBackground,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Chat Bot",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ModelStatusBadge(modelStatus = modelStatus, downloadState = downloadState)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable { onOpenModelSelector() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = RustPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (downloadState) {
                            is DownloadState.Downloading -> "Downloading (${downloadState.progressPercent}%)..."
                            else -> activeModel?.displayName ?: "Choose Model"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RustPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Model",
                        tint = RustPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onOpenHistory,
                    shape = RoundedCornerShape(12.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        if (conversationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(8.dp)
                                    .background(RustPrimary, CircleShape)
                            )
                        }
                    }
                }

                Surface(
                    onClick = onNewChat,
                    shape = RoundedCornerShape(12.dp),
                    color = BotBadgeBackground,
                    border = BorderStroke(1.dp, UserBubbleBorder),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = RustPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopDownloadProgressBanner(
    downloadState: DownloadState.Downloading,
    onCancel: () -> Unit
) {
    Surface(
        color = Color(0xFFFFF7ED),
        border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = RustPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Downloading ${downloadState.model.displayName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${downloadState.bytesDownloaded / 1_000_000} MB / ${downloadState.totalBytes / 1_000_000} MB (${downloadState.progressPercent}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RustPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Download",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { downloadState.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = RustPrimary,
                trackColor = Color(0xFFFED7AA)
            )
        }
    }
}

@Composable
fun ModelStatusBadge(
    modelStatus: ModelStatus,
    downloadState: DownloadState
) {
    val (text, bgColor, textColor) = when {
        downloadState is DownloadState.Downloading -> Triple("Downloading ${downloadState.progressPercent}%", Color(0xFFFFF3E0), Color(0xFFE65100))
        modelStatus is ModelStatus.Ready -> Triple("Offline (${modelStatus.backend})", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        modelStatus is ModelStatus.Initializing -> Triple("Loading...", Color(0xFFFFF3E0), Color(0xFFE65100))
        modelStatus is ModelStatus.NotDownloaded -> Triple("Model Required", Color(0xFFF3F4F6), Color(0xFF4B5563))
        modelStatus is ModelStatus.Checking -> Triple("Checking...", Color(0xFFF3F4F6), Color(0xFF4B5563))
        modelStatus is ModelStatus.Error -> Triple("Setup Issue", Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Triple("Offline", Color(0xFFF3F4F6), Color(0xFF4B5563))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
