package com.support.tech.newsaibuddy.di

import android.content.Context
import com.support.tech.newsaibuddy.data.local.SavedArticlesManager
import com.support.tech.newsaibuddy.data.local.UserSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing local data managers (DataStore-backed).
 */
@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideUserSessionManager(
        @ApplicationContext context: Context
    ): UserSessionManager = UserSessionManager(context)

    @Provides
    @Singleton
    fun provideSavedArticlesManager(
        @ApplicationContext context: Context
    ): SavedArticlesManager = SavedArticlesManager(context)
}
