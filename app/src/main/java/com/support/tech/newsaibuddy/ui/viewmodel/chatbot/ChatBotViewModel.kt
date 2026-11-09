package com.support.tech.newsaibuddy.ui.viewmodel.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling chat interactions.
 * Manages conversation state with Google Gemini AI model using immutable StateFlow updates.
 */
@HiltViewModel
class ChatBotViewModel @Inject constructor(
    generativeModel: GenerativeModel
) : ViewModel() {

    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("Hello, ChatBot.") },
            content(role = "model") { text("Great to meet you. What would you like to know?") }
        )
    )

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = chat.history.map { content ->
                ChatBotMessage(
                    text = content.parts.first().asTextOrNull() ?: "",
                    participant = if (content.role == "user") ChatBotMessageSender.USER else ChatBotMessageSender.MODEL,
                    isPending = false
                )
            }
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /**
     * Sends a message from the user to the Gemini AI model.
     * Employs Unidirectional Data Flow by appending a pending message first,
     * awaiting model completion, and updating with the model response or error.
     */
    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val pendingUserMessage = ChatBotMessage(
            text = userMessage,
            participant = ChatBotMessageSender.USER,
            isPending = true
        )

        _uiState.update { current ->
            current.copy(messages = current.messages + pendingUserMessage)
        }

        viewModelScope.launch {
            var responseText: String? = null
            var lastError: Exception? = null

            // Retry up to 3 times in case of temporary 503 high-demand spikes
            for (attempt in 1..3) {
                try {
                    val response = chat.sendMessage(userMessage)
                    responseText = response.text
                    lastError = null
                    break
                } catch (e: Exception) {
                    lastError = e
                    val errorMsg = e.message.orEmpty()
                    val isTemporary = errorMsg.contains("503") ||
                            errorMsg.contains("high demand", ignoreCase = true) ||
                            errorMsg.contains("UNAVAILABLE", ignoreCase = true)

                    if (isTemporary && attempt < 3) {
                        kotlinx.coroutines.delay(attempt * 1500L)
                    } else {
                        break
                    }
                }
            }

            _uiState.update { current ->
                val resolvedMessages = current.messages.map { msg ->
                    if (msg.id == pendingUserMessage.id) msg.copy(isPending = false) else msg
                }

                if (responseText != null) {
                    val modelMessage = ChatBotMessage(
                        text = responseText,
                        participant = ChatBotMessageSender.MODEL,
                        isPending = false
                    )
                    current.copy(messages = resolvedMessages + modelMessage)
                } else {
                    val rawMsg = lastError?.localizedMessage.orEmpty()
                    val userFriendlyMsg = when {
                        rawMsg.contains("503") || rawMsg.contains("high demand", ignoreCase = true) ->
                            "The Gemini model is currently experiencing high demand. Please try sending your message again in a moment."
                        rawMsg.contains("404") || rawMsg.contains("NOT_FOUND", ignoreCase = true) ->
                            "Model not found. Please check model configuration."
                        rawMsg.contains("API_KEY_INVALID", ignoreCase = true) || rawMsg.contains("403") ->
                            "Invalid or unauthorized API key. Please check your Gemini API key in local.properties."
                        else -> rawMsg.ifBlank { "Something went wrong. Please try again." }
                    }

                    val errorMessage = ChatBotMessage(
                        text = userFriendlyMsg,
                        participant = ChatBotMessageSender.ERROR
                    )
                    current.copy(messages = resolvedMessages + errorMessage)
                }
            }
        }
    }
}
