package com.support.tech.newsaibuddy

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class annotated with @HiltAndroidApp.
 * Implements [ImageLoaderFactory] to configure global Coil image caching
 * (25% Memory Cache + 100 MB Disk Cache) and crossfade performance.
 */
@HiltAndroidApp
class NewsAIBuddyApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Utilize up to 25% of available app RAM for image caching
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("news_image_cache"))
                    .maxSizeBytes(100L * 1024L * 1024L) // 100 MB disk cache limit for news images
                    .build()
            }
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Cache images aggressively even if publishers omit Cache-Control headers
            .build()
    }
}
