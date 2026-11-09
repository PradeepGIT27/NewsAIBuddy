package com.support.tech.newsaibuddy.data.api

import com.support.tech.newsaibuddy.data.remote.dto.NewsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface defining the API endpoints for fetching news data from NewsAPI.
 * Authentication is automatically injected by [com.support.tech.newsaibuddy.data.remote.interceptor.ApiKeyInterceptor].
 */
interface ApiService {
    /**
     * Fetches top headlines based on a specific country.
     * @param country The country code (e.g. "us", "in").
     */
    @GET("v2/top-headlines")
    suspend fun getTopHeadlinesBasedOnCountry(
        @Query("country") country: String
    ): Response<NewsResponseDto>

    /**
     * Fetches top headlines based on a specific category.
     * @param category The category name (e.g. "business", "technology").
     */
    @GET("v2/top-headlines")
    suspend fun getTopHeadlinesBasedOnCategory(
        @Query("category") category: String
    ): Response<NewsResponseDto>
}
