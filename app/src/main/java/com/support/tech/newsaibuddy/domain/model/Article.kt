package com.support.tech.newsaibuddy.domain.model

/**
 * Domain entity representing a news article.
 * Pure business model free of serialization or UI presentation concerns.
 */
data class Article(
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?,
    val sourceName: String?
)

