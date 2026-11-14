package com.support.tech.newsaibuddy.data.local

import android.content.Context
import androidx.room.Room
import com.support.tech.newsaibuddy.data.local.db.AppDatabase
import com.support.tech.newsaibuddy.data.local.db.ArticleEntity
import com.support.tech.newsaibuddy.data.local.db.NewsDao
import com.support.tech.newsaibuddy.domain.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Manages saved articles persistence using Room Database safely on [Dispatchers.IO].
 * Articles survive app close/open and are managed through [NewsDao].
 */
class SavedArticlesManager(private val newsDao: NewsDao) {

    constructor(context: Context) : this(
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "news_ai_buddy.db"
        ).fallbackToDestructiveMigration().build().newsDao()
    )

    val savedArticles: Flow<List<Article>> = newsDao.getSavedArticles().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun saveArticles(articles: List<Article>) = withContext(Dispatchers.IO) {
        val entities = articles.map { ArticleEntity.fromDomain(it) }
        newsDao.insertArticles(entities)
    }

    suspend fun addArticle(article: Article) = withContext(Dispatchers.IO) {
        newsDao.insertArticles(listOf(ArticleEntity.fromDomain(article)))
    }

    suspend fun removeArticle(article: Article) = withContext(Dispatchers.IO) {
        val targetUrl = article.url?.takeIf { it.isNotBlank() }
            ?: article.title?.takeIf { it.isNotBlank() }
        if (targetUrl != null) {
            newsDao.deleteArticleByUrl(targetUrl)
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        newsDao.clearAllSavedArticles()
    }
}
