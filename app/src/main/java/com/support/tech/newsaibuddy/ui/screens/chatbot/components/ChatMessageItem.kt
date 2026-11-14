package com.support.tech.newsaibuddy.ui.screens.chatbot.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.support.tech.newsaibuddy.data.entity.ChatBotMessage
import com.support.tech.newsaibuddy.data.entity.ChatBotMessageSender
import com.support.tech.newsaibuddy.ui.theme.BotAvatarBg
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.RustSoftBg
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.theme.UserBubbleBackground
import com.support.tech.newsaibuddy.ui.theme.UserBubbleBorder

@Composable
fun ChatBubbleItem(
    message: ChatBotMessage,
    isTtsSpeaking: Boolean = false,
    isTtsPaused: Boolean = false,
    onTtsClick: () -> Unit = {},
    onReaderModeClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when (message.participant) {
            ChatBotMessageSender.USER -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
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
                            FormattedMarkdownText(
                                text = message.text,
                                textColor = TextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            ChatBotMessageSender.MODEL -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
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
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTtsSpeaking) RustSoftBg else CardBackground
                        ),
                        border = BorderStroke(
                            width = if (isTtsSpeaking || isTtsPaused) 1.5.dp else 1.dp,
                            color = if (isTtsSpeaking || isTtsPaused) RustPrimary else CardBorder
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CHAT BOT (OFFLINE)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RustPrimary
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    if (message.text.isNotBlank() && !message.isPending) {
                                        IconButton(
                                            onClick = onTtsClick,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = when {
                                                    isTtsSpeaking -> Icons.Default.Pause
                                                    isTtsPaused -> Icons.Default.PlayArrow
                                                    else -> Icons.AutoMirrored.Filled.VolumeUp
                                                },
                                                contentDescription = "Listen Response",
                                                tint = if (isTtsSpeaking || isTtsPaused) RustPrimary else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = onReaderModeClick,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ChromeReaderMode,
                                                contentDescription = "Reader Mode",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    if (message.isPending) {
                                        CircularProgressIndicator(
                                            color = RustPrimary,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (message.text.isEmpty()) {
                                Text(
                                    text = "Thinking...",
                                    fontSize = 15.sp,
                                    color = TextMuted,
                                    lineHeight = 22.sp
                                )
                            } else {
                                FormattedMarkdownText(
                                    text = message.text,
                                    textColor = TextPrimary,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            ChatBotMessageSender.ERROR -> {
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
                        FormattedMarkdownText(
                            text = message.text,
                            textColor = Color(0xFF991B1B),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 22.sp
) {
    val lines = text.split("\n")
    var inCodeBlock = false
    val codeBlockLines = mutableListOf<String>()

    Column(modifier = modifier) {
        lines.forEachIndexed { index, rawLine ->
            val trimmedLine = rawLine.trim()

            if (trimmedLine.startsWith("```")) {
                if (inCodeBlock) {
                    val codeContent = codeBlockLines.joinToString("\n")
                    CodeBlockItem(code = codeContent)
                    codeBlockLines.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                return@forEachIndexed
            }

            if (inCodeBlock) {
                codeBlockLines.add(rawLine)
                return@forEachIndexed
            }

            when {
                trimmedLine.matches(Regex("^(\\s*[-*_]){3,}\\s*$")) -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = CardBorder.copy(alpha = 0.6f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                trimmedLine.startsWith("#") -> {
                    val level = trimmedLine.takeWhile { it == '#' }.length
                    val headerText = trimmedLine.removePrefix("#".repeat(level)).trim()
                    val headerFontSize = when (level) {
                        1 -> 18.sp
                        2 -> 16.sp
                        else -> 15.sp
                    }
                    val annotatedHeader = parseInlineMarkdown(headerText, textColor)
                    if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = annotatedHeader,
                        fontSize = headerFontSize,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = (headerFontSize.value + 6).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> {
                    val bulletContent = trimmedLine.drop(2).trim()
                    val annotated = parseInlineMarkdown(bulletContent, textColor)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "•  ",
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = RustPrimary
                        )
                        Text(
                            text = annotated,
                            fontSize = fontSize,
                            color = textColor,
                            lineHeight = lineHeight
                        )
                    }
                }

                trimmedLine.matches(Regex("^\\d+\\.\\s+.*$")) -> {
                    val dotIndex = trimmedLine.indexOf('.')
                    val numberStr = trimmedLine.substring(0, dotIndex + 1)
                    val contentStr = trimmedLine.substring(dotIndex + 1).trim()
                    val annotated = parseInlineMarkdown(contentStr, textColor)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "$numberStr ",
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = RustPrimary
                        )
                        Text(
                            text = annotated,
                            fontSize = fontSize,
                            color = textColor,
                            lineHeight = lineHeight
                        )
                    }
                }

                trimmedLine.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                else -> {
                    val annotatedLine = parseInlineMarkdown(rawLine, textColor)
                    Text(
                        text = annotatedLine,
                        fontSize = fontSize,
                        color = textColor,
                        lineHeight = lineHeight,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }

        if (inCodeBlock && codeBlockLines.isNotEmpty()) {
            CodeBlockItem(code = codeBlockLines.joinToString("\n"))
        }
    }
}

@Composable
private fun CodeBlockItem(code: String) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = code,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFF1F5F9),
            modifier = Modifier.padding(12.dp)
        )
    }
}

fun parseInlineMarkdown(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val pattern = Regex("(\\*{2}|_{2}|\\*|_|`)(.*?)\\1")
        var currentIndex = 0

        pattern.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val delimiter = matchResult.groupValues[1]
            val content = matchResult.groupValues[2]

            if (start > currentIndex) {
                append(text.substring(currentIndex, start))
            }

            when (delimiter) {
                "**", "__" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                        append(content)
                    }
                }
                "*", "_" -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor)) {
                        append(content)
                    }
                }
                "`" -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            background = Color(0xFFF1F5F9),
                            color = Color(0xFF0F172A)
                        )
                    ) {
                        append(" $content ")
                    }
                }
                else -> append(matchResult.value)
            }

            currentIndex = end
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
