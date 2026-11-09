package com.support.tech.newsaibuddy.data.datasource.news

import com.support.tech.newsaibuddy.data.remote.dto.NewsResponseDto
import retrofit2.Response

/**
 * Data source interface defining raw network data fetch operations for news.
 */
interface NewsDataSource {
    suspend fun getTopHeadlinesBasedOnCountry(country: String): Response<NewsResponseDto>
    suspend fun getTopHeadlineBasedOnCategory(category: String): Response<NewsResponseDto>
}
