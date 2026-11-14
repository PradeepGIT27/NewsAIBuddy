package com.support.tech.newsaibuddy.ui.screens.chatbot.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.support.tech.newsaibuddy.data.litertlm.DownloadState
import com.support.tech.newsaibuddy.data.litertlm.LiteRtModel
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.ScreenBackground
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.viewmodel.chatbot.ModelStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectionBottomSheet(
    availableModels: List<LiteRtModel>,
    downloadedModelIds: Set<String>,
    activeModel: LiteRtModel?,
    modelStatus: ModelStatus,
    downloadState: DownloadState,
    onSelectModel: (LiteRtModel) -> Unit,
    onForceCpuModel: (LiteRtModel) -> Unit,
    onCancelDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = RustPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Select On-Device AI Model",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Runs 100% locally on your phone",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            availableModels.forEach { model ->
                val isDownloaded = downloadedModelIds.contains(model.id)
                val isActive = activeModel?.id == model.id && modelStatus is ModelStatus.Ready
                val isInitializing = modelStatus is ModelStatus.Initializing && modelStatus.model?.id == model.id
                val activeDownloadingState = downloadState as? DownloadState.Downloading
                val isThisDownloading = activeDownloadingState?.model?.id == model.id

                ModelSelectionCardItem(
                    model = model,
                    isDownloaded = isDownloaded,
                    isActive = isActive,
                    isInitializing = isInitializing,
                    downloadState = if (isThisDownloading) activeDownloadingState else null,
                    onSelect = { onSelectModel(model) },
                    onForceCpu = { onForceCpuModel(model) },
                    onCancelDownload = onCancelDownload
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ModelSelectionCardItem(
    model: LiteRtModel,
    isDownloaded: Boolean,
    isActive: Boolean,
    isInitializing: Boolean,
    downloadState: DownloadState.Downloading?,
    onSelect: () -> Unit,
    onForceCpu: () -> Unit,
    onCancelDownload: () -> Unit
) {
    val borderColor = if (isActive) RustPrimary else CardBorder
    val containerColor = if (isActive) Color(0xFFFFFBF5) else ScreenBackground

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isActive) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = model.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${model.formattedSize})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = model.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                when {
                    downloadState != null -> {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${downloadState.progressPercent}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RustPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    tint = TextMuted,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onCancelDownload() }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { downloadState.progressPercent / 100f },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = RustPrimary,
                                trackColor = Color(0xFFFED7AA)
                            )
                        }
                    }

                    isActive -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    isInitializing -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = RustPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Loading...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = RustPrimary
                            )
                        }
                    }

                    isDownloaded -> {
                        Button(
                            onClick = onSelect,
                            colors = ButtonDefaults.buttonColors(containerColor = RustPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Select",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Select",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    else -> {
                        Button(
                            onClick = onSelect,
                            colors = ButtonDefaults.buttonColors(containerColor = RustPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Get Download",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Get Download",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if (isDownloaded && !isActive && !isInitializing && downloadState == null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Load with CPU (Safe Mode)",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onForceCpu() }
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OnDeviceModelCard(
    modelStatus: ModelStatus,
    downloadState: DownloadState,
    downloadedModelIds: Set<String>,
    activeModel: LiteRtModel?,
    onSelectModel: (LiteRtModel) -> Unit,
    onLoadWithCpu: (LiteRtModel) -> Unit,
    onCancelDownload: () -> Unit,
    onRefresh: () -> Unit,
    onOpenModelSelector: () -> Unit
) {
    var showAdbHint by rememberSaveable { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = RustPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "On-Device LLM (LiteRT-LM)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3F4F6)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Check",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Runs 100% offline on your device's GPU/CPU. Zero API keys, zero quota limits, complete privacy.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (modelStatus is ModelStatus.Initializing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        color = RustPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = modelStatus.info,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Please wait a few seconds...",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AVAILABLE MODELS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = TextSecondary
                    )

                    Text(
                        text = "View All →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RustPrimary,
                        modifier = Modifier.clickable { onOpenModelSelector() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LiteRtModel.ALL_MODELS.forEach { model ->
                    val isDownloaded = downloadedModelIds.contains(model.id)
                    val isActive = activeModel?.id == model.id && modelStatus is ModelStatus.Ready
                    val currentDownloadingState = downloadState as? DownloadState.Downloading
                    val isThisDownloading = currentDownloadingState?.model?.id == model.id

                    ModelOptionRow(
                        model = model,
                        isDownloaded = isDownloaded,
                        isActive = isActive,
                        downloadState = if (isThisDownloading) currentDownloadingState else null,
                        onSelect = { onSelectModel(model) },
                        onCancelDownload = onCancelDownload
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = if (showAdbHint) "Hide adb command instructions ▲" else "Or push model file via ADB ▼",
                    fontSize = 12.sp,
                    color = RustPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { showAdbHint = !showAdbHint }
                        .padding(vertical = 4.dp)
                )

                if (showAdbHint) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "1. adb push <model.litertlm> /data/local/tmp/model.litertlm\n2. adb shell run-as com.support.tech.newsaibuddy cp /data/local/tmp/model.litertlm files/models/\n3. Tap 'Check' above.",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            if (modelStatus is ModelStatus.Error) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Initialization Error: ${modelStatus.error}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFDC2626)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (modelStatus.failedModel != null) {
                                OutlinedButton(
                                    onClick = { onLoadWithCpu(modelStatus.failedModel) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Memory,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry (CPU Mode)", fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = onOpenModelSelector,
                                colors = ButtonDefaults.buttonColors(containerColor = RustPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Select Other Model", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelOptionRow(
    model: LiteRtModel,
    isDownloaded: Boolean,
    isActive: Boolean,
    downloadState: DownloadState.Downloading?,
    onSelect: () -> Unit,
    onCancelDownload: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) Color(0xFFFFFBF5) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isActive) RustPrimary else CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${model.formattedSize} • ${model.description}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            when {
                downloadState != null -> {
                    Text(
                        text = "${downloadState.progressPercent}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RustPrimary
                    )
                }

                isActive -> {
                    Text(
                        text = "Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                isDownloaded -> {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = RustPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Select", fontSize = 11.sp)
                    }
                }

                else -> {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = RustPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Download", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
