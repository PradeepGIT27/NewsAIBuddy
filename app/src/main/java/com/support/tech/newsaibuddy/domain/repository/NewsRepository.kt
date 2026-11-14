package com.support.tech.newsaibuddy.domain.repository

import androidx.paging.PagingData
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.domain.model.Article
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for accessing news data.
 * Resides in Domain layer to ensure UI and Business Logic do not depend directly on Data layer implementations.
 */
interface NewsRepository {
    fun getTopHeadlinesByCategory(category: String): Flow<ResourceState<List<Article>>>
    fun getTopHeadlinesByCountry(country: String): Flow<ResourceState<List<Article>>>
    fun getNewsPagingStream(category: String): Flow<PagingData<Article>>
}
