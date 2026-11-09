package com.support.tech.newsaibuddy.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.support.tech.newsaibuddy.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * Hilt module providing Gemini AI GenerativeModel.
 */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        val config = generationConfig {
            temperature = 0.7f
        }

        val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(Date())
        val systemInstruction = content {
            text("You are NewsAIBuddy, a helpful AI news assistant. Today's date is $currentDate. Always use this date as the current date reference when asked about today, recent events, or current dates.")
        }

        return GenerativeModel(
            modelName = "gemini-3.5-flash",
            apiKey = BuildConfig.BOT_KEY,
            generationConfig = config,
            systemInstruction = systemInstruction,
            requestOptions = RequestOptions(timeout = 60.seconds)
        )
    }
}

