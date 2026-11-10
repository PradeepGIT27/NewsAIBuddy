package com.support.tech.newsaibuddy.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Network Data Transfer Object (DTO) for the NewsAPI top headlines response.
 */
@JsonClass(generateAdapter = true)
data class NewsResponseDto(
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "totalResults") val totalResults: Int? = null,
    @param:Json(name = "articles") val articles: List<ArticleDto>? = null
)

@JsonClass(generateAdapter = true)
data class ArticleDto(
    @param:Json(name = "author") val author: String? = null,
    @param:Json(name = "title") val title: String? = null,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "url") val url: String? = null,
    @param:Json(name = "urlToImage") val urlToImage: String? = null,
    @param:Json(name = "publishedAt") val publishedAt: String? = null,
    @param:Json(name = "content") val content: String? = null,
    @param:Json(name = "source") val source: SourceDto? = null
)

@JsonClass(generateAdapter = true)
data class SourceDto(
    @param:Json(name = "id") val id: String? = null,
    @param:Json(name = "name") val name: String? = null
)

