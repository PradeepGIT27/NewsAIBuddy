package com.support.tech.newsaibuddy.ui.viewmodel.chatbot

import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling chat interactions.
 *
 * This ViewModel manages the state of the chat, including the history of messages
 * and the current UI state. It uses a [GenerativeModel] to interact with the AI
 * and sends/receives messages.
 *
 * @property generativeModel The AI model used for generating responses.
 */
@HiltViewModel
class ChatBotViewModel @Inject constructor( generativeModel: GenerativeModel) : ViewModel() {
    // A mutable list to store chat messages. This is not directly exposed to the UI.
    private val _chatMessages = mutableStateListOf<ChatBotMessage>()
    // An immutable view of the chat messages for the UI to observe.
    val chatMessages: List<ChatBotMessage> = _chatMessages

    // Initializes the chat session with a predefined history.
    // This sets up an initial conversation context for the AI.
    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("Hello, ChatBot.") },
            content(role = "model") { text("Great to meet you. What would you like to know?") }
        )
    )

    // MutableStateFlow to hold the current UI state of the chat.
    // It's initialized with the chat history, mapping AI content to ChatBotMessage objects.
    // ChatUiState is a data class that likely encapsulates the list of messages and
    // potentially other UI-related state.
    private val _uiState: MutableStateFlow<ChatUiState> =
        MutableStateFlow(ChatUiState(chat.history.map { content ->
            // Map the initial messages
            ChatBotMessage(
                text = content.parts.first().asTextOrNull() ?: "",
                participant = if (content.role == "user") ChatBotMessageSender.USER else ChatBotMessageSender.MODEL,
                isPending = false
            )
        }))
    // Exposes the UI state as an immutable StateFlow for the UI to observe changes.
    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()


    /**
     * Sends a message from the user to the chat.
     *
     * This function adds the user's message to the UI as a pending message,
     * then sends it to the AI model and updates the UI with the model's response.
     * It handles potential errors during the API call.
     *
     * @param userMessage The text of the message sent by the user.
     */
    fun sendMessage(userMessage: String) {
        // Add a pending message
        _uiState.value.addMessage(
            ChatBotMessage(
                text = userMessage,
                participant = ChatBotMessageSender.USER,
                isPending = true
            )
        )

        viewModelScope.launch {
            // Launch a coroutine in the ViewModel's scope to handle the asynchronous API call.
            try {
                // Send the user's message to the AI model.
                val response = chat.sendMessage(userMessage)

                // Replace the pending user message with a confirmed one (isPending = false).
                _uiState.value.replaceLastPendingMessage()

                // If the model provides a response text, add it to the chat UI.
                response.text?.let { modelResponse ->
                    _uiState.value.addMessage(
                        ChatBotMessage(
                            text = modelResponse,
                            participant = ChatBotMessageSender.MODEL,
                            isPending = false
                        )
                    )
                }
            } catch (e: Exception) {
                // If an error occurs during the API call:
                // 1. Replace the pending user message.
                _uiState.value.replaceLastPendingMessage()
                _uiState.value.addMessage(
                    ChatBotMessage(
                        text = e.localizedMessage?.toString() ?: "Something went wrong",
                        participant = ChatBotMessageSender.ERROR
                    )
                )
            }
        }
    }
}