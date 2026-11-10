package com.support.tech.newsaibuddy.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.support.tech.newsaibuddy.domain.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.savedArticlesDataStore: DataStore<Preferences> by preferencesDataStore(name = "saved_articles")

/**
 * Serializable wrapper for [Article] to store in DataStore.
 */
@Serializable
data class SerializableArticle(
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null,
    val sourceName: String? = null
)

/**
 * Manages saved articles persistence using DataStore Preferences.
 * Articles survive app close/open and are only removed when manually cleared.
 */
class SavedArticlesManager(private val context: Context) {

    companion object {
        private val KEY_SAVED_ARTICLES = stringPreferencesKey("saved_articles_json")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val savedArticles: Flow<List<Article>> = context.savedArticlesDataStore.data.map { prefs ->
        val jsonString = prefs[KEY_SAVED_ARTICLES] ?: "[]"
        try {
            json.decodeFromString<List<SerializableArticle>>(jsonString).map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveArticles(articles: List<Article>) {
        val serializable = articles.map { it.toSerializable() }
        val jsonString = json.encodeToString(serializable)
        context.savedArticlesDataStore.edit { prefs ->
            prefs[KEY_SAVED_ARTICLES] = jsonString
        }
    }

    suspend fun addArticle(article: Article) {
        context.savedArticlesDataStore.edit { prefs ->
            val current = prefs[KEY_SAVED_ARTICLES] ?: "[]"
            val list = try {
                json.decodeFromString<MutableList<SerializableArticle>>(current)
            } catch (e: Exception) {
                mutableListOf()
            }
            if (list.none { it.url == article.url }) {
                list.add(article.toSerializable())
            }
            prefs[KEY_SAVED_ARTICLES] = json.encodeToString(list)
        }
    }

    suspend fun removeArticle(article: Article) {
        context.savedArticlesDataStore.edit { prefs ->
            val current = prefs[KEY_SAVED_ARTICLES] ?: "[]"
            val list = try {
                json.decodeFromString<MutableList<SerializableArticle>>(current)
            } catch (e: Exception) {
                mutableListOf()
            }
            list.removeAll { it.url == article.url }
            prefs[KEY_SAVED_ARTICLES] = json.encodeToString(list)
        }
    }

    suspend fun clearAll() {
        context.savedArticlesDataStore.edit { prefs ->
            prefs[KEY_SAVED_ARTICLES] = "[]"
        }
    }
}

private fun Article.toSerializable() = SerializableArticle(
    author = author,
    title = title,
    description = description,
    url = url,
    urlToImage = urlToImage,
    publishedAt = publishedAt,
    content = content,
    sourceName = sourceName
)

private fun SerializableArticle.toDomain() = Article(
    author = author,
    title = title,
    description = description,
    url = url,
    urlToImage = urlToImage,
    publishedAt = publishedAt,
    content = content,
    sourceName = sourceName
)
