package com.support.tech.newsaibuddy.data.datasource

import com.support.tech.newsaibuddy.data.entity.NewsResponse
import retrofit2.Response

interface NewsDataSource {
    suspend fun getTopHeadlinesBasedOnCountry(country: String, apiKey: String): Response<NewsResponse>
    suspend fun getTopHeadlineBasedOnCategory(category: String): Response<NewsResponse>
}