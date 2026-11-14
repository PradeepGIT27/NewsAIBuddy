package com.support.tech.newsaibuddy.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.support.tech.newsaibuddy.domain.model.Article
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.RustSoftBg
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.utils.TtsState
import com.support.tech.newsaibuddy.utils.TtsStatus
import java.nio.charset.StandardCharsets

/**
 * Creates an animated linear gradient brush representing a shimmer effect.
 */
@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        )

        val transition = rememberInfiniteTransition(label = "ShimmerTransition")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ShimmerTranslate"
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnimation.value - 300f, translateAnimation.value - 300f),
            end = Offset(translateAnimation.value, translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

/**
 * Image composable that displays an animated shimmer gradient while loading,
 * leverages Coil memory & disk caching, and shows a fallback placeholder icon on error.
 */
@Composable
fun ShimmerAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val context = LocalContext.current
    val imageRequest = remember(model) {
        ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.clip(shape),
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(shimmerBrush())
            )
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = "Failed to load image",
                    tint = TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    )
}

/**
 * Editorial News Card:
 * - Tap card to toggle expansion
 * - Displays TTS Listen (Play/Pause/Resume) button & Reader Mode button
 * - When expanded, displays image (with animated Shimmer placeholder & caching) and article details
 * - Bottom "Reader Mode" and "Read web article" action buttons
 */
@Composable
fun NewsArticleCard(
    article: Article,
    isSaved: Boolean,
    isTtsSpeaking: Boolean = false,
    isTtsPaused: Boolean = false,
    onBookmarkClick: () -> Unit,
    onTtsClick: () -> Unit = {},
    onReaderModeClick: () -> Unit = {},
    onReadFullArticleClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        onClick = { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTtsSpeaking) RustSoftBg else CardBackground
        ),
        border = BorderStroke(
            width = if (isTtsSpeaking || isTtsPaused) 1.5.dp else 1.dp,
            color = if (isTtsSpeaking || isTtsPaused) RustPrimary else CardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Source name, TTS Button, Reader Mode Button, Expand chevron & Bookmark icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.sourceName ?: "Top Story",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RustPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // TTS Listen / Play / Pause Button
                    IconButton(
                        onClick = onTtsClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isTtsSpeaking -> Icons.Default.Pause
                                isTtsPaused -> Icons.Default.PlayArrow
                                else -> Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = if (isTtsSpeaking) "Pause Speech" else "Listen to News (TTS)",
                            tint = if (isTtsSpeaking || isTtsPaused) RustPrimary else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Reader Mode Launch Button
                    IconButton(
                        onClick = onReaderModeClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ChromeReaderMode,
                            contentDescription = "Reader Mode",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Bookmark Icon Button
                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove Bookmark" else "Save Article",
                            tint = if (isSaved) RustPrimary else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Headline
            Text(
                text = article.title.orEmpty(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 24.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            // When expanded: show the Article Image with Shimmer if available
            if (isExpanded) {
                if (!article.urlToImage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ShimmerAsyncImage(
                        model = article.urlToImage,
                        contentDescription = article.title,
                        contentScale = ContentScale.Crop,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                // Author / Published date metadata if available
                if (!article.author.isNullOrBlank() || !article.publishedAt.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val metadata = listOfNotNull(
                        article.author?.let { "By $it" },
                        article.publishedAt
                    ).joinToString(" • ")
                    Text(
                        text = metadata,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Description summary
            if (!article.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 20.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Expanded content snippet (if distinct from description)
            if (isExpanded && !article.content.isNullOrBlank() && article.content != article.description) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.content,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
            }

            // When expanded: Action buttons ("Reader Mode" & "Read web article")
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reader Mode Button
                    Button(
                        onClick = onReaderModeClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RustSoftBg),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ChromeReaderMode,
                            contentDescription = "Reader Mode",
                            tint = RustPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reader Mode",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RustPrimary
                        )
                    }

                    // Web Article Button
                    Button(
                        onClick = onReadFullArticleClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RustPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Text(
                            text = "Full Article",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowOutward,
                            contentDescription = "Read full article",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable function to display a list of news articles with TTS & Reader Mode support.
 */
@Composable
fun NewsCardView(
    articles: List<Article>,
    navController: NavController,
    ttsState: TtsState = TtsState(),
    isArticleSaved: (Article) -> Boolean = { false },
    onBookmarkClick: (Article) -> Unit = {},
    onTtsClick: (Article) -> Unit = {},
    onReaderModeClick: (Article) -> Unit = {}
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(articles, key = { it.url ?: it.title.hashCode().toString() }) { article ->
            val articleId = article.url ?: article.title.hashCode().toString()
            val isThisSpeaking = ttsState.currentId == articleId && ttsState.status == TtsStatus.SPEAKING
            val isThisPaused = ttsState.currentId == articleId && ttsState.status == TtsStatus.PAUSED

            NewsArticleCard(
                article = article,
                isSaved = isArticleSaved(article),
                isTtsSpeaking = isThisSpeaking,
                isTtsPaused = isThisPaused,
                onBookmarkClick = { onBookmarkClick(article) },
                onTtsClick = { onTtsClick(article) },
                onReaderModeClick = { onReaderModeClick(article) },
                onReadFullArticleClick = {
                    if (!article.url.isNullOrBlank()) {
                        val encodedUrl = android.util.Base64.encodeToString(
                            article.url.toByteArray(StandardCharsets.UTF_8),
                            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
                        )
                        navController.navigate("referenceScreen/$encodedUrl")
                    }
                }
            )
        }
    }
}

/**
 * Centered CircularProgressIndicator with brand rust color.
 */
@Composable
fun AppLoader() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = RustPrimary,
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * Friendly empty state component.
 */
@Composable
fun EmptyStateComponent(message: String = "No stories found") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = TextMuted,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * User-friendly No Internet / Network Error view with icon, title, message, and Retry button.
 */
@Composable
fun NetworkErrorComponent(
    message: String = "No internet connection. Please check your network and try again.",
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = RustSoftBg,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "No Internet",
                        tint = RustPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Internet Connection",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RustPrimary),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Retry",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
