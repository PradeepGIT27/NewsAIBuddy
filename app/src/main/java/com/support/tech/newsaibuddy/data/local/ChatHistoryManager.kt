package com.support.tech.newsaibuddy.data.local

import android.content.Context
import androidx.room.Room
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender
import com.support.tech.newsaibuddy.data.local.db.AppDatabase
import com.support.tech.newsaibuddy.data.local.db.ChatDao
import com.support.tech.newsaibuddy.data.local.db.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class SerializableChatMessage(
    val id: String = "",
    val text: String = "",
    val participant: String = ChatBotMessageSender.USER.name,
    val isPending: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ChatConversation(
    val id: String,
    val title: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<SerializableChatMessage> = emptyList(),
    val modelId: String? = null
) {
    fun toDomainMessages(): List<ChatBotMessage> {
        return messages.map { msg ->
            val sender = try {
                ChatBotMessageSender.valueOf(msg.participant)
            } catch (_: Exception) {
                ChatBotMessageSender.USER
            }
            ChatBotMessage(
                id = msg.id,
                text = msg.text,
                participant = sender,
                isPending = msg.isPending,
                timestamp = msg.timestamp
            )
        }
    }
}

fun ChatBotMessage.toSerializable(): SerializableChatMessage {
    return SerializableChatMessage(
        id = id,
        text = text,
        participant = participant.name,
        isPending = isPending,
        timestamp = timestamp
    )
}

/**
 * Manages chat history persistence using Room Database via [ChatDao] on [Dispatchers.IO].
 */
class ChatHistoryManager(private val chatDao: ChatDao) {

    constructor(context: Context) : this(
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "news_ai_buddy.db"
        ).fallbackToDestructiveMigration().build().chatDao()
    )

    val defaultMessages: Flow<List<ChatBotMessage>> = chatDao.getMessagesForConversation("default_conversation").map { entities ->
        entities.map { it.toDomain() }
    }

    val conversations: Flow<List<ChatConversation>> = defaultMessages.map { domainMsgs ->
        if (domainMsgs.isEmpty()) {
            emptyList()
        } else {
            val serializableMsgs = domainMsgs.map { it.toSerializable() }
            val firstUserMsg = domainMsgs.firstOrNull { it.participant == ChatBotMessageSender.USER }?.text ?: "Chat Session"
            val title = if (firstUserMsg.length > 30) firstUserMsg.take(28) + "..." else firstUserMsg
            listOf(
                ChatConversation(
                    id = "default_conversation",
                    title = title,
                    updatedAt = domainMsgs.lastOrNull()?.timestamp ?: System.currentTimeMillis(),
                    messages = serializableMsgs
                )
            )
        }
    }

    suspend fun saveMessages(messages: List<ChatBotMessage>, conversationId: String = "default_conversation") = withContext(Dispatchers.IO) {
        val entities = messages.map { ChatMessageEntity.fromDomain(it, conversationId) }
        chatDao.insertMessages(entities)
    }

    suspend fun saveOrUpdateConversation(conversation: ChatConversation) = withContext(Dispatchers.IO) {
        val domainMessages = conversation.toDomainMessages()
        saveMessages(domainMessages, conversation.id)
    }

    suspend fun deleteConversation(conversationId: String) = withContext(Dispatchers.IO) {
        chatDao.clearConversation(conversationId)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        chatDao.clearConversation("default_conversation")
    }
}
