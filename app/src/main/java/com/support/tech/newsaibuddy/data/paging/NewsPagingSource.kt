package com.support.tech.newsaibuddy.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.support.tech.newsaibuddy.data.datasource.news.NewsDataSource
import com.support.tech.newsaibuddy.data.remote.mapper.toDomain
import com.support.tech.newsaibuddy.domain.model.Article

/**
 * PagingSource implementation for infinite scrolling news feeds.
 */
class NewsPagingSource(
    private val newsDataSource: NewsDataSource,
    private val category: String
) : PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        return try {
            val response = newsDataSource.getTopHeadlineBasedOnCategory(
                category = category,
                page = page,
                pageSize = params.loadSize
            )

            if (response.isSuccessful && response.body() != null) {
                val articles = response.body()!!.articles.toDomain()
                LoadResult.Page(
                    data = articles,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (articles.isEmpty()) null else page + 1
                )
            } else {
                LoadResult.Error(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
