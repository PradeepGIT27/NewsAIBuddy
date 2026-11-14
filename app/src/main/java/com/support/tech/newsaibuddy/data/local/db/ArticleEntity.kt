package com.support.tech.newsaibuddy.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.support.tech.newsaibuddy.domain.model.Article

@Entity(tableName = "saved_articles")
data class ArticleEntity(
    @PrimaryKey val url: String,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null,
    val sourceName: String? = null,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Article = Article(
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content,
        sourceName = sourceName
    )

    companion object {
        fun fromDomain(article: Article): ArticleEntity {
            val key = article.url?.takeIf { it.isNotBlank() }
                ?: article.title?.takeIf { it.isNotBlank() }
                ?: "article_${System.currentTimeMillis()}_${article.hashCode()}"
            return ArticleEntity(
                url = key,
                author = article.author,
                title = article.title,
                description = article.description,
                urlToImage = article.urlToImage,
                publishedAt = article.publishedAt,
                content = article.content,
                sourceName = article.sourceName
            )
        }
    }
}
