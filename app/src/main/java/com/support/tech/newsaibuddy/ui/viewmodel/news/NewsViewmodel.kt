package com.support.tech.newsaibuddy.ui.viewmodel.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.domain.model.NewsCategory
import com.support.tech.newsaibuddy.domain.usecase.GetNewsByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling news data.
 * Adheres to MVVM + StateFlow with Unidirectional Data Flow (UDF).
 * Injects Domain UseCase instead of depending directly on Data layer repositories.
 */
@HiltViewModel
class NewsViewmodel @Inject constructor(
    private val getNewsByCategoryUseCase: GetNewsByCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        getNewsByCategory(NewsCategory.BUSINESS.categoryValue)
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
}
