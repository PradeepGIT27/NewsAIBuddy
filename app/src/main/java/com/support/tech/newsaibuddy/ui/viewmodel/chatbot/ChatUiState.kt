package com.support.tech.newsaibuddy.ui.viewmodel.chatbot

import com.support.tech.newsaibuddy.data.entity.ChatBotMessage

/**
 * Immutable UI state for the chat screen.
 *
 * @param messages The list of messages in the chat.
 */
data class ChatUiState(
    val messages: List<ChatBotMessage> = emptyList()
)
