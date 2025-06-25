package com.support.tech.newsaibuddy.ui.repository

// Import necessary classes and interfaces
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.data.datasource.news.NewsDataSource
import com.support.tech.newsaibuddy.data.entity.NewsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

// NewsRepository class is responsible for fetching news data from the data source
class NewsRepository @Inject constructor(private val newsDataSource: NewsDataSource) {

    // Function to get top headlines based on country
    fun getTopHeadlinesBasedOnCountry(country: String, apiKey: String): Flow<ResourceState<NewsResponse>> {
        // Create a flow to emit ResourceState objects
        return flow {
            // Emit Loading state
            emit(ResourceState.Loading())

            // Fetch data from the data source
            val response = newsDataSource.getTopHeadlinesBasedOnCountry(country, apiKey)

            // Check if the response is successful and the body is not null
            if (response.isSuccessful && response.body() != null) {
                // Emit Success state with the response body
                emit(ResourceState.Success(response.body()!!))
            } else {
                // Emit Error state with an error message
                emit(ResourceState.Error("Error fetching data"))
            }
        }.catch { e ->
            // Catch any exceptions and emit Error state with the localized message or a default message
            emit(ResourceState.Error(e.localizedMessage ?: "Some error in flow"))
        }
    }

    // Function to get top headlines based on category
    fun getTopHeadlinesBasedOnCategory(category: String): Flow<ResourceState<NewsResponse>> {
        // Create a flow to emit ResourceState objects
        return flow {
            // Emit Loading state
            emit(ResourceState.Loading())

            // Fetch data from the data source
            val response = newsDataSource.getTopHeadlineBasedOnCategory(category)

            // Check if the response is successful and the body is not null
            if (response.isSuccessful && response.body() != null) {
                // Emit Success state with the response body
                emit(ResourceState.Success(response.body()!!))

            } else {
                // Emit Error state with an error message
                emit(ResourceState.Error("Error fetching data"))
            }
        }.catch { e ->
            // Catch any exceptions and emit Error state with the localized message or a default message
            emit(ResourceState.Error(e.localizedMessage ?: "Some error in flow"))
        }
    }
}