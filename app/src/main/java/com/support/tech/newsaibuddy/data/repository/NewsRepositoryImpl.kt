package com.support.tech.newsaibuddy.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.support.tech.newsaibuddy.data.api.ResourceState
import com.support.tech.newsaibuddy.data.datasource.news.NewsDataSource
import com.support.tech.newsaibuddy.data.paging.NewsPagingSource
import com.support.tech.newsaibuddy.data.remote.mapper.toDomain
import com.support.tech.newsaibuddy.domain.model.Article
import com.support.tech.newsaibuddy.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Repository implementation residing in the Data layer.
 * Coordinates data operations, fetches DTOs from [NewsDataSource],
 * transforms them into Domain entities via mappers, and emits reactive [ResourceState] flows.
 */
class NewsRepositoryImpl @Inject constructor(
    private val newsDataSource: NewsDataSource
) : NewsRepository {

    override fun getTopHeadlinesByCategory(category: String): Flow<ResourceState<List<Article>>> {
        return flow {
            emit(ResourceState.Loading())
            val response = newsDataSource.getTopHeadlineBasedOnCategory(category)
            if (response.isSuccessful && response.body() != null) {
                val articles = response.body()!!.articles.toDomain()
                emit(ResourceState.Success(articles))
            } else {
                val code = response.code()
                val err = mapHttpErrorCode(code, response.errorBody()?.string() ?: response.message())
                android.util.Log.e("NewsRepository", "HTTP $code: $err")
                emit(ResourceState.Error(err))
            }
        }.catch { e ->
            android.util.Log.e("NewsRepository", "Exception fetching news", e)
            emit(ResourceState.Error(mapExceptionToFriendlyMessage(e)))
        }
    }

    override fun getTopHeadlinesByCountry(country: String): Flow<ResourceState<List<Article>>> {
        return flow {
            emit(ResourceState.Loading())
            val response = newsDataSource.getTopHeadlinesBasedOnCountry(country)
            if (response.isSuccessful && response.body() != null) {
                val articles = response.body()!!.articles.toDomain()
                emit(ResourceState.Success(articles))
            } else {
                val code = response.code()
                val err = mapHttpErrorCode(code, response.errorBody()?.string() ?: response.message())
                android.util.Log.e("NewsRepository", "HTTP $code: $err")
                emit(ResourceState.Error(err))
            }
        }.catch { e ->
            android.util.Log.e("NewsRepository", "Exception fetching news", e)
            emit(ResourceState.Error(mapExceptionToFriendlyMessage(e)))
        }
    }

    override fun getNewsPagingStream(category: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { NewsPagingSource(newsDataSource, category) }
        ).flow
    }

    private fun mapHttpErrorCode(code: Int, serverMessage: String): String {
        return when (code) {
            429 -> "Too many requests. Please try again in a few moments."
            500, 502, 503, 504 -> "Server is currently unavailable. Please try again later."
            else -> if (serverMessage.isNotBlank() && !serverMessage.contains("<html>", ignoreCase = true)) {
                serverMessage
            } else {
                "Unable to fetch news stories (Error $code)."
            }
        }
    }

    private fun mapExceptionToFriendlyMessage(e: Throwable): String {
        val message = e.localizedMessage ?: ""
        return when {
            e is UnknownHostException || message.contains("Unable to resolve host", ignoreCase = true) || message.contains("No address associated with hostname", ignoreCase = true) -> {
                "No internet connection. Please check your network and try again."
            }
            e is ConnectException || e is SocketTimeoutException -> {
                "Connection timed out. Please check your network connection."
            }
            e is IOException -> {
                "Network error. Please verify your connection and try again."
            }
            else -> {
                if (message.isNotBlank()) message else "Failed to load news. Please try again."
            }
        }
    }
}
