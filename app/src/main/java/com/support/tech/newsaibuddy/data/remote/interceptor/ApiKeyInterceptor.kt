package com.support.tech.newsaibuddy.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that attaches the API key header to every outgoing NewsAPI request.
 * Centralizes auth token management without polluting Retrofit interfaces.
 */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithApiKey = originalRequest.newBuilder()
            .header("X-Api-Key", apiKey)
            .build()
        return chain.proceed(requestWithApiKey)
    }
}

