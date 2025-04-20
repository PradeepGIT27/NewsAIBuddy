package com.support.tech.newsaibuddy.ui.repository

import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.data.datasource.NewsDataSource
import com.support.tech.newsaibuddy.data.entity.NewsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NewsRepository @Inject constructor(private val newsDataSource: NewsDataSource) {
    fun getTopHeadlinesBasedOnCountry(country: String, apiKey: String): Flow<ResourceState<NewsResponse>> {
        return flow {
            emit(ResourceState.Loading())

            val response = newsDataSource.getTopHeadlinesBasedOnCountry(country, apiKey)

            if (response.isSuccessful && response.body() != null) {

                emit(ResourceState.Success(response.body()!!))

            } else {
                emit(ResourceState.Error("Error fetching data"))
            }
        }.catch { e ->
            emit(ResourceState.Error(e.localizedMessage ?: "Some error in flow"))
        }
    }

    fun getTopHeadlinesBasedOnCategory(category: String): Flow<ResourceState<NewsResponse>> {
        return flow {
            emit(ResourceState.Loading())

            val response = newsDataSource.getTopHeadlineBasedOnCategory(category)

            if (response.isSuccessful && response.body() != null) {

                emit(ResourceState.Success(response.body()!!))

            } else {
                emit(ResourceState.Error("Error fetching data"))
            }
        }.catch { e ->
            emit(ResourceState.Error(e.localizedMessage ?: "Some error in flow"))
        }
    }
}