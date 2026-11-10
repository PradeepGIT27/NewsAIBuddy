package com.support.tech.newsaibuddy.ui.screens.chatbot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender
import com.support.tech.newsaibuddy.data.litertlm.DownloadState
import com.support.tech.newsaibuddy.data.litertlm.LiteRtModel
import com.support.tech.newsaibuddy.ui.theme.BotAvatarBg
import com.support.tech.newsaibuddy.ui.theme.BotBadgeBackground
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.ScreenBackground
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.theme.UserBubbleBackground
import com.support.tech.newsaibuddy.ui.theme.UserBubbleBorder
import com.support.tech.newsaibuddy.ui.viewmodel.chatbot.ChatBotViewModel
import com.support.tech.newsaibuddy.ui.viewmodel.chatbot.ModelStatus

/**
 * ChatBot Screen powered by Google LiteRT-LM (On-Device LLM):
 * - Top header with rounded back button, title, on-device status badge & bot avatar
 * - "NEW CONVERSATION" divider
 * - On-Device model management card when model setup or download is required
 * - Distinct styled chat bubbles for User (warm cream) and Bot (white + avatar icon) with streaming support
 * - "A PLACE TO START" prompt suggestion cards
 * - Rounded input bar with rust upward-arrow send button & on-device AI status
 */
@Composable
fun ChatBotScreen(
    navController: NavController,
    chatViewModel: ChatBotViewModel = hiltViewModel()
) {
    val chatUiState by chatViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(chatUiState.messages.size) {
        if (chatUiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatUiState.messages.size - 1)
        }
    }

    val isModelReady = chatUiState.modelStatus is ModelStatus.Ready

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            ChatBotTopBar(
                modelStatus = chatUiState.modelStatus,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            ChatBotInputSection(
                isReady = isModelReady,
                isGenerating = chatUiState.isGenerating,
                onSendMessage = { text ->
                    chatViewModel.sendMessage(text)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // "NEW CONVERSATION" Divider
            NewConversationDivider()

            // Chat messages list
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // If model needs setup or downloading, show the on-device model card first
                if (chatUiState.modelStatus !is ModelStatus.Ready) {
                    item {
                        OnDeviceModelCard(
                            modelStatus = chatUiState.modelStatus,
                            downloadState = chatUiState.downloadState,
                            onDownloadModel = { model ->
                                chatViewModel.startDownload(model)
                            },
                            onRefresh = {
                                chatViewModel.checkAndInitLocalModel()
                            }
                        )
                    }
                }

                items(chatUiState.messages, key = { it.id }) { message ->
                    ChatBubbleItem(message = message)
                }

                // Starter Prompts section ("A PLACE TO START")
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    PlaceToStartSection(
                        enabled = isModelReady && !chatUiState.isGenerating,
                        onPromptClick = { prompt ->
                            chatViewModel.sendMessage(prompt)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Top App Bar with title, subtitle, on-device status badge & robot icon.
 */
@Composable
private fun ChatBotTopBar(
    modelStatus: ModelStatus,
    onBackClick: () -> Unit
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded Back Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardBackground,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .size(42.dp)
                    .clickable(onClick = onBackClick)
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

            // Title & Subtitle Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Chat Bot",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ModelStatusBadge(modelStatus = modelStatus)
                }
                Text(
                    text = "On-Device AI • Google LiteRT-LM",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Robot badge icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BotBadgeBackground,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = "AI Companion",
                        tint = RustPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact pill badge indicating the on-device model status.
 */
@Composable
private fun ModelStatusBadge(modelStatus: ModelStatus) {
    val (text, bgColor, textColor) = when (modelStatus) {
        is ModelStatus.Ready -> Triple("Offline (${modelStatus.backend})", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        is ModelStatus.Initializing -> Triple("Loading...", Color(0xFFFFF3E0), Color(0xFFE65100))
        is ModelStatus.NotDownloaded -> Triple("Model Required", Color(0xFFF3F4F6), Color(0xFF4B5563))
        is ModelStatus.Checking -> Triple("Checking...", Color(0xFFF3F4F6), Color(0xFF4B5563))
        is ModelStatus.Error -> Triple("Setup Issue", Color(0xFFFFEBEE), Color(0xFFC62828))
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

/**
 * Setup and download card for On-Device LiteRT-LM models.
 */
@Composable
private fun OnDeviceModelCard(
    modelStatus: ModelStatus,
    downloadState: DownloadState,
    onDownloadModel: (LiteRtModel) -> Unit,
    onRefresh: () -> Unit
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
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3F4F6),
                    modifier = Modifier.clickable(onClick = onRefresh)
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

            // Download State Progress
            if (downloadState is DownloadState.Downloading) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Downloading model weights...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = RustPrimary
                        )
                        Text(
                            text = "${downloadState.progressPercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RustPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { downloadState.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = RustPrimary,
                        trackColor = Color(0xFFE5E7EB)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${downloadState.bytesDownloaded / 1_000_000} MB / ${downloadState.totalBytes / 1_000_000} MB",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            } else if (modelStatus is ModelStatus.Initializing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = RustPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = modelStatus.info,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            } else {
                // Model Download Options
                Text(
                    text = "AVAILABLE MODELS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LiteRtModel.ALL_MODELS.forEach { model ->
                    ModelOptionRow(
                        model = model,
                        onDownload = { onDownloadModel(model) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ADB Push manual option toggle
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
                Text(
                    text = "Error: ${modelStatus.error}",
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626)
                )
            }
        }
    }
}

@Composable
private fun ModelOptionRow(
    model: LiteRtModel,
    onDownload: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ScreenBackground,
        border = BorderStroke(1.dp, CardBorder),
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
                    text = "${model.displayName} (${model.formattedSize})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = model.description,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onDownload,
                colors = ButtonDefaults.buttonColors(containerColor = RustPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Download",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Get",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Centered "NEW CONVERSATION" divider line.
 */
@Composable
private fun NewConversationDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = CardBorder,
            thickness = 1.dp
        )
        Text(
            text = "NEW CONVERSATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = CardBorder,
            thickness = 1.dp
        )
    }
}

/**
 * Message Bubble matching Figma design with streaming typing support.
 */
@Composable
fun ChatBubbleItem(message: ChatBotMessage) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when (message.participant) {
            ChatBotMessageSender.USER -> {
                // USER MESSAGE (Right aligned, warm cream container)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = UserBubbleBackground),
                        border = BorderStroke(1.dp, UserBubbleBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth(0.82f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "YOU",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RustPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            ChatBotMessageSender.MODEL -> {
                // BOT MESSAGE (Left aligned, white container with robot avatar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Small Bot Avatar on the left
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .background(BotAvatarBg, shape = RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = "Bot Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Card(
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CHAT BOT (OFFLINE)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                if (message.isPending) {
                                    CircularProgressIndicator(
                                        color = RustPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.text.ifEmpty { "Thinking..." },
                                fontSize = 15.sp,
                                color = if (message.text.isEmpty()) TextMuted else TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            ChatBotMessageSender.ERROR -> {
                // ERROR MESSAGE
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "NOTICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            color = Color(0xFF991B1B),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * "A PLACE TO START" prompt suggestions matching Figma design.
 */
@Composable
private fun PlaceToStartSection(
    enabled: Boolean,
    onPromptClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = "A PLACE TO START",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        PromptCard(
            text = "Help me understand a headline",
            enabled = enabled,
            onClick = { onPromptClick("Help me understand a headline") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PromptCard(
            text = "Explain a business term",
            enabled = enabled,
            onClick = { onPromptClick("Explain a business term") }
        )
    }
}

@Composable
private fun PromptCard(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) TextPrimary else TextMuted
            )

            Icon(
                imageVector = Icons.Default.ArrowOutward,
                contentDescription = null,
                tint = if (enabled) RustPrimary else TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Bottom Input Bar with Upward Arrow Send Button & Disclaimer.
 */
@Composable
private fun ChatBotInputSection(
    isReady: Boolean,
    isGenerating: Boolean,
    onSendMessage: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    val canSend = isReady && !isGenerating && text.isNotBlank()

    Surface(
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Input Field
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            text = if (isReady) "Ask on-device AI anything..." else "Setup or download model to start...",
                            fontSize = 15.sp,
                            color = TextMuted
                        )
                    },
                    enabled = isReady && !isGenerating,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (canSend) {
                                onSendMessage(text.trim())
                                text = ""
                            }
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RustPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Upward Arrow Send Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (canSend) RustPrimary else RustPrimary.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(54.dp)
                        .clickable(enabled = canSend) {
                            if (canSend) {
                                onSendMessage(text.trim())
                                text = ""
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.ArrowUpward,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Disclaimer Text
            Text(
                text = "100% On-Device AI • No data leaves your phone • Verify important info",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
