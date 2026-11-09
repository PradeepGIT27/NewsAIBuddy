package com.support.tech.newsaibuddy.ui.screens.chatbot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender
import com.support.tech.newsaibuddy.ui.theme.BotAvatarBg
import com.support.tech.newsaibuddy.ui.theme.BotBadgeBackground
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.ScreenBackground
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.theme.UserBubbleBackground
import com.support.tech.newsaibuddy.ui.theme.UserBubbleBorder
import com.support.tech.newsaibuddy.ui.viewmodel.chatbot.ChatBotViewModel
import kotlinx.coroutines.launch

/**
 * ChatBot Screen matching the Figma design:
 * - Top header with rounded back button, title, subtitle & bot avatar
 * - "NEW CONVERSATION" divider
 * - Distinct styled chat bubbles for User (warm cream) and Bot (white + avatar icon)
 * - "A PLACE TO START" prompt suggestion cards
 * - Rounded input bar with rust upward-arrow send button & AI disclaimer
 */
@Composable
fun ChatBotScreen(
    navController: NavController,
    chatViewModel: ChatBotViewModel = hiltViewModel()
) {
    val chatUiState by chatViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to latest message
    LaunchedEffect(chatUiState.messages.size) {
        if (chatUiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatUiState.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            ChatBotTopBar(onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            ChatBotInputSection(
                onSendMessage = { text ->
                    chatViewModel.sendMessage(text)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // "NEW CONVERSATION" Divider
            NewConversationDivider()

            // Chat messages list
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(chatUiState.messages, key = { it.id }) { message ->
                    ChatBubbleItem(message = message)
                }

                // Starter Prompts section ("A PLACE TO START")
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    PlaceToStartSection(
                        onPromptClick = { prompt ->
                            chatViewModel.sendMessage(prompt)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Top App Bar matching the Figma design:
 * Rounded Back button | Title + Subtitle | Robot Badge
 */
@Composable
private fun ChatBotTopBar(onBackClick: () -> Unit) {
    Surface(
        color = ScreenBackground,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded Back Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardBackground,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .size(42.dp)
                    .clickable(onClick = onBackClick)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Title & Subtitle Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Chat Bot",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Your news companion",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // Robot badge icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BotBadgeBackground,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = "AI Companion",
                        tint = RustPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Centered "NEW CONVERSATION" divider line.
 */
@Composable
private fun NewConversationDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = CardBorder,
            thickness = 1.dp
        )
        Text(
            text = "NEW CONVERSATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = CardBorder,
            thickness = 1.dp
        )
    }
}

/**
 * Message Bubble matching Figma design.
 */
@Composable
fun ChatBubbleItem(message: ChatBotMessage) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when (message.participant) {
            ChatBotMessageSender.USER -> {
                // USER MESSAGE (Right aligned, warm cream container)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (message.isPending) {
                        CircularProgressIndicator(
                            color = RustPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = UserBubbleBackground),
                        border = BorderStroke(1.dp, UserBubbleBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth(0.82f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "YOU",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RustPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            ChatBotMessageSender.MODEL -> {
                // BOT MESSAGE (Left aligned, white container with robot avatar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Small Bot Avatar on the left
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .background(BotAvatarBg, shape = RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = "Bot Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Card(
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CHAT BOT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            ChatBotMessageSender.ERROR -> {
                // ERROR MESSAGE
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "NOTICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            color = Color(0xFF991B1B),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * "A PLACE TO START" prompt suggestions matching Figma design.
 */
@Composable
private fun PlaceToStartSection(onPromptClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = "A PLACE TO START",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        PromptCard(
            text = "Help me understand a headline",
            onClick = { onPromptClick("Help me understand a headline") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PromptCard(
            text = "Explain a business term",
            onClick = { onPromptClick("Explain a business term") }
        )
    }
}

@Composable
private fun PromptCard(
    text: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Icon(
                imageVector = Icons.Default.ArrowOutward,
                contentDescription = null,
                tint = RustPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Bottom Input Bar with Upward Arrow Send Button & Disclaimer.
 */
@Composable
private fun ChatBotInputSection(onSendMessage: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }

    Surface(
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Input Field
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            text = "Enter your message",
                            fontSize = 15.sp,
                            color = TextMuted
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (text.isNotBlank()) {
                                onSendMessage(text.trim())
                                text = ""
                            }
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RustPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Upward Arrow Send Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RustPrimary,
                    modifier = Modifier
                        .size(54.dp)
                        .clickable {
                            if (text.isNotBlank()) {
                                onSendMessage(text.trim())
                                text = ""
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Disclaimer Text
            Text(
                text = "AI can make mistakes. Verify with the original source.",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
