package com.support.tech.newsaibuddy.data.api

import com.support.tech.newsaibuddy.data.AppConstants
import com.support.tech.newsaibuddy.data.entity.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Interface defining the API endpoints for fetching news data.
 */
interface ApiService {
    /**
     * Fetches top headlines based on a specific country.
     * @param country The country code (e.g., "us", "in").
     * @param apiKey Your NewsAPI key.
     * @return A Retrofit Response containing NewsResponse data.
     */
    @GET("v2/top-headlines")
    suspend fun getTopHeadlinesBasedOnCountry(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String
    ): Response<NewsResponse>

    /**
     * Fetches top headlines based on a specific category.
     * The API key is injected via a header.
     * @param country The category name (e.g., "business", "technology"). Note: The parameter name is "country" due to API consistency, but it represents a category.
     * @return A Retrofit Response containing NewsResponse data.
     */
    @Headers("X-Api-Key: ${AppConstants.API_KEY}")
    @GET("v2/top-headlines")
    suspend fun getTopHeadlinesBasedOnCategory(
        @Query("category") country: String,
    ): Response<NewsResponse>
}