package com.support.tech.newsaibuddy.domain.usecase

import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.domain.model.Article
import com.support.tech.newsaibuddy.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to fetch top headline articles for a specific news category.
 */
class GetNewsByCategoryUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(category: String): Flow<ResourceState<List<Article>>> {
        return newsRepository.getTopHeadlinesByCategory(category)
    }
}

