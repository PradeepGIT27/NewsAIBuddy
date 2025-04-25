package com.support.tech.newsaibuddy.data.api

import com.support.tech.newsaibuddy.BuildConfig
import com.support.tech.newsaibuddy.data.entity.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {
    @GET("v2/top-headlines")
    suspend fun getTopHeadlinesBasedOnCountry(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String
    ): Response<NewsResponse>

    @Headers("X-Api-Key: ${BuildConfig.NEWS_API_KEY}")
    @GET("v2/top-headlines")
    suspend fun getTopHeadlinesBasedOnCategory(
        @Query("category") country: String,
    ): Response<NewsResponse>
}