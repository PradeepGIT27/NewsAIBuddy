package com.support.tech.newsaibuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.support.tech.newsaibuddy.data.AppConstants
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.data.entity.NewsResponse
import com.support.tech.newsaibuddy.ui.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewmodel @Inject constructor(private val newsRepository: NewsRepository) : ViewModel() {
    private val _news: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading()) // here in viewmodel can change it

    val news: StateFlow<ResourceState<NewsResponse>> = _news// here not change in ui

    init {
        getNews()
    }

    private fun getNews() {
        viewModelScope.launch(Dispatchers.IO) {
            newsRepository.getTopHeadlines(AppConstants.COUNTRY, AppConstants.API_KEY)
                .collectLatest { newsResponse ->
                    _news.value = newsResponse
                }
        }
    }

}