package com.support.tech.newsaibuddy.data.datasource.news

import com.support.tech.newsaibuddy.data.remote.dto.NewsResponseDto
import retrofit2.Response

/**
 * Data source interface defining raw network data fetch operations for news.
 */
interface NewsDataSource {
    suspend fun getTopHeadlinesBasedOnCountry(country: String, page: Int = 1, pageSize: Int = 20): Response<NewsResponseDto>
    suspend fun getTopHeadlineBasedOnCategory(category: String, page: Int = 1, pageSize: Int = 20): Response<NewsResponseDto>
}
