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
    data class Initializing(val info: String = "Initializing on-device AI...", val model: LiteRtModel? = null) : ModelStatus
    data class Ready(val backend: String, val modelPath: String, val activeModel: LiteRtModel?) : ModelStatus
    data class Error(val error: String, val failedModel: LiteRtModel? = null) : ModelStatus
}

/**
 * Immutable UI state for the chat screen.
 *
 * @param messages The list of messages in the chat.
 * @param isGenerating Whether the model is currently generating a response.
 * @param modelStatus The current lifecycle status of the on-device LiteRT-LM model.
 * @param downloadState The state of any active in-app model download.
 * @param starterPrompts List of 3 dynamic or fallback starter prompts for "A PLACE TO START".
 * @param availableModels All supported models.
 * @param downloadedModelIds Set of model IDs that exist on local storage.
 * @param activeModel The model currently loaded in the LiteRT-LM engine.
 * @param showModelSelectorSheet Whether the model selection sheet/dialog is currently visible.
 */
data class ChatUiState(
    val messages: List<ChatBotMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val modelStatus: ModelStatus = ModelStatus.Checking,
    val downloadState: DownloadState = DownloadState.Idle,
    val starterPrompts: List<String> = DEFAULT_STARTER_PROMPTS,
    val availableModels: List<LiteRtModel> = LiteRtModel.ALL_MODELS,
    val downloadedModelIds: Set<String> = emptySet(),
    val activeModel: LiteRtModel? = null,
    val showModelSelectorSheet: Boolean = false
) {
    companion object {
        val DEFAULT_STARTER_PROMPTS = listOf(
            "Summarize top news stories from today",
            "Explain key business & technology terms in simple words",
            "What are the main implications of current global headlines?"
        )
    }
}
