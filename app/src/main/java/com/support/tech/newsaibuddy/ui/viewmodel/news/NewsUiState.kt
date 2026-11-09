package com.support.tech.newsaibuddy.ui.viewmodel.news

import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.domain.model.Article

/**
 * Immutable UI State for the News Screen.
 * Holds the map of category keys to their respective [ResourceState].
 */
data class NewsUiState(
    val categoryStates: Map<String, ResourceState<List<Article>>> = emptyMap(),
    val savedArticles: List<Article> = emptyList(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = ""
) {
    /**
     * Helper to retrieve state for a given category.
     * Defaults to [ResourceState.Loading] if the category has not started loading.
     */
    fun getStateForCategory(category: String): ResourceState<List<Article>> {
        return categoryStates[category.lowercase()] ?: ResourceState.Loading()
    }

    fun isArticleSaved(article: Article): Boolean {
        return savedArticles.any { it.url == article.url }
    }
}

