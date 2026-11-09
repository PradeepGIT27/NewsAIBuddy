package com.support.tech.newsaibuddy.domain.model

/**
 * Supported news categories with their display titles and query values.
 */
enum class NewsCategory(val displayName: String, val categoryValue: String) {
    BUSINESS("Business", "business"),
    ENTERTAINMENT("Entertainment", "entertainment"),
    GENERAL("General", "general"),
    HEALTH("Health", "health"),
    SCIENCE("Science", "science"),
    SPORTS("Sports", "sports"),
    TECHNOLOGY("Technology", "technology");

    companion object {
        fun fromValue(value: String): NewsCategory {
            return entries.firstOrNull { it.categoryValue.equals(value, ignoreCase = true) } ?: BUSINESS
        }
    }
}

