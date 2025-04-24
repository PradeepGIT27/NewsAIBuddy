package com.support.tech.newsaibuddy.ui.viewmodel.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _businessNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading()) // here in viewmodel can change it

    val businessNews: StateFlow<ResourceState<NewsResponse>> = _businessNews// here not change in ui

    private val _entertainmentNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    val entertainmentNews: StateFlow<ResourceState<NewsResponse>> = _entertainmentNews

    private val _generalNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    val generalNews: StateFlow<ResourceState<NewsResponse>> = _generalNews

    private val _healthNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    val healthNews: StateFlow<ResourceState<NewsResponse>> = _healthNews

    private val _scienceNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    val scienceNews: StateFlow<ResourceState<NewsResponse>> = _scienceNews

    private val _sportsNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    val sportsNews: StateFlow<ResourceState<NewsResponse>> = _sportsNews

    private val _technologyNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    val technologyNews: StateFlow<ResourceState<NewsResponse>> = _technologyNews


    init {
        getNewsByCategory("business")
    }

//    private fun getNewsByCountry() {
//        viewModelScope.launch(Dispatchers.IO) {
//            newsRepository.getTopHeadlinesBasedOnCountry(AppConstants.COUNTRY, AppConstants.API_KEY)
//                .collectLatest { _ ->
//                    //_news.value = newsResponse
//                }
//        }
//    }

    fun getNewsByCategory(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            newsRepository.getTopHeadlinesBasedOnCategory(
                category
            ).collectLatest { newsResponse ->
                    when (category) {
                        "business" -> _businessNews.value = newsResponse
                        "entertainment" -> _entertainmentNews.value = newsResponse
                        "general" -> _generalNews.value = newsResponse
                        "health" -> _healthNews.value = newsResponse
                        "science" -> _scienceNews.value = newsResponse
                        "sports" -> _sportsNews.value = newsResponse
                        "technology" -> _technologyNews.value = newsResponse
                    }
                }
        }
    }

}