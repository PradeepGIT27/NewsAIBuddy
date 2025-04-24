/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.support.tech.newsaibuddy.ui.viewmodel.chatbot

import androidx.compose.runtime.toMutableStateList
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage

class ChatUiState(
    messages: List<ChatBotMessage> = emptyList()
) {
    private val _messages: MutableList<ChatBotMessage> = messages.toMutableStateList()
    val messages: List<ChatBotMessage> = _messages

    fun addMessage(msg: ChatBotMessage) {
        _messages.add(msg)
    }

    fun replaceLastPendingMessage() {
        val lastMessage = _messages.lastOrNull()
        lastMessage?.let {
            val newMessage = lastMessage.apply { isPending = false }
            _messages.removeAt(_messages.lastIndex)
            _messages.add(newMessage)
        }
    }
}
