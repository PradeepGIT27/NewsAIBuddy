package com.support.tech.newsaibuddy.data.datasource.news

import com.support.tech.newsaibuddy.data.api.ApiService
import com.support.tech.newsaibuddy.data.entity.NewsResponse
import retrofit2.Response
import javax.inject.Inject

class NewsDataSourceImpl @Inject constructor(private val apiService: ApiService) : NewsDataSource {
    override suspend fun getTopHeadlinesBasedOnCountry(country: String, apiKey: String): Response<NewsResponse> {
        return apiService.getTopHeadlinesBasedOnCountry(country, apiKey)
    }

    override suspend fun getTopHeadlineBasedOnCategory(
        category: String,
    ): Response<NewsResponse> {
        return apiService.getTopHeadlinesBasedOnCategory(category)
    }
}