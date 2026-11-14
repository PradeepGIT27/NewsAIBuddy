package com.support.tech.newsaibuddy.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM saved_articles ORDER BY savedAt DESC")
    fun getSavedArticles(): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArticles(articles: List<ArticleEntity>)

    @Query("DELETE FROM saved_articles WHERE url = :url")
    fun deleteArticleByUrl(url: String): Int

    @Query("DELETE FROM saved_articles")
    fun clearAllSavedArticles(): Int
}
