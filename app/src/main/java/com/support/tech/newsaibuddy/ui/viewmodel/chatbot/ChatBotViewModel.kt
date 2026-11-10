package com.support.tech.newsaibuddy.ui.viewmodel.chatbot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender
import com.support.tech.newsaibuddy.data.litertlm.DownloadState
import com.support.tech.newsaibuddy.data.litertlm.LiteRtLmService
import com.support.tech.newsaibuddy.data.litertlm.LiteRtModel
import com.support.tech.newsaibuddy.data.litertlm.LiteRtModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for handling on-device AI chat interactions using Google LiteRT-LM.
 * Manages model downloading, initialization, streaming generation, and UI state.
 */
@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val liteRtModelManager: LiteRtModelManager,
    private val liteRtLmService: LiteRtLmService
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

    init {
        checkAndInitLocalModel()
    }

    /**
     * Checks if a local .litertlm file exists on device storage.
     */
    fun checkAndInitLocalModel() {
        val existingModel = liteRtModelManager.findExistingModelFile()
        if (existingModel != null && existingModel.exists()) {
            loadModel(existingModel.absolutePath)
        } else {
            _uiState.update {
                it.copy(modelStatus = ModelStatus.NotDownloaded(LiteRtModel.ALL_MODELS))
            }
        }
    }

    /**
     * Initializes the LiteRT-LM engine with the specified model file path.
     */
    fun loadModel(modelPath: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(modelStatus = ModelStatus.Initializing("Loading on-device model..."))
            }

            val result = liteRtLmService.initialize(modelPath)
            result.fold(
                onSuccess = { backend ->
                    _uiState.update {
                        it.copy(modelStatus = ModelStatus.Ready(backend = backend, modelPath = modelPath))
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Model initialization failed", error)
                    _uiState.update {
                        it.copy(
                            modelStatus = ModelStatus.Error(
                                error.localizedMessage ?: "Failed to initialize LiteRT-LM engine."
                            )
                        )
                    }
                }
            )
        }
    }

    /**
     * Downloads an on-device model directly within the app and initializes it.
     */
    fun startDownload(model: LiteRtModel) {
        viewModelScope.launch {
            liteRtModelManager.downloadModel(model).collect { downloadState ->
                _uiState.update { it.copy(downloadState = downloadState) }

                when (downloadState) {
                    is DownloadState.Completed -> {
                        loadModel(downloadState.filePath)
                    }
                    is DownloadState.Failed -> {
                        _uiState.update {
                            it.copy(
                                modelStatus = ModelStatus.Error(downloadState.error)
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    /**
     * Sends a user message and streams the AI response chunk by chunk.
     */
    fun sendMessage(userMessage: String) {
        val trimmed = userMessage.trim()
        if (trimmed.isBlank() || _uiState.value.isGenerating) return

        val userChatMessage = ChatBotMessage(
            text = trimmed,
            participant = ChatBotMessageSender.USER,
            isPending = false
        )

        val botMessageId = UUID.randomUUID().toString()
        val pendingBotMessage = ChatBotMessage(
            id = botMessageId,
            text = "",
            participant = ChatBotMessageSender.MODEL,
            isPending = true
        )

        _uiState.update { current ->
            current.copy(
                messages = current.messages + userChatMessage + pendingBotMessage,
                isGenerating = true
            )
        }

        viewModelScope.launch {
            val responseBuilder = StringBuilder()
            try {
                liteRtLmService.sendMessageStream(trimmed).collect { chunk ->
                    responseBuilder.append(chunk)
                    val currentText = responseBuilder.toString()

                    _uiState.update { current ->
                        val updatedMessages = current.messages.map { msg ->
                            if (msg.id == botMessageId) {
                                msg.copy(text = currentText, isPending = false)
                            } else {
                                msg
                            }
                        }
                        current.copy(messages = updatedMessages)
                    }
                }

                _uiState.update { current ->
                    current.copy(isGenerating = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating response", e)
                _uiState.update { current ->
                    val resolvedMessages = current.messages.filter { it.id != botMessageId }
                    val errorMessage = ChatBotMessage(
                        text = "Error: ${e.localizedMessage ?: "Failed to generate on-device response."}",
                        participant = ChatBotMessageSender.ERROR
                    )
                    current.copy(
                        messages = resolvedMessages + errorMessage,
                        isGenerating = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        liteRtLmService.close()
    }

    companion object {
        private const val TAG = "ChatBotViewModel"
    }
}
