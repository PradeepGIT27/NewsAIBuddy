// Define the package for the application class
package com.support.tech.newsaibuddy

// Import necessary Android and Hilt libraries
import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Annotate the application class with @HiltAndroidApp to enable Hilt dependency injection
@HiltAndroidApp
// Define the main application class, inheriting from Application
class NewsAIBuddyApplication: Application()
