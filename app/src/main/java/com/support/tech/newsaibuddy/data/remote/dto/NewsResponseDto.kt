package com.support.tech.newsaibuddy.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Network Data Transfer Object (DTO) for the NewsAPI top headlines response.
 */
@JsonClass(generateAdapter = true)
data class NewsResponseDto(
    @Json(name = "status") val status: String? = null,
    @Json(name = "totalResults") val totalResults: Int? = null,
    @Json(name = "articles") val articles: List<ArticleDto>? = null
)

@JsonClass(generateAdapter = true)
data class ArticleDto(
    @Json(name = "author") val author: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "urlToImage") val urlToImage: String? = null,
    @Json(name = "publishedAt") val publishedAt: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "source") val source: SourceDto? = null
)

@JsonClass(generateAdapter = true)
data class SourceDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null
)

