// Define the package for the NewsResponse data class
package com.support.tech.newsaibuddy.data.entity

// Define the NewsResponse data class
data class NewsResponse(
    // Status of the API response
    val status : String,
    // Total number of results found
    val totalResults: Int,
    // List of articles
    val articles:List<Article>
)

// Define the Article data class
data class Article (
    // Author of the article
    val author: String?,
    // Title of the article
    val title: String?,
    // Description of the article
    val description: String?,
    // Boolean flag to indicate if the article is expanded
    var expanded: Boolean = false,
    // URL of the article
    val url: String?,
    // URL of the image associated with the article
    val urlToImage: String?,
    // Publication date of the article
    val publishedAt: String?,
    // Content of the article
    val content: String?,
    // Source of the article
    val source: Source?
)

// Define the Source data class
data class Source (
    // ID of the source
    val id: String?,
    // Name of the source
    val name:String?
)
