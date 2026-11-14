package com.support.tech.newsaibuddy.ui.screens.chatbot.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage

@Composable
fun ChatMessageList(
    messages: List<ChatBotMessage>,
    listState: LazyListState,
    ttsSpeakingMessageId: String?,
    ttsPausedMessageId: String?,
    onTtsClick: (ChatBotMessage) -> Unit,
    onReaderModeClick: (ChatBotMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = messages,
            key = { it.id }
        ) { message ->
            ChatBubbleItem(
                message = message,
                isTtsSpeaking = message.id == ttsSpeakingMessageId,
                isTtsPaused = message.id == ttsPausedMessageId,
                onTtsClick = { onTtsClick(message) },
                onReaderModeClick = { onReaderModeClick(message) }
            )
        }
    }
}
