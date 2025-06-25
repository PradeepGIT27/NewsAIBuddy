package com.support.tech.newsaibuddy.ui.screens.news

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.ui.components.AppLoader
import com.support.tech.newsaibuddy.ui.components.EmptyStateComponent
import com.support.tech.newsaibuddy.ui.components.NewsCardView
import com.support.tech.newsaibuddy.ui.theme.appColor40
import com.support.tech.newsaibuddy.ui.theme.appColor80
import com.support.tech.newsaibuddy.ui.viewmodel.news.NewsViewmodel
import kotlinx.coroutines.launch

// Suppress unused material 3 scaffold padding parameter warning
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
// Composable function for the news screen
@Composable
fun NewsScreen(
    navController: NavController,
    newsViewModel: NewsViewmodel = hiltViewModel()
) {
    // List of news categories
    val newsCategory = listOf(
        "Business", "Entertainment", "General", "Health", "Science", "Sports", "Technology"
    )
    // Number of pages in the pager
    val pageCount = newsCategory.size
    // State for the horizontal pager
    val pagerState = rememberPagerState(initialPage = 0) { pageCount }
    // State for the selected tab index
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Coroutine scope for launching coroutines
    val coroutineScope = rememberCoroutineScope()

    // Effect to fetch news when the current page changes
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
        fetchNewsIfNeeded(selectedTabIndex, newsCategory, newsViewModel)
    }

    // Scaffold for the screen layout
    Scaffold(   floatingActionButton = {
        // Floating action button to navigate to the chatbot screen
        FloatingActionButton(
            onClick = {
               navController.navigate("chatBotScreen")
            },
            containerColor = appColor80,
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Face, "Add chat")
        }
    }) {
        // Column to arrange elements vertically
        Column {
            ScrollableTabRow(
                // Selected tab index
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = Color.White,
                contentColor = Color.Gray,
                indicator = { tabPositions ->
                    TabRowDefaults.PrimaryIndicator(
                        color = appColor40,
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .fillMaxWidth()
                    )
                }) {
                // Iterate over news categories to create tabs
                newsCategory.forEachIndexed { index, tab ->
                    Tab(selected = selectedTabIndex == index, onClick = {
                        selectedTabIndex = index
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                        fetchNewsIfNeeded(selectedTabIndex, newsCategory, newsViewModel)
                    }, modifier = Modifier.padding(8.dp), content = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically // Center content vertically
                        ) {
                            // Text for the tab
                            Text(
                                text = tab,
                                // Padding around the text
                                modifier = Modifier.padding(8.dp),
                                color = if (selectedTabIndex == index) Color.Black else Color.Gray
                            )
                        }
                    })
                }
            }

            // Horizontal pager to display news content for each category
            HorizontalPager(
                state = pagerState, modifier = Modifier.fillMaxWidth()
            ) { page ->
                // Collect news resource state based on the current page
                val newsRes = when (page) {
                    0 -> newsViewModel.businessNews.collectAsState()
                    1 -> newsViewModel.entertainmentNews.collectAsState()
                    2 -> newsViewModel.generalNews.collectAsState()
                    3 -> newsViewModel.healthNews.collectAsState()
                    4 -> newsViewModel.scienceNews.collectAsState()
                    5 -> newsViewModel.sportsNews.collectAsState()
                    6 -> newsViewModel.technologyNews.collectAsState()
                    else -> {
                        newsViewModel.businessNews.collectAsState()
                    }
                }
                // Handle different states of news resource
                when (val currentNews = newsRes.value) {
                    // Loading state: display loader
                    is ResourceState.Loading -> {
                        AppLoader()
                    }

                    // Success state: display news articles
                    is ResourceState.Success -> {
                        val response = currentNews.data
                        val articles = response.articles
                        // If no articles, display empty state component
                        if (articles.isEmpty()) {
                            EmptyStateComponent()
                        } else {
                            // Display news card view with articles
                            NewsCardView(articles, navController)
                        }
                    }

                    // Error state: display empty state component and log error
                    is ResourceState.Error -> {
                        EmptyStateComponent()
                        Log.d("Logger", "Error fetching news: ${currentNews.error}")
                    }
                    // Add a comment to explain the purpose of this block
                }
            }
        }
    }

}

// Function to fetch news if needed based on the selected tab
private fun fetchNewsIfNeeded(
    selectedTabIndex: Int,
    newsCategory: List<String>,
    newsViewModel: NewsViewmodel
) {
    val shouldFetchNews = when (selectedTabIndex) {
        // Check if news for the selected category has not been fetched successfully
        0 -> newsViewModel.businessNews.value !is ResourceState.Success
        1 -> newsViewModel.entertainmentNews.value !is ResourceState.Success
        2 -> newsViewModel.generalNews.value !is ResourceState.Success
        3 -> newsViewModel.healthNews.value !is ResourceState.Success
        4 -> newsViewModel.scienceNews.value !is ResourceState.Success
        5 -> newsViewModel.sportsNews.value !is ResourceState.Success
        6 -> newsViewModel.technologyNews.value !is ResourceState.Success
        else -> false
    }
    // If news needs to be fetched, call the view model function
    if (shouldFetchNews) {
        newsViewModel.getNewsByCategory(newsCategory[selectedTabIndex].lowercase())
    }
}
