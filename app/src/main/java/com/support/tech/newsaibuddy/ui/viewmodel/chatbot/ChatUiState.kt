package com.support.tech.newsaibuddy.ui.viewmodel.chatbot

import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.litertlm.DownloadState
import com.support.tech.newsaibuddy.data.litertlm.LiteRtModel

/**
 * State representing the status of the on-device LiteRT-LM model.
 */
sealed interface ModelStatus {
    data object Checking : ModelStatus
    data class NotDownloaded(val availableModels: List<LiteRtModel>) : ModelStatus
    data class Initializing(val info: String = "Initializing on-device AI...") : ModelStatus
    data class Ready(val backend: String, val modelPath: String) : ModelStatus
    data class Error(val error: String) : ModelStatus
}

/**
 * Immutable UI state for the chat screen.
 *
 * @param messages The list of messages in the chat.
 * @param isGenerating Whether the model is currently generating a response.
 * @param modelStatus The current lifecycle status of the on-device LiteRT-LM model.
 * @param downloadState The state of any active in-app model download.
 */
data class ChatUiState(
    val messages: List<ChatBotMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val modelStatus: ModelStatus = ModelStatus.Checking,
    val downloadState: DownloadState = DownloadState.Idle
)
