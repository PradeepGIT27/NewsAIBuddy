package com.support.tech.newsaibuddy.data.datasource.news

import com.support.tech.newsaibuddy.data.api.ApiService
import com.support.tech.newsaibuddy.data.remote.dto.NewsResponseDto
import retrofit2.Response
import javax.inject.Inject

/**
 * Concrete implementation of [NewsDataSource] backed by Retrofit [ApiService].
 */
class NewsDataSourceImpl @Inject constructor(
    private val apiService: ApiService
) : NewsDataSource {

    override suspend fun getTopHeadlinesBasedOnCountry(country: String): Response<NewsResponseDto> {
        return apiService.getTopHeadlinesBasedOnCountry(country)
    }

    override suspend fun getTopHeadlineBasedOnCategory(category: String): Response<NewsResponseDto> {
        return apiService.getTopHeadlinesBasedOnCategory(category)
    }
}
