package com.support.tech.newsaibuddy.ui.viewmodel.news

import com.support.tech.newsaibuddy.domain.model.Article
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsViewModelTest {

    @Test
    fun isArticleSaved_returnsTrueWhenArticlePresentInList() {
        val article = Article(
            author = "Tech Reporter",
            title = "AI Breakdown",
            description = "Latest AI trends",
            url = "https://example.com/ai",
            urlToImage = null,
            publishedAt = "2026-03-30",
            content = null,
            sourceName = "TechNews"
        )
        val state = NewsUiState(savedArticles = listOf(article))
        assertTrue(state.isArticleSaved(article))
    }

    @Test
    fun isArticleSaved_returnsFalseWhenArticleNotSaved() {
        val article1 = Article(author = null, title = "A1", description = null, url = "https://example.com/1", urlToImage = null, publishedAt = null, content = null, sourceName = null)
        val article2 = Article(author = null, title = "A2", description = null, url = "https://example.com/2", urlToImage = null, publishedAt = null, content = null, sourceName = null)

        val state = NewsUiState(savedArticles = listOf(article1))
        assertFalse(state.isArticleSaved(article2))
    }
}
