package com.support.tech.newsaibuddy.data.entity

import java.util.UUID

enum class ChatBotMessageSender {
    USER,
    MODEL,
    ERROR
}

data class ChatBotMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val details: String = "",
    val participant: ChatBotMessageSender = ChatBotMessageSender.USER,
    var isPending: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)