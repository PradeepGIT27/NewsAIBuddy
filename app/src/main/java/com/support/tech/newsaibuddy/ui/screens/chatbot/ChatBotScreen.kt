package com.support.tech.newsaibuddy.ui.screens.chatbot

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.support.tech.newsaibuddy.data.litertlm.DownloadState
import com.support.tech.newsaibuddy.ui.components.ReaderContent
import com.support.tech.newsaibuddy.ui.components.ReaderModeSheet
import com.support.tech.newsaibuddy.ui.components.TtsControlBar
import com.support.tech.newsaibuddy.ui.screens.chatbot.components.ChatBotInputSection
import com.support.tech.newsaibuddy.ui.screens.chatbot.components.ChatBotTopBar
import com.support.tech.newsaibuddy.ui.screens.chatbot.components.ChatBubbleItem
import com.support.tech.newsaibuddy.ui.screens.chatbot.components.HistoryBottomSheet
import com.support.tech.newsaibuddy.ui.screens.chatbot.components.ModelSelectionBottomSheet
import com.support.tech.newsaibuddy.ui.screens.chatbot.components.OnDeviceModelCard
import com.support.tech.newsaibuddy.ui.screens.chatbot.components.PlaceToStartSection
import com.support.tech.newsaibuddy.ui.screens.chatbot.components.TopDownloadProgressBanner
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.ScreenBackground
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.viewmodel.chatbot.ChatBotViewModel
import com.support.tech.newsaibuddy.ui.viewmodel.chatbot.ModelStatus
import com.support.tech.newsaibuddy.utils.SpeechRecognizerManager
import com.support.tech.newsaibuddy.utils.TtsStatus

/**
 * Clean, modular ChatBot Screen powered by Google LiteRT-LM (On-Device LLM).
 */
@Composable
fun ChatBotScreen(
    navController: NavController,
    chatViewModel: ChatBotViewModel = hiltViewModel()
) {
    val chatUiState by chatViewModel.uiState.collectAsState()
    val ttsState by chatViewModel.ttsState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var activeReaderContent by remember { mutableStateOf<ReaderContent?>(null) }

    val speechRecognizerManager = remember(context) {
        SpeechRecognizerManager(
            context = context,
            onResult = { text ->
                if (text.isNotBlank()) {
                    chatViewModel.sendMessage(text)
                }
            },
            onError = { err ->
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechRecognizerManager.startListening()
        } else {
            Toast.makeText(context, "Microphone permission required for voice input.", Toast.LENGTH_SHORT).show()
        }
    }

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
                    },
                    onMicClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            speechRecognizerManager.startListening()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (chatUiState.downloadState is DownloadState.Downloading) {
                    TopDownloadProgressBanner(
                        downloadState = chatUiState.downloadState as DownloadState.Downloading,
                        onCancel = { chatViewModel.cancelDownload() }
                    )
                }

                NewConversationDivider(
                    title = chatUiState.currentConversationTitle,
                    onNewChatClick = { chatViewModel.startNewConversation() }
                )

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
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
