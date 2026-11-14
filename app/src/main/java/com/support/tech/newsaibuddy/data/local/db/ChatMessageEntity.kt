package com.support.tech.newsaibuddy.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String = "default_conversation",
    val text: String,
    val participant: String,
    val isPending: Boolean,
    val timestamp: Long
) {
    fun toDomain(): ChatBotMessage {
        val sender = try {
            ChatBotMessageSender.valueOf(participant)
        } catch (_: Exception) {
            ChatBotMessageSender.USER
        }
        return ChatBotMessage(
            id = id,
            text = text,
            participant = sender,
            isPending = isPending,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(message: ChatBotMessage, conversationId: String = "default_conversation"): ChatMessageEntity {
            return ChatMessageEntity(
                id = message.id,
                conversationId = conversationId,
                text = message.text,
                participant = message.participant.name,
                isPending = message.isPending,
                timestamp = message.timestamp
            )
        }
    }
}
