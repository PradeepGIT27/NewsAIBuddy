package com.support.tech.newsaibuddy.data.datasource

import com.support.tech.newsaibuddy.data.api.ApiService
import com.support.tech.newsaibuddy.data.entity.NewsResponse
import retrofit2.Response
import javax.inject.Inject

class NewsDataSourceImpl @Inject constructor(private val apiService: ApiService) : NewsDataSource {
    override suspend fun getTopHeadlines(country: String, apiKey: String): Response<NewsResponse> {
        return apiService.getTopHeadlines(country, apiKey)
    }
}