package com.support.tech.newsaibuddy.data.remote.mapper

import com.support.tech.newsaibuddy.data.remote.dto.ArticleDto
import com.support.tech.newsaibuddy.domain.model.Article

/**
 * Mapper extension functions converting Network DTOs into Domain Entities.
 */
fun ArticleDto.toDomain(): Article {
    return Article(
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content,
        sourceName = source?.name
    )
}

fun List<ArticleDto>?.toDomain(): List<Article> {
    return this?.map { it.toDomain() } ?: emptyList()
}

