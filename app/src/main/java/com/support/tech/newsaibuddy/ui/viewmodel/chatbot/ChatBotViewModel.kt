package com.support.tech.newsaibuddy.ui.viewmodel.chatbot

import android.util.Log
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
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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

            // Retry up to 2 times for transient 503 / server / timeout errors
            val maxRetries = 2
            for (attempt in 0..maxRetries) {
                try {
                    // Timeout after 60 seconds to allow model generation while preventing infinite loading spinners
                    val response = withTimeoutOrNull(60000L.milliseconds) {
                        chat.sendMessage(userMessage)
                    }
                    if (response != null) {
                        responseText = response.text
                        break
                    } else {
                        lastError = Exception("Request timed out. Please try again.")
                    }
                } catch (e: Exception) {
                    val isTimeout = e is java.util.concurrent.CancellationException ||
                            e.cause is java.util.concurrent.CancellationException ||
                            e.message?.contains("Timed out", ignoreCase = true) == true
                    lastError = if (isTimeout) {
                        Exception("Request timed out. Please try again.")
                    } else {
                        e
                    }
                    Log.e("ChatBotViewModel", "Attempt ${attempt + 1} failed sending message", e)

                    val errorMsg = e.message.orEmpty()
                    val isTransient = isTimeout || errorMsg.contains("503") || errorMsg.contains("500") || errorMsg.contains("UNAVAILABLE", ignoreCase = true)
                    if (isTransient && attempt < maxRetries) {
                        kotlinx.coroutines.delay((1000L * (attempt + 1)).milliseconds)
                        continue
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
                    val formattedMsg = when {
                        rawMsg.contains("503") || rawMsg.contains("UNAVAILABLE", ignoreCase = true) ->
                            "The AI service is temporarily overloaded (HTTP 503). Please try again in a moment."
                        rawMsg.contains("500") || rawMsg.contains("INTERNAL", ignoreCase = true) ->
                            "The AI service encountered a temporary server error. Please try again."
                        rawMsg.contains("Timed out", ignoreCase = true) ->
                            "Request timed out. Please try again."
                        rawMsg.contains("UnknownException") || rawMsg.contains("Something unexpected happened") ->
                            "Something went wrong with the AI service. Please try again."
                        rawMsg.isBlank() -> "Something went wrong. Please try again."
                        else -> rawMsg
                    }
                    val errorMessage = ChatBotMessage(
                        text = formattedMsg,
                        participant = ChatBotMessageSender.ERROR
                    )
                    current.copy(messages = resolvedMessages + errorMessage)
                }
            }
        }
    }
}
