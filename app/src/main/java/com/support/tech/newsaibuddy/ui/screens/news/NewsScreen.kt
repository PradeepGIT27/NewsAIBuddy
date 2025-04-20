package com.support.tech.newsaibuddy.ui.screens.news

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.ui.components.AppLoader
import com.support.tech.newsaibuddy.ui.components.EmptyStateComponent
import com.support.tech.newsaibuddy.ui.components.NewsCardView
import com.support.tech.newsaibuddy.ui.viewmodel.NewsViewmodel

@Composable
fun NewsScreen(
    newsViewModel: NewsViewmodel = hiltViewModel()
) {
    val tabs = listOf(
        "Business",
        "Entertainment",
        "General",
        "Health",
        "Science",
        "Sports",
        "Technology"
    )
    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }
    Column {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            containerColor = Color.White,
            contentColor = Color.Gray,
            indicator = { tabPositions ->
                TabRowDefaults.PrimaryIndicator(
                    color = Color.Black,
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .fillMaxWidth()
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        when (selectedTabIndex) {
                            0 -> newsViewModel.getNewsByCategory("business")
                            1 -> newsViewModel.getNewsByCategory("entertainment")
                            2 -> newsViewModel.getNewsByCategory("general")
                            3 -> newsViewModel.getNewsByCategory("health")
                            4 -> newsViewModel.getNewsByCategory("science")
                            5 -> newsViewModel.getNewsByCategory("sports")
                            6 -> newsViewModel.getNewsByCategory("technology")
                        }
                    },
                    modifier = Modifier.padding(8.dp),
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tab,
                                modifier = Modifier
                                    .padding(8.dp),
                                color = if (selectedTabIndex == index) Color.Black else Color.Gray
                            )
                        }
                    }
                )
            }
        }

        val newsRes = when (selectedTabIndex) {
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
        when (val currentNews = newsRes.value) {
            is ResourceState.Loading -> {
                AppLoader()
            }

            is ResourceState.Success -> {
                val response = currentNews.data
                val articles = response.articles
                if (articles.isEmpty()) {
                    EmptyStateComponent()
                } else {
                    NewsCardView(articles)
                }
            }

            is ResourceState.Error -> {
                EmptyStateComponent()
                Log.d("Logger", "Error fetching news: ${currentNews.error}")
            }
        }
    }
}