package com.support.tech.newsaibuddy.di

import android.content.Context
import androidx.room.Room
import com.support.tech.newsaibuddy.data.local.ChatHistoryManager
import com.support.tech.newsaibuddy.data.local.SavedArticlesManager
import com.support.tech.newsaibuddy.data.local.UserSessionManager
import com.support.tech.newsaibuddy.data.local.db.AppDatabase
import com.support.tech.newsaibuddy.data.local.db.ChatDao
import com.support.tech.newsaibuddy.data.local.db.NewsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing local database, DAOs, and data managers.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "news_ai_buddy.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideNewsDao(database: AppDatabase): NewsDao = database.newsDao()

    @Provides
    @Singleton
    fun provideChatDao(database: AppDatabase): ChatDao = database.chatDao()

    @Provides
    @Singleton
    fun provideUserSessionManager(
        @ApplicationContext context: Context
    ): UserSessionManager = UserSessionManager(context)

    @Provides
    @Singleton
    fun provideSavedArticlesManager(
        newsDao: NewsDao
    ): SavedArticlesManager = SavedArticlesManager(newsDao)

    @Provides
    @Singleton
    fun provideChatHistoryManager(
        chatDao: ChatDao
    ): ChatHistoryManager = ChatHistoryManager(chatDao)
}
