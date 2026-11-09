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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.domain.model.NewsCategory
import com.support.tech.newsaibuddy.ui.components.AppLoader
import com.support.tech.newsaibuddy.ui.components.EmptyStateComponent
import com.support.tech.newsaibuddy.ui.components.NewsCardView
import com.support.tech.newsaibuddy.ui.theme.appColor40
import com.support.tech.newsaibuddy.ui.theme.appColor80
import com.support.tech.newsaibuddy.ui.viewmodel.news.NewsViewmodel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NewsScreen(
    navController: NavController,
    newsViewModel: NewsViewmodel = hiltViewModel()
) {
    val categories = NewsCategory.entries
    val pagerState = rememberPagerState(initialPage = 0) { categories.size }
    val coroutineScope = rememberCoroutineScope()
    val uiState by newsViewModel.uiState.collectAsState()

    LaunchedEffect(pagerState.currentPage) {
        val category = categories[pagerState.currentPage].categoryValue
        newsViewModel.getNewsByCategory(category)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("chatBotScreen")
                },
                containerColor = appColor80,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Face, "Add chat")
            }
        }
    ) {
        Column {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                containerColor = Color.White,
                contentColor = Color.Gray,
                indicator = { tabPositions ->
                    TabRowDefaults.PrimaryIndicator(
                        color = appColor40,
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .fillMaxWidth()
                    )
                }
            ) {
                categories.forEachIndexed { index, category ->
                    val isSelected = pagerState.currentPage == index
                    Tab(
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            newsViewModel.getNewsByCategory(category.categoryValue)
                        },
                        modifier = Modifier.padding(8.dp),
                        content = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.displayName,
                                    modifier = Modifier.padding(8.dp),
                                    color = if (isSelected) Color.Black else Color.Gray
                                )
                            }
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val category = categories[page].categoryValue
                val resourceState = uiState.getStateForCategory(category)

                when (resourceState) {
                    is ResourceState.Loading -> {
                        AppLoader()
                    }
                    is ResourceState.Success -> {
                        val articles = resourceState.data
                        if (articles.isEmpty()) {
                            EmptyStateComponent()
                        } else {
                            NewsCardView(articles, navController)
                        }
                    }
                    is ResourceState.Error -> {
                        EmptyStateComponent()
                        Log.d("NewsScreen", "Error fetching news for $category: ${resourceState.error}")
                    }
                }
            }
        }
    }
}
