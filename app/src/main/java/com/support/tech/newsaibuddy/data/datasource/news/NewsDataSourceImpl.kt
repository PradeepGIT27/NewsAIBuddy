package com.support.tech.newsaibuddy.data.datasource.news

import com.support.tech.newsaibuddy.data.api.ApiService
import com.support.tech.newsaibuddy.data.entity.NewsResponse
import retrofit2.Response
import javax.inject.Inject
/**
 * Implementation of [NewsDataSource] that fetches news data from an [ApiService].
 *
 * @property apiService The [ApiService] instance used to make network requests.
 */
class NewsDataSourceImpl @Inject constructor(private val apiService: ApiService) : NewsDataSource {
    /**
     * Retrieves top headlines based on the specified country and API key.
     *
     * @param country The country for which to fetch top headlines.
     * @param apiKey The API key for accessing the news service.
     * @return A [Response] containing the [NewsResponse] if successful, or an error response.
     */
    override suspend fun getTopHeadlinesBasedOnCountry(country: String, apiKey: String): Response<NewsResponse> {
        return apiService.getTopHeadlinesBasedOnCountry(country, apiKey)
    }
    /**
     * Retrieves top headlines based on the specified category.
     *
     * @param category The category for which to fetch top headlines.
     * @return A [Response] containing the [NewsResponse] if successful, or an error response.
     */
    override suspend fun getTopHeadlineBasedOnCategory(
        category: String,
    ): Response<NewsResponse> {
        return apiService.getTopHeadlinesBasedOnCategory(category)
    }
}