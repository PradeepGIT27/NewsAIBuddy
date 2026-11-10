package com.support.tech.newsaibuddy.ui.screens.news

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PersonOutline
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.domain.model.NewsCategory
import com.support.tech.newsaibuddy.ui.components.AppLoader
import com.support.tech.newsaibuddy.ui.components.EmptyStateComponent
import com.support.tech.newsaibuddy.ui.components.NewsCardView
import com.support.tech.newsaibuddy.ui.screens.profile.ProfileScreen
import com.support.tech.newsaibuddy.ui.theme.CardBackground
import com.support.tech.newsaibuddy.ui.theme.CardBorder
import com.support.tech.newsaibuddy.ui.theme.RustPrimary
import com.support.tech.newsaibuddy.ui.theme.RustSoftBg
import com.support.tech.newsaibuddy.ui.theme.ScreenBackground
import com.support.tech.newsaibuddy.ui.theme.TextMuted
import com.support.tech.newsaibuddy.ui.theme.TextPrimary
import com.support.tech.newsaibuddy.ui.theme.TextSecondary
import com.support.tech.newsaibuddy.ui.viewmodel.auth.AuthViewModel
import com.support.tech.newsaibuddy.ui.viewmodel.news.NewsViewmodel
import kotlinx.coroutines.launch

@Composable
fun NewsScreen(
    navController: NavController,
    newsViewModel: NewsViewmodel = hiltViewModel(),
    authViewModel: AuthViewModel
) {
    val categories = NewsCategory.entries
    val pagerState = rememberPagerState(initialPage = 0) { categories.size }
    val coroutineScope = rememberCoroutineScope()
    val uiState by newsViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // 0 = News, 1 = Saved, 2 = Chat Bot, 3 = Profile
    var selectedBottomNavIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(pagerState.currentPage) {
        val category = categories[pagerState.currentPage].categoryValue
        newsViewModel.getNewsByCategory(category)
    }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            FigmaBottomNavigation(
                selectedIndex = selectedBottomNavIndex,
                onItemSelected = { index ->
                    if (index == 2) {
                        navController.navigate("chatBotScreen")
                    } else {
                        selectedBottomNavIndex = index
                    }
                }
            )
        }
    ) { paddingValues ->

        when (selectedBottomNavIndex) {
            3 -> {
                // PROFILE VIEW
                ProfileScreen(
                    authViewModel = authViewModel,
                    savedArticleCount = uiState.savedArticles.size,
                    onClearSavedArticles = { newsViewModel.clearAllSavedArticles() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .statusBarsPadding()
                ) {
                    // Top Section (Eyebrow, Title, Search button)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "A WORLD IN FOCUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp,
                            color = RustPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "The news brief.",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = TextPrimary
                            )

                            // Rounded Search Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CardBackground,
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { newsViewModel.toggleSearchActive() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Search field when active
                        if (uiState.isSearchActive) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { newsViewModel.setSearchQuery(it) },
                                placeholder = { Text("Search stories...", color = TextMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RustPrimary,
                                    unfocusedBorderColor = CardBorder,
                                    focusedContainerColor = CardBackground,
                                    unfocusedContainerColor = CardBackground
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (selectedBottomNavIndex == 1) {
                        // SAVED STORIES VIEW
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Saved Stories",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${uiState.savedArticles.size} stories",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }

                            if (uiState.savedArticles.isEmpty()) {
                                EmptyStateComponent("No saved articles yet. Bookmark stories to read them here!")
                            } else {
                                NewsCardView(
                                    articles = uiState.savedArticles,
                                    navController = navController,
                                    isArticleSaved = { true },
                                    onBookmarkClick = { newsViewModel.toggleBookmark(it) }
                                )
                            }
                        }
                    } else {
                        // NEWS STORIES VIEW (selectedBottomNavIndex == 0)

                        // Category Chips (Horizontal Scrollable)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(scrollState)
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            categories.forEachIndexed { index, category ->
                                val isSelected = pagerState.currentPage == index
                                CategoryChip(
                                    label = category.displayName,
                                    isSelected = isSelected,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                        newsViewModel.getNewsByCategory(category.categoryValue)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Section Header (e.g., "Business" + "4 stories")
                        val currentCategory = categories[pagerState.currentPage]
                        val resourceState = uiState.getStateForCategory(currentCategory.categoryValue)
                        val articleCount = (resourceState as? ResourceState.Success)?.data?.size ?: 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentCategory.displayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Text(
                                text = if (resourceState is ResourceState.Success) "$articleCount stories" else "",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        // Paged News Content
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val pageCategory = categories[page].categoryValue
                            val state = uiState.getStateForCategory(pageCategory)

                            when (state) {
                                is ResourceState.Loading -> AppLoader()
                                is ResourceState.Success -> {
                                    val filteredArticles = if (uiState.searchQuery.isBlank()) {
                                        state.data
                                    } else {
                                        state.data.filter { article ->
                                            article.title?.contains(uiState.searchQuery, ignoreCase = true) == true ||
                                                    article.description?.contains(uiState.searchQuery, ignoreCase = true) == true ||
                                                    article.sourceName?.contains(uiState.searchQuery, ignoreCase = true) == true
                                        }
                                    }

                                    if (filteredArticles.isEmpty()) {
                                        EmptyStateComponent("No stories found in this category")
                                    } else {
                                        NewsCardView(
                                            articles = filteredArticles,
                                            navController = navController,
                                            isArticleSaved = { uiState.isArticleSaved(it) },
                                            onBookmarkClick = { newsViewModel.toggleBookmark(it) }
                                        )
                                    }
                                }
                                is ResourceState.Error -> {
                                    EmptyStateComponent("Failed to load news. Check network connection.")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Category Pill Chip matching Figma design.
 */
@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) RustPrimary else CardBackground,
        border = if (isSelected) null else BorderStroke(1.dp, CardBorder),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp)
        )
    }
}

/**
 * Bottom Navigation Bar:
 * News | Saved | Chat Bot | Profile
 */
@Composable
fun FigmaBottomNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Surface(
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(color = CardBorder, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "News",
                    icon = if (selectedIndex == 0) Icons.AutoMirrored.Filled.Article else Icons.AutoMirrored.Outlined.Article,
                    isSelected = selectedIndex == 0,
                    onClick = { onItemSelected(0) }
                )

                BottomNavItem(
                    label = "Saved",
                    icon = if (selectedIndex == 1) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    isSelected = selectedIndex == 1,
                    onClick = { onItemSelected(1) }
                )

                BottomNavItem(
                    label = "Chat Bot",
                    icon = if (selectedIndex == 2) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                    isSelected = selectedIndex == 2,
                    onClick = { onItemSelected(2) }
                )

                BottomNavItem(
                    label = "Profile",
                    icon = if (selectedIndex == 3) Icons.Filled.Person else Icons.Outlined.PersonOutline,
                    isSelected = selectedIndex == 3,
                    onClick = { onItemSelected(3) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 54.dp, height = 30.dp)
                .background(
                    color = if (isSelected) RustSoftBg else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) RustPrimary else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) RustPrimary else TextSecondary
        )
    }
}
