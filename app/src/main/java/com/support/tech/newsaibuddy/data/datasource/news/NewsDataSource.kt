// Package declaration for the news data source
package com.support.tech.newsaibuddy.data.datasource.news

// Import necessary classes
import com.support.tech.newsaibuddy.data.entity.NewsResponse
import retrofit2.Response

// Interface defining the contract for news data operations
interface NewsDataSource {
    // Suspend function to get top headlines based on country
    // Takes country code and API key as parameters
    // Returns a Response object containing NewsResponse
    suspend fun getTopHeadlinesBasedOnCountry(country: String, apiKey: String): Response<NewsResponse>

    // Suspend function to get top headlines based on category
    // Takes category as a parameter
    // Returns a Response object containing NewsResponse
    suspend fun getTopHeadlineBasedOnCategory(category: String): Response<NewsResponse>
}