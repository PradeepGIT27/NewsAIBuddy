package com.support.tech.newsaibuddy.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.chatHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_history")

/**
 * Serializable chat message entry for persistence in DataStore.
 */
@Serializable
data class SerializableChatMessage(
    val id: String = "",
    val text: String = "",
    val participant: String = ChatBotMessageSender.USER.name,
    val isPending: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class representing a saved conversation thread in local storage.
 */
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

/**
 * Converts a domain [ChatBotMessage] into a [SerializableChatMessage].
 */
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
 * Manages chat history persistence using DataStore Preferences.
 * Stores conversation threads locally on device so they survive app restarts.
 */
class ChatHistoryManager(private val context: Context) {

    companion object {
        private val KEY_CONVERSATIONS = stringPreferencesKey("saved_conversations_json")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val conversations: Flow<List<ChatConversation>> = context.chatHistoryDataStore.data.map { prefs ->
        val jsonString = prefs[KEY_CONVERSATIONS] ?: "[]"
        try {
            json.decodeFromString<List<ChatConversation>>(jsonString)
                .sortedByDescending { it.updatedAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveOrUpdateConversation(conversation: ChatConversation) {
        context.chatHistoryDataStore.edit { prefs ->
            val currentJson = prefs[KEY_CONVERSATIONS] ?: "[]"
            val currentList = try {
                json.decodeFromString<MutableList<ChatConversation>>(currentJson)
            } catch (_: Exception) {
                mutableListOf()
            }

            val index = currentList.indexOfFirst { it.id == conversation.id }
            if (index >= 0) {
                currentList[index] = conversation
            } else {
                currentList.add(0, conversation)
            }

            prefs[KEY_CONVERSATIONS] = json.encodeToString(currentList)
        }
    }

    suspend fun deleteConversation(conversationId: String) {
        context.chatHistoryDataStore.edit { prefs ->
            val currentJson = prefs[KEY_CONVERSATIONS] ?: "[]"
            val currentList = try {
                json.decodeFromString<MutableList<ChatConversation>>(currentJson)
            } catch (_: Exception) {
                mutableListOf()
            }

            currentList.removeAll { it.id == conversationId }
            prefs[KEY_CONVERSATIONS] = json.encodeToString(currentList)
        }
    }

    suspend fun clearAllHistory() {
        context.chatHistoryDataStore.edit { prefs ->
            prefs[KEY_CONVERSATIONS] = "[]"
        }
    }
}
