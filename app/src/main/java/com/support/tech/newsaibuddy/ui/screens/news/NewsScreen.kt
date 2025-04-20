package com.support.tech.newsaibuddy.ui.screens.news

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
    val newsRes = newsViewModel.news.collectAsState()

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