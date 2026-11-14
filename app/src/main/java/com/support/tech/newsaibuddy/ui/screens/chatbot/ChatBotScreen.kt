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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender
import com.support.tech.newsaibuddy.data.local.ChatConversation
import com.support.tech.newsaibuddy.data.litertlm.DownloadState
import com.support.tech.newsaibuddy.data.litertlm.LiteRtModel
import androidx.compose.runtime.remember
import com.support.tech.newsaibuddy.ui.components.ReaderContent
import com.support.tech.newsaibuddy.ui.components.ReaderModeSheet
import com.support.tech.newsaibuddy.ui.components.TtsControlBar
import com.support.tech.newsaibuddy.utils.TtsStatus
import com.support.tech.newsaibuddy.ui.theme.BotAvatarBg
import com.support.tech.newsaibuddy.ui.theme.BotBadgeBackground
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.RustSoftBg
import com.support.tech.newsaibuddy.ui.theme.ScreenBackground
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.theme.UserBubbleBackground
import com.support.tech.newsaibuddy.ui.theme.UserBubbleBorder
import com.support.tech.newsaibuddy.ui.viewmodel.chatbot.ChatBotViewModel
import com.support.tech.newsaibuddy.ui.viewmodel.chatbot.ModelStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ChatBot Screen powered by Google LiteRT-LM (On-Device LLM):
 * - Top header with back button, history drawer toggle, "+ New Chat" button & model picker selector
 * - Offline local conversation history with drawer sheet & auto-saving capability
 * - Sticky Top Download Progress Banner when downloading in background
 * - Model Selection Modal BottomSheet allowing users to change or download models anytime
 * - "NEW CONVERSATION" divider
 * - On-Device model management card when setup or download is required
 * - Distinct styled chat bubbles for User and Bot with streaming support
 * - "A PLACE TO START" prompt suggestion cards
 * - Rounded input bar with rust upward-arrow send button & on-device AI status
 */
@Composable
fun ChatBotScreen(
    navController: NavController,
    chatViewModel: ChatBotViewModel = hiltViewModel()
) {
    val chatUiState by chatViewModel.uiState.collectAsState()
    val ttsState by chatViewModel.ttsState.collectAsState()
    val listState = rememberLazyListState()

    var activeReaderContent by remember { mutableStateOf<ReaderContent?>(null) }

    // Auto-scroll to latest message
    LaunchedEffect(chatUiState.messages.size) {
        if (chatUiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatUiState.messages.size - 1)
        }
    }

    val isModelReady = chatUiState.modelStatus is ModelStatus.Ready

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = ScreenBackground,
            topBar = {
                ChatBotTopBar(
                    modelStatus = chatUiState.modelStatus,
                    activeModel = chatUiState.activeModel,
                    downloadState = chatUiState.downloadState,
                    conversationCount = chatUiState.conversations.size,
                    onBackClick = { navController.popBackStack() },
                    onOpenModelSelector = { chatViewModel.toggleModelSelector(true) },
                    onOpenHistory = { chatViewModel.toggleHistorySheet(true) },
                    onNewChat = { chatViewModel.startNewConversation() }
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
                // Sticky Download Progress Banner at top of screen
                if (chatUiState.downloadState is DownloadState.Downloading) {
                    TopDownloadProgressBanner(
                        downloadState = chatUiState.downloadState as DownloadState.Downloading,
                        onCancel = { chatViewModel.cancelDownload() }
                    )
                }

                // "NEW CONVERSATION" Divider
                NewConversationDivider(
                    title = chatUiState.currentConversationTitle,
                    onNewChatClick = { chatViewModel.startNewConversation() }
                )

                // Chat messages list
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // If model needs setup or downloading, or has error, show the on-device model card first
                    if (chatUiState.modelStatus !is ModelStatus.Ready) {
                        item {
                            OnDeviceModelCard(
                                modelStatus = chatUiState.modelStatus,
                                downloadState = chatUiState.downloadState,
                                downloadedModelIds = chatUiState.downloadedModelIds,
                                activeModel = chatUiState.activeModel,
                                onSelectModel = { model ->
                                    chatViewModel.selectModel(model)
                                },
                                onLoadWithCpu = { model ->
                                    chatViewModel.loadModel(model, forceCpu = true)
                                },
                                onCancelDownload = {
                                    chatViewModel.cancelDownload()
                                },
                                onRefresh = {
                                    chatViewModel.checkAndInitLocalModel()
                                },
                                onOpenModelSelector = {
                                    chatViewModel.toggleModelSelector(true)
                                }
                            )
                        }
                    }

                    items(chatUiState.messages, key = { it.id }) { message ->
                        val isThisSpeaking = ttsState.currentId == message.id && ttsState.status == TtsStatus.SPEAKING
                        val isThisPaused = ttsState.currentId == message.id && ttsState.status == TtsStatus.PAUSED

                        ChatBubbleItem(
                            message = message,
                            isTtsSpeaking = isThisSpeaking,
                            isTtsPaused = isThisPaused,
                            onTtsClick = {
                                chatViewModel.playOrPauseTts(message.id, message.text)
                            },
                            onReaderModeClick = {
                                activeReaderContent = ReaderContent(
                                    id = message.id,
                                    title = "AI Companion Response",
                                    sourceName = "NewsAIBuddy AI",
                                    body = message.text,
                                    isChatBotResponse = true
                                )
                            }
                        )
                    }

                    // Starter Prompts section ("A PLACE TO START")
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        PlaceToStartSection(
                            prompts = chatUiState.starterPrompts,
                            enabled = isModelReady && !chatUiState.isGenerating,
                            onPromptClick = { prompt ->
                                chatViewModel.sendMessage(prompt)
                            }
                        )
                    }
                }
            }
        }

        // Floating Audio Playback Control Bar
        TtsControlBar(
            ttsState = ttsState,
            onPlayPauseClick = {
                if (ttsState.status == TtsStatus.SPEAKING) {
                    chatViewModel.pauseTts()
                } else {
                    chatViewModel.resumeTts()
                }
            },
            onStopClick = { chatViewModel.stopTts() },
            onSpeedSelected = { rate -> chatViewModel.setTtsSpeechRate(rate) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        // Reader Mode Modal Sheet for ChatBot Response
        if (activeReaderContent != null) {
            ReaderModeSheet(
                content = activeReaderContent,
                ttsState = ttsState,
                onDismiss = { activeReaderContent = null },
                onPlayPauseTts = { id, text, title ->
                    chatViewModel.playTts(id, text, title)
                },
                onStopTts = { chatViewModel.stopTts() }
            )
        }
    }

    // Model Selector Bottom Sheet
    if (chatUiState.showModelSelectorSheet) {
        ModelSelectionBottomSheet(
            availableModels = chatUiState.availableModels,
            downloadedModelIds = chatUiState.downloadedModelIds,
            activeModel = chatUiState.activeModel,
            modelStatus = chatUiState.modelStatus,
            downloadState = chatUiState.downloadState,
            onSelectModel = { model ->
                chatViewModel.selectModel(model)
            },
            onForceCpuModel = { model ->
                chatViewModel.loadModel(model, forceCpu = true)
            },
            onCancelDownload = {
                chatViewModel.cancelDownload()
            },
            onDismiss = {
                chatViewModel.toggleModelSelector(false)
            }
        )
    }

    // Offline History Modal Bottom Sheet
    if (chatUiState.showHistorySheet) {
        HistoryBottomSheet(
            conversations = chatUiState.conversations,
            currentConversationId = chatUiState.currentConversationId,
            searchQuery = chatUiState.historySearchQuery,
            onSearchQueryChange = { chatViewModel.updateHistorySearchQuery(it) },
            onSelectConversation = { conv ->
                chatViewModel.loadConversation(conv)
            },
            onStartNewChat = {
                chatViewModel.startNewConversation()
            },
            onDeleteConversation = { conv ->
                chatViewModel.setConversationToDelete(conv)
            },
            onClearAllClick = {
                chatViewModel.setShowClearAllDialog(true)
            },
            onDismiss = {
                chatViewModel.toggleHistorySheet(false)
            }
        )
    }

    // Single Conversation Delete Confirmation Dialog
    chatUiState.conversationToDelete?.let { convToDelete ->
        AlertDialog(
            onDismissRequest = { chatViewModel.setConversationToDelete(null) },
            title = {
                Text(
                    text = "Delete Conversation?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${convToDelete.title}\"? This conversation will be permanently removed from offline history.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { chatViewModel.deleteConversation(convToDelete.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatViewModel.setConversationToDelete(null) }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Clear All History Confirmation Dialog
    if (chatUiState.showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { chatViewModel.setShowClearAllDialog(false) },
            title = {
                Text(
                    text = "Clear All History?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "This will delete all ${chatUiState.conversations.size} saved conversations from local device storage.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { chatViewModel.clearAllHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatViewModel.setShowClearAllDialog(false) }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * Top App Bar with title, history icon, new chat button & active model selector chip.
 */
@Composable
private fun ChatBotTopBar(
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
            // Rounded Back Button
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

            // Title & Active Model Selector Column
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

                // Clickable Model Selector Chip
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

            // Action Buttons Row: History Drawer & "+ New" Chat
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // History Drawer Button with counter badge
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

                // "+ New Chat" Button
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

/**
 * Sticky Download Progress Banner at top of screen during background model downloading.
 */
@Composable
private fun TopDownloadProgressBanner(
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

/**
 * Compact pill badge indicating the on-device model status.
 */
@Composable
private fun ModelStatusBadge(
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

/**
 * Offline Conversation History Bottom Sheet displaying saved chats with search & delete actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryBottomSheet(
    conversations: List<ChatConversation>,
    currentConversationId: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectConversation: (ChatConversation) -> Unit,
    onStartNewChat: () -> Unit,
    onDeleteConversation: (ChatConversation) -> Unit,
    onClearAllClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredConversations = if (searchQuery.isBlank()) {
        conversations
    } else {
        conversations.filter { conv ->
            conv.title.contains(searchQuery, ignoreCase = true) ||
            conv.messages.any { msg -> msg.text.contains(searchQuery, ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .navigationBarsPadding()
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = RustPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Conversation History",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${conversations.size} offline chats saved",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (conversations.isNotEmpty()) {
                        IconButton(onClick = onClearAllClick) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Clear All History",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: "+ Start New Conversation" Button
            Button(
                onClick = {
                    onStartNewChat()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RustPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start New Conversation",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            if (conversations.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search chat history...", fontSize = 13.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ScreenBackground,
                        unfocusedContainerColor = ScreenBackground,
                        focusedBorderColor = RustPrimary,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Notice Banner about offline storage
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, UserBubbleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = RustPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "History is stored 100% locally on device. Clearing app cache or uninstalling will remove history.",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Conversations List or Empty State
            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching conversations found" else "No conversation history yet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Each chat is automatically saved locally.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conversation ->
                        val isSelected = conversation.id == currentConversationId
                        HistoryConversationItem(
                            conversation = conversation,
                            isSelected = isSelected,
                            onSelect = { onSelectConversation(conversation) },
                            onDelete = { onDeleteConversation(conversation) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Individual Conversation Item card inside the History sheet.
 */
@Composable
private fun HistoryConversationItem(
    conversation: ChatConversation,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (isSelected) RustPrimary else CardBorder
    val containerColor = if (isSelected) Color(0xFFFFFBF5) else ScreenBackground

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (isSelected) RustPrimary else Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = conversation.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatRelativeTime(conversation.updatedAt),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                val lastMessage = conversation.messages.lastOrNull()?.text ?: ""
                Text(
                    text = if (lastMessage.isNotBlank()) lastMessage else "${conversation.messages.size} messages",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Trash / Delete Icon Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Conversation",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        diff < 0 || seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "Yesterday"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

/**
 * Modal Bottom Sheet allowing user to choose, switch, or download on-device LLM models anytime.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelectionBottomSheet(
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
            // Header Row
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

            // List of Models
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

/**
 * Individual model item inside the Model Selection sheet.
 */
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

                // Action Button / Status Badge
                when {
                    downloadState != null -> {
                        // Currently Downloading this model
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
                        // Model downloaded: Show "Select" / "Switch" button
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
                        // Model not downloaded: Show "Get Download" button
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

            // CPU Fallback option if user wants forced CPU mode for stability
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

/**
 * Setup and download card for On-Device LiteRT-LM models shown in main screen body.
 */
@Composable
private fun OnDeviceModelCard(
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
                // Model Options Header
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
        color = ScreenBackground,
        border = BorderStroke(if (isActive) 1.5.dp else 1.dp, if (isActive) RustPrimary else CardBorder),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = model.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${model.formattedSize})",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Text(
                    text = model.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            when {
                downloadState != null -> {
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
                }

                isActive -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
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
    }
}

/**
 * Centered "NEW CONVERSATION" divider line with current title & quick start chip.
 */
@Composable
private fun NewConversationDivider(
    title: String,
    onNewChatClick: () -> Unit
) {
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
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ScreenBackground,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .clickable { onNewChatClick() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = title.uppercase(LocalLocale.current.platformLocale),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = RustPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = RustPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = CardBorder,
            thickness = 1.dp
        )
    }
}

/**
 * Message Bubble matching Figma design with streaming typing support & TTS / Reader Mode integration.
 */
@Composable
fun ChatBubbleItem(
    message: ChatBotMessage,
    isTtsSpeaking: Boolean = false,
    isTtsPaused: Boolean = false,
    onTtsClick: () -> Unit = {},
    onReaderModeClick: () -> Unit = {}
) {
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
                            FormattedMarkdownText(
                                text = message.text,
                                textColor = TextPrimary,
                                fontSize = 15.sp,
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
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTtsSpeaking) RustSoftBg else CardBackground
                        ),
                        border = BorderStroke(
                            width = if (isTtsSpeaking || isTtsPaused) 1.5.dp else 1.dp,
                            color = if (isTtsSpeaking || isTtsPaused) RustPrimary else CardBorder
                        ),
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
                                    color = RustPrimary
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    if (message.text.isNotBlank() && !message.isPending) {
                                        // TTS Listen Button
                                        IconButton(
                                            onClick = onTtsClick,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = when {
                                                    isTtsSpeaking -> Icons.Default.Pause
                                                    isTtsPaused -> Icons.Default.PlayArrow
                                                    else -> Icons.AutoMirrored.Filled.VolumeUp
                                                },
                                                contentDescription = "Listen Response",
                                                tint = if (isTtsSpeaking || isTtsPaused) RustPrimary else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Reader Mode Button
                                        IconButton(
                                            onClick = onReaderModeClick,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ChromeReaderMode,
                                                contentDescription = "Reader Mode",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    if (message.isPending) {
                                        CircularProgressIndicator(
                                            color = RustPrimary,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (message.text.isEmpty()) {
                                Text(
                                    text = "Thinking...",
                                    fontSize = 15.sp,
                                    color = TextMuted,
                                    lineHeight = 22.sp
                                )
                            } else {
                                FormattedMarkdownText(
                                    text = message.text,
                                    textColor = TextPrimary,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
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
                        FormattedMarkdownText(
                            text = message.text,
                            textColor = Color(0xFF991B1B),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Markdown Formatted Text Renderer for Chat Bot messages.
 */
@Composable
fun FormattedMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 22.sp
) {
    val lines = text.split("\n")
    var inCodeBlock = false
    val codeBlockLines = mutableListOf<String>()

    Column(modifier = modifier) {
        lines.forEachIndexed { index, rawLine ->
            val trimmedLine = rawLine.trim()

            // Handle Code Block ```
            if (trimmedLine.startsWith("```")) {
                if (inCodeBlock) {
                    val codeContent = codeBlockLines.joinToString("\n")
                    CodeBlockItem(code = codeContent)
                    codeBlockLines.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                return@forEachIndexed
            }

            if (inCodeBlock) {
                codeBlockLines.add(rawLine)
                return@forEachIndexed
            }

            when {
                // Horizontal Divider: --- or *** or ___
                trimmedLine.matches(Regex("^(\\s*[-*_]){3,}\\s*$")) -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = CardBorder.copy(alpha = 0.6f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Headers: #, ##, ###
                trimmedLine.startsWith("#") -> {
                    val level = trimmedLine.takeWhile { it == '#' }.length
                    val headerText = trimmedLine.removePrefix("#".repeat(level)).trim()
                    val headerFontSize = when (level) {
                        1 -> 18.sp
                        2 -> 16.sp
                        else -> 15.sp
                    }
                    val annotatedHeader = parseInlineMarkdown(headerText, textColor)
                    if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = annotatedHeader,
                        fontSize = headerFontSize,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = (headerFontSize.value + 6).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Bullet items: - or *
                trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> {
                    val bulletContent = trimmedLine.drop(2).trim()
                    val annotated = parseInlineMarkdown(bulletContent, textColor)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "•  ",
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = RustPrimary
                        )
                        Text(
                            text = annotated,
                            fontSize = fontSize,
                            color = textColor,
                            lineHeight = lineHeight
                        )
                    }
                }

                // Numbered list items: 1.  2.  10.  etc.
                trimmedLine.matches(Regex("^\\d+\\.\\s+.*$")) -> {
                    val dotIndex = trimmedLine.indexOf('.')
                    val numberStr = trimmedLine.substring(0, dotIndex + 1)
                    val contentStr = trimmedLine.substring(dotIndex + 1).trim()
                    val annotated = parseInlineMarkdown(contentStr, textColor)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "$numberStr ",
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = RustPrimary
                        )
                        Text(
                            text = annotated,
                            fontSize = fontSize,
                            color = textColor,
                            lineHeight = lineHeight
                        )
                    }
                }

                // Empty lines
                trimmedLine.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Normal Paragraph line
                else -> {
                    val annotatedLine = parseInlineMarkdown(rawLine, textColor)
                    Text(
                        text = annotatedLine,
                        fontSize = fontSize,
                        color = textColor,
                        lineHeight = lineHeight,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }

        if (inCodeBlock && codeBlockLines.isNotEmpty()) {
            CodeBlockItem(code = codeBlockLines.joinToString("\n"))
        }
    }
}

@Composable
private fun CodeBlockItem(code: String) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = code,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFF1F5F9),
            modifier = Modifier.padding(12.dp)
        )
    }
}

/**
 * Parses inline markdown tags (**bold**, *italic*, `code`) into AnnotatedString.
 */
fun parseInlineMarkdown(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val pattern = Regex("(\\*{2}|_{2}|\\*|_|`)(.*?)\\1")
        var currentIndex = 0

        pattern.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val delimiter = matchResult.groupValues[1]
            val content = matchResult.groupValues[2]

            if (start > currentIndex) {
                append(text.substring(currentIndex, start))
            }

            when (delimiter) {
                "**", "__" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                        append(content)
                    }
                }
                "*", "_" -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor)) {
                        append(content)
                    }
                }
                "`" -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            background = Color(0xFFF1F5F9),
                            color = Color(0xFF0F172A)
                        )
                    ) {
                        append(" $content ")
                    }
                }
                else -> append(matchResult.value)
            }

            currentIndex = end
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

/**
 * "A PLACE TO START" prompt suggestions displaying 3 dynamic or curated prompts.
 */
@Composable
private fun PlaceToStartSection(
    prompts: List<String>,
    enabled: Boolean,
    onPromptClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Suggestions",
                tint = RustPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "A PLACE TO START",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = TextSecondary
            )
        }

        prompts.take(3).forEachIndexed { index, promptText ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(10.dp))
            }
            PromptCard(
                text = promptText,
                enabled = enabled,
                onClick = { onPromptClick(promptText) }
            )
        }
    }
}

@Composable
private fun PromptCard(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
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
                color = if (enabled) TextPrimary else TextMuted,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ArrowOutward,
                contentDescription = "Send Prompt",
                tint = if (enabled) RustPrimary else TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Bottom Input Bar with rust circle send button and on-device privacy badge.
 */
@Composable
private fun ChatBotInputSection(
    isReady: Boolean,
    isGenerating: Boolean,
    onSendMessage: (String) -> Unit
) {
    var inputText by rememberSaveable { mutableStateOf("") }

    Surface(
        color = ScreenBackground,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rounded Input Text Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isReady) "Ask NewsAIBuddy..." else "Load a model to start chat...",
                            color = TextMuted,
                            fontSize = 15.sp
                        )
                    },
                    enabled = isReady && !isGenerating,
                    singleLine = false,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && isReady && !isGenerating) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        }
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        disabledContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = RustPrimary,
                        unfocusedBorderColor = CardBorder,
                        disabledBorderColor = CardBorder
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Rust Circle Upward-Arrow Send Button
                Surface(
                    onClick = {
                        if (inputText.isNotBlank() && isReady && !isGenerating) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && isReady && !isGenerating,
                    shape = CircleShape,
                    color = if (inputText.isNotBlank() && isReady && !isGenerating) RustPrimary else Color(0xFFE5E7EB),
                    modifier = Modifier.size(48.dp)
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
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank() && isReady && !isGenerating) Color.White else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Runs 100% on-device via LiteRT-LM • Zero data leaves phone",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
