package com.support.tech.newsaibuddy.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender
import com.support.tech.newsaibuddy.ui.screens.chatbot.components.ChatBubbleItem
import org.junit.Rule
import org.junit.Test

class ChatMessageItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun userMessage_displaysUserTextAndYouTag() {
        val userMsg = ChatBotMessage(
            text = "Hello AI Companion!",
            participant = ChatBotMessageSender.USER
        )

        composeTestRule.setContent {
            ChatBubbleItem(message = userMsg)
        }

        composeTestRule.onNodeWithText("YOU").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hello AI Companion!").assertIsDisplayed()
    }

    @Test
    fun botMessage_displaysBotTextAndOfflineTag() {
        val botMsg = ChatBotMessage(
            text = "Hello! I am your offline assistant.",
            participant = ChatBotMessageSender.MODEL
        )

        composeTestRule.setContent {
            ChatBubbleItem(message = botMsg)
        }

        composeTestRule.onNodeWithText("CHAT BOT (OFFLINE)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hello! I am your offline assistant.").assertIsDisplayed()
    }
}
