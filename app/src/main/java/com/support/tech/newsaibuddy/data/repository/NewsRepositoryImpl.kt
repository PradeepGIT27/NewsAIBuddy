package com.support.tech.newsaibuddy.data.repository

import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.data.datasource.news.NewsDataSource
import com.support.tech.newsaibuddy.data.remote.mapper.toDomain
import com.support.tech.newsaibuddy.domain.model.Article
import com.support.tech.newsaibuddy.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Repository implementation residing in the Data layer.
 * Coordinates data operations, fetches DTOs from [NewsDataSource],
 * transforms them into Domain entities via mappers, and emits reactive [ResourceState] flows.
 */
class NewsRepositoryImpl @Inject constructor(
    private val newsDataSource: NewsDataSource
) : NewsRepository {

    override fun getTopHeadlinesByCategory(category: String): Flow<ResourceState<List<Article>>> {
        return flow {
            emit(ResourceState.Loading())
            val response = newsDataSource.getTopHeadlineBasedOnCategory(category)
            if (response.isSuccessful && response.body() != null) {
                val articles = response.body()!!.articles.toDomain()
                emit(ResourceState.Success(articles))
            } else {
                emit(ResourceState.Error("Error fetching news: ${response.message()}"))
            }
        }.catch { e ->
            emit(ResourceState.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override fun getTopHeadlinesByCountry(country: String): Flow<ResourceState<List<Article>>> {
        return flow {
            emit(ResourceState.Loading())
            val response = newsDataSource.getTopHeadlinesBasedOnCountry(country)
            if (response.isSuccessful && response.body() != null) {
                val articles = response.body()!!.articles.toDomain()
                emit(ResourceState.Success(articles))
            } else {
                emit(ResourceState.Error("Error fetching news: ${response.message()}"))
            }
        }.catch { e ->
            emit(ResourceState.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}

