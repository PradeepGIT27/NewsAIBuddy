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

/**
 * ViewModel for handling news data.
 *
 * This ViewModel is responsible for fetching and managing news data for different categories.
 * It uses Hilt for dependency injection to get an instance of [NewsRepository].
 */
@HiltViewModel
class NewsViewmodel @Inject constructor(private val newsRepository: NewsRepository) : ViewModel() {

    // MutableStateFlow for business news. It can be updated within the ViewModel.
    private val _businessNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading()) // here in viewmodel can change it

    // StateFlow for business news. This is exposed to the UI and is immutable from the UI's perspective.
    val businessNews: StateFlow<ResourceState<NewsResponse>> = _businessNews// here not change in ui

    // MutableStateFlow for entertainment news.
    private val _entertainmentNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    // StateFlow for entertainment news.
    val entertainmentNews: StateFlow<ResourceState<NewsResponse>> = _entertainmentNews

    // MutableStateFlow for general news.
    private val _generalNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    // StateFlow for general news.
    val generalNews: StateFlow<ResourceState<NewsResponse>> = _generalNews

    // MutableStateFlow for health news.
    private val _healthNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    // StateFlow for health news.
    val healthNews: StateFlow<ResourceState<NewsResponse>> = _healthNews

    // MutableStateFlow for science news.
    private val _scienceNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    // StateFlow for science news.
    val scienceNews: StateFlow<ResourceState<NewsResponse>> = _scienceNews

    // MutableStateFlow for sports news.
    private val _sportsNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    // StateFlow for sports news.
    val sportsNews: StateFlow<ResourceState<NewsResponse>> = _sportsNews

    // MutableStateFlow for technology news.
    private val _technologyNews: MutableStateFlow<ResourceState<NewsResponse>> =
        MutableStateFlow(ResourceState.Loading())

    // StateFlow for technology news.
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

    /**
     * Fetches news based on the provided category.
     *
     * This function launches a coroutine in the IO dispatcher to fetch news from the [NewsRepository].
     * The fetched news is then collected and assigned to the corresponding MutableStateFlow based on the category.
     */
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