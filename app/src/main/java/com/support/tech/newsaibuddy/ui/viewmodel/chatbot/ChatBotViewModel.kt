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


@HiltViewModel
class ChatBotViewModel @Inject constructor( generativeModel: GenerativeModel) : ViewModel() {
    private val _chatMessages = mutableStateListOf<ChatBotMessage>()
    val chatMessages: List<ChatBotMessage> = _chatMessages

    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("Hello, ChatBot.") },
            content(role = "model") { text("Great to meet you. What would you like to know?") }
        )
    )

    private val _uiState: MutableStateFlow<ChatUiState> =
        MutableStateFlow(ChatUiState(chat.history.map { content ->
            // Map the initial messages
            ChatBotMessage(
                text = content.parts.first().asTextOrNull() ?: "",
                participant = if (content.role == "user") ChatBotMessageSender.USER else ChatBotMessageSender.MODEL,
                isPending = false
            )
        }))
    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()


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
            try {
                val response = chat.sendMessage(userMessage)

                _uiState.value.replaceLastPendingMessage()

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