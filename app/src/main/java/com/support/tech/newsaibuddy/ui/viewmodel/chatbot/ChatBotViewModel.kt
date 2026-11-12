package com.support.tech.newsaibuddy.ui.viewmodel.chatbot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender
import com.support.tech.newsaibuddy.data.local.ChatConversation
import com.support.tech.newsaibuddy.data.local.ChatHistoryManager
import com.support.tech.newsaibuddy.data.local.toSerializable
import com.support.tech.newsaibuddy.data.litertlm.DownloadState
import com.support.tech.newsaibuddy.data.litertlm.LiteRtLmService
import com.support.tech.newsaibuddy.data.litertlm.LiteRtModel
import com.support.tech.newsaibuddy.data.litertlm.LiteRtModelManager
import com.support.tech.newsaibuddy.domain.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for handling on-device AI chat interactions using Google LiteRT-LM.
 * Manages model downloading, initialization, switching, streaming generation, conversation history, and UI state.
 */
@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val liteRtModelManager: LiteRtModelManager,
    private val liteRtLmService: LiteRtLmService,
    private val newsRepository: NewsRepository,
    private val chatHistoryManager: ChatHistoryManager
) : ViewModel() {

    private val initialWelcomeMessage = ChatBotMessage(
        text = "Hello! I'm NewsAIBuddy, your on-device AI companion running 100% locally with Google LiteRT-LM. How can I assist you today?",
        participant = ChatBotMessageSender.MODEL,
        isPending = false
    )

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = listOf(initialWelcomeMessage),
            modelStatus = ModelStatus.Checking
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var downloadJob: Job? = null

    init {
        checkAndInitLocalModel()
        fetchDynamicStarterPrompts()
        observeConversationHistory()
    }

    /**
     * Observes local conversation history from DataStore.
     */
    private fun observeConversationHistory() {
        viewModelScope.launch {
            chatHistoryManager.conversations.collect { convList ->
                _uiState.update { it.copy(conversations = convList) }
            }
        }
    }

    /**
     * Updates the set of model IDs that are downloaded on local device storage.
     */
    fun refreshDownloadedModels() {
        val downloadedSet = LiteRtModel.ALL_MODELS
            .filter { liteRtModelManager.isModelDownloaded(it) }
            .map { it.id }
            .toSet()

        _uiState.update { it.copy(downloadedModelIds = downloadedSet) }
    }

    /**
     * Dynamically fetches top headlines if internet connection is active to generate
     * relevant starter prompt cards under "A PLACE TO START", falling back to curated prompts if offline.
     */
    private fun fetchDynamicStarterPrompts() {
        viewModelScope.launch {
            newsRepository.getTopHeadlinesByCategory("general").collect { resource ->
                if (resource is ResourceState.Success && resource.data.isNotEmpty()) {
                    val articles = resource.data.filter { !it.title.isNullOrBlank() }.take(3)
                    if (articles.isNotEmpty()) {
                        val dynamicPrompts = mutableListOf<String>()

                        val firstTitle = articles.getOrNull(0)?.title?.trim()
                        if (firstTitle != null) {
                            dynamicPrompts.add("Summarize story: \"${truncateTitle(firstTitle)}\"")
                        }

                        val secondTitle = articles.getOrNull(1)?.title?.trim()
                        if (secondTitle != null) {
                            dynamicPrompts.add("What are the key takeaways from: \"${truncateTitle(secondTitle)}\"?")
                        } else {
                            dynamicPrompts.add("Explain key business & technology terms in simple words")
                        }

                        val thirdTitle = articles.getOrNull(2)?.title?.trim()
                        if (thirdTitle != null) {
                            dynamicPrompts.add("What is the background context of: \"${truncateTitle(thirdTitle)}\"?")
                        } else {
                            dynamicPrompts.add("What are the main implications of current global headlines?")
                        }

                        _uiState.update { current ->
                            current.copy(starterPrompts = dynamicPrompts)
                        }
                    }
                }
            }
        }
    }

    private fun truncateTitle(title: String, maxLength: Int = 55): String {
        return if (title.length > maxLength) {
            title.take(maxLength - 3) + "..."
        } else {
            title
        }
    }

    /**
     * Checks if a local .litertlm file exists on device storage and loads it.
     */
    fun checkAndInitLocalModel() {
        refreshDownloadedModels()

        // If Singleton LiteRtLmService already has an active engine in memory, restore state immediately
        if (liteRtLmService.isReady && liteRtLmService.loadedModelPath != null) {
            val loadedPath = liteRtLmService.loadedModelPath!!
            val matchedModel = LiteRtModel.ALL_MODELS.find {
                it.filename.equals(java.io.File(loadedPath).name, ignoreCase = true) ||
                it.id == liteRtModelManager.getSelectedModelId()
            }
            if (matchedModel != null) {
                _uiState.update {
                    it.copy(
                        modelStatus = ModelStatus.Ready(
                            backend = liteRtLmService.activeBackend ?: "CPU",
                            modelPath = loadedPath,
                            activeModel = matchedModel
                        ),
                        activeModel = matchedModel
                    )
                }
                return
            }
        }

        val existingModelFile = liteRtModelManager.findExistingModelFile()
        if (existingModelFile != null && existingModelFile.exists()) {
            val matchedModel = LiteRtModel.ALL_MODELS.find {
                it.filename.equals(existingModelFile.name, ignoreCase = true) ||
                it.id == liteRtModelManager.getSelectedModelId()
            } ?: LiteRtModel.QWEN_0_6B

            loadModel(matchedModel)
        } else {
            _uiState.update {
                it.copy(
                    modelStatus = ModelStatus.NotDownloaded(LiteRtModel.ALL_MODELS),
                    activeModel = null
                )
            }
        }
    }

    /**
     * Handles user action to select a model. If downloaded, switches to it; otherwise starts download.
     */
    fun selectModel(model: LiteRtModel) {
        if (liteRtModelManager.isModelDownloaded(model)) {
            loadModel(model)
        } else {
            startDownload(model)
        }
    }

    /**
     * Initializes the LiteRT-LM engine with the specified model, cancelling any in-flight load.
     */
    fun loadModel(model: LiteRtModel, forceCpu: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    modelStatus = ModelStatus.Initializing(
                        info = "Loading ${model.displayName}...",
                        model = model
                    ),
                    activeModel = model
                )
            }

            val modelFile = liteRtModelManager.getModelFile(model)
                ?: liteRtModelManager.getTargetModelFile(model)

            if (!modelFile.exists()) {
                _uiState.update {
                    it.copy(
                        modelStatus = ModelStatus.NotDownloaded(LiteRtModel.ALL_MODELS),
                        activeModel = null
                    )
                }
                return@launch
            }

            val result = liteRtLmService.initialize(modelFile.absolutePath, forceCpu)
            result.fold(
                onSuccess = { backend ->
                    liteRtModelManager.setSelectedModelId(model.id)
                    refreshDownloadedModels()
                    _uiState.update {
                        it.copy(
                            modelStatus = ModelStatus.Ready(
                                backend = backend,
                                modelPath = modelFile.absolutePath,
                                activeModel = model
                            ),
                            activeModel = model,
                            showModelSelectorSheet = false
                        )
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Model initialization failed for ${model.displayName}", error)
                    val errorMsg = error.localizedMessage ?: "Failed to initialize ${model.displayName}."
                    _uiState.update {
                        it.copy(
                            modelStatus = ModelStatus.Error(
                                error = errorMsg,
                                failedModel = model
                            )
                        )
                    }
                }
            )
        }
    }

    /**
     * Downloads an on-device model directly in background and initializes it upon completion.
     */
    fun startDownload(model: LiteRtModel) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            liteRtModelManager.downloadModel(model).collect { downloadState ->
                _uiState.update { it.copy(downloadState = downloadState) }

                if (downloadState is DownloadState.Completed) {
                    refreshDownloadedModels()
                    loadModel(downloadState.model)
                }
            }
        }
    }

    /**
     * Cancels an active in-flight model download.
     */
    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        liteRtModelManager.cancelDownload()
        _uiState.update { it.copy(downloadState = DownloadState.Idle) }
    }

    fun toggleModelSelector(show: Boolean) {
        _uiState.update { it.copy(showModelSelectorSheet = show) }
    }

    fun toggleHistorySheet(show: Boolean) {
        _uiState.update { it.copy(showHistorySheet = show) }
    }

    fun updateHistorySearchQuery(query: String) {
        _uiState.update { it.copy(historySearchQuery = query) }
    }

    fun setConversationToDelete(conversation: ChatConversation?) {
        _uiState.update { it.copy(conversationToDelete = conversation) }
    }

    fun setShowClearAllDialog(show: Boolean) {
        _uiState.update { it.copy(showClearAllDialog = show) }
    }

    /**
     * Resets active chat screen state and model context to start a fresh conversation.
     */
    fun startNewConversation() {
        viewModelScope.launch {
            liteRtLmService.resetConversation()
            _uiState.update {
                it.copy(
                    currentConversationId = null,
                    currentConversationTitle = "New Conversation",
                    messages = listOf(initialWelcomeMessage),
                    showHistorySheet = false
                )
            }
        }
    }

    /**
     * Loads an existing conversation thread from history.
     */
    fun loadConversation(conversation: ChatConversation) {
        viewModelScope.launch {
            liteRtLmService.resetConversation()
            val domainMsgs = conversation.toDomainMessages()
            val messagesToDisplay = domainMsgs.ifEmpty { listOf(initialWelcomeMessage) }

            _uiState.update {
                it.copy(
                    currentConversationId = conversation.id,
                    currentConversationTitle = conversation.title,
                    messages = messagesToDisplay,
                    showHistorySheet = false
                )
            }
        }
    }

    /**
     * Deletes a specific conversation entry from local history.
     */
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            chatHistoryManager.deleteConversation(conversationId)
            _uiState.update { it.copy(conversationToDelete = null) }

            if (_uiState.value.currentConversationId == conversationId) {
                startNewConversation()
            }
        }
    }

    /**
     * Clears all saved conversation history from device storage.
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            chatHistoryManager.clearAllHistory()
            _uiState.update { it.copy(showClearAllDialog = false) }
            startNewConversation()
        }
    }

    /**
     * Generates a concise title for a conversation based on the user's first prompt.
     */
    private fun generateConversationTitle(prompt: String): String {
        val cleaned = prompt.trim().replace("\n", " ")
        return if (cleaned.length > 35) {
            cleaned.take(32) + "..."
        } else {
            cleaned.ifBlank { "New Conversation" }
        }
    }

    /**
     * Auto-saves or updates the active conversation entry in local storage.
     */
    private suspend fun autoSaveConversation(messages: List<ChatBotMessage>, firstUserText: String) {
        val currentState = _uiState.value
        val convId = currentState.currentConversationId ?: UUID.randomUUID().toString()
        val convTitle = if (currentState.currentConversationId == null || currentState.currentConversationTitle == "New Conversation") {
            generateConversationTitle(firstUserText)
        } else {
            currentState.currentConversationTitle
        }

        if (currentState.currentConversationId == null) {
            _uiState.update {
                it.copy(
                    currentConversationId = convId,
                    currentConversationTitle = convTitle
                )
            }
        }

        val conversationToSave = ChatConversation(
            id = convId,
            title = convTitle,
            updatedAt = System.currentTimeMillis(),
            messages = messages.map { it.toSerializable() },
            modelId = currentState.activeModel?.id
        )

        chatHistoryManager.saveOrUpdateConversation(conversationToSave)
    }

    /**
     * Sends a prompt to the LiteRT-LM engine and streams the generated response.
     * Automatically saves each conversation and message to local offline history.
     */
    fun sendMessage(userText: String) {
        if (userText.isBlank() || _uiState.value.isGenerating) return

        val userMessage = ChatBotMessage(
            text = userText.trim(),
            participant = ChatBotMessageSender.USER,
            isPending = false
        )

        val modelMessageId = UUID.randomUUID().toString()
        val modelPendingMessage = ChatBotMessage(
            id = modelMessageId,
            text = "",
            participant = ChatBotMessageSender.MODEL,
            isPending = true
        )

        val newMessagesList = _uiState.value.messages + userMessage + modelPendingMessage

        _uiState.update { current ->
            current.copy(
                messages = newMessagesList,
                isGenerating = true
            )
        }

        viewModelScope.launch {
            // Save initial user message & pending bot state
            autoSaveConversation(newMessagesList, userText)

            try {
                var accumulatedResponse = ""
                liteRtLmService.sendMessageStream(userText.trim()).collect { token ->
                    accumulatedResponse += token
                    val updatedText = accumulatedResponse

                    _uiState.update { current ->
                        val updatedMessages = current.messages.map { msg ->
                            if (msg.id == modelMessageId) {
                                msg.copy(text = updatedText, isPending = false)
                            } else {
                                msg
                            }
                        }
                        current.copy(messages = updatedMessages)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating response", e)
                _uiState.update { current ->
                    val updatedMessages = current.messages.map { msg ->
                        if (msg.id == modelMessageId) {
                            msg.copy(
                                text = "Sorry, an error occurred while generating response: ${e.localizedMessage}",
                                isPending = false
                            )
                        } else {
                            msg
                        }
                    }
                    current.copy(messages = updatedMessages)
                }
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
                // Save final state with complete bot response to local history
                autoSaveConversation(_uiState.value.messages, userText)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Do not call liteRtLmService.close() here as liteRtLmService is a @Singleton.
        // Keeping it active preserves the loaded model in memory when switching tabs.
    }

    companion object {
        private const val TAG = "ChatBotViewModel"
    }
}
