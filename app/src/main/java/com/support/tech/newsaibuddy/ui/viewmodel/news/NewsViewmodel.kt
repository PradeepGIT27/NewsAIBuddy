package com.support.tech.newsaibuddy.ui.viewmodel.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.data.local.SavedArticlesManager
import com.support.tech.newsaibuddy.domain.model.Article
import com.support.tech.newsaibuddy.domain.model.NewsCategory
import com.support.tech.newsaibuddy.domain.usecase.GetNewsByCategoryUseCase
import com.support.tech.newsaibuddy.utils.TtsManager
import com.support.tech.newsaibuddy.utils.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling news data and TTS audio playback.
 * Adheres to MVVM + StateFlow with Unidirectional Data Flow (UDF).
 * Saved articles are persisted via [SavedArticlesManager] (DataStore-backed).
 */
@HiltViewModel
class NewsViewmodel @Inject constructor(
    private val getNewsByCategoryUseCase: GetNewsByCategoryUseCase,
    private val savedArticlesManager: SavedArticlesManager,
    private val ttsManager: TtsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()
    val ttsState: StateFlow<TtsState> = ttsManager.ttsState

    init {
        getNewsByCategory(NewsCategory.BUSINESS.categoryValue)
        // Load saved articles from persistent storage
        viewModelScope.launch {
            savedArticlesManager.savedArticles.collect { articles ->
                _uiState.update { current ->
                    current.copy(savedArticles = articles)
                }
            }
        }
    }

    /**
     * Toggles TTS Play/Pause for a given news article.
     */
    fun playOrPauseTts(article: Article) {
        val articleId = article.url ?: article.title.hashCode().toString()
        val title = article.title ?: "News story"
        val speechText = listOfNotNull(article.title, article.description, article.content)
            .filter { it.isNotBlank() }
            .joinToString(". ")

        ttsManager.togglePlayPause(articleId, speechText, title)
    }

    fun playTts(id: String, text: String, title: String) {
        ttsManager.togglePlayPause(id, text, title)
    }

    fun pauseTts() {
        ttsManager.pause()
    }

    fun resumeTts() {
        ttsManager.resume()
    }

    fun stopTts() {
        ttsManager.stop()
    }

    fun setTtsSpeechRate(rate: Float) {
        ttsManager.setSpeechRate(rate)
    }

    /**
     * Fetches news based on the provided category.
     * Updates [NewsUiState] reactively via StateFlow.
     */
    fun getNewsByCategory(category: String, forceRefresh: Boolean = false) {
        val categoryKey = category.lowercase()
        val currentCategoryState = _uiState.value.categoryStates[categoryKey]

        if (!forceRefresh && currentCategoryState is ResourceState.Success) {
            return
        }

        viewModelScope.launch {
            getNewsByCategoryUseCase(categoryKey).collectLatest { resourceState ->
                _uiState.update { current ->
                    current.copy(
                        categoryStates = current.categoryStates + (categoryKey to resourceState)
                    )
                }
            }
        }
    }

    /**
     * Toggles bookmark state and persists to DataStore.
     */
    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            val isSaved = _uiState.value.savedArticles.any { it.url == article.url }
            if (isSaved) {
                savedArticlesManager.removeArticle(article)
            } else {
                savedArticlesManager.addArticle(article)
            }
        }
    }

    /**
     * Clears all saved articles from DataStore.
     */
    fun clearAllSavedArticles() {
        viewModelScope.launch {
            savedArticlesManager.clearAll()
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSearchActive() {
        _uiState.update { it.copy(isSearchActive = !it.isSearchActive, searchQuery = "") }
    }
}
