package com.support.tech.newsaibuddy.ui.screens.reference

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Use autoMirrored for RTL support
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.support.tech.newsaibuddy.ui.theme.appColor80

// Opt-in for ExperimentalMaterial3Api as some Material 3 components might be experimental
@OptIn(ExperimentalMaterial3Api::class)
// Suppress lint warning for enabling JavaScript in WebView, which is necessary for many websites
@SuppressLint("SetJavaScriptEnabled") // Suppress lint warning for JavaScript enabling
@Composable
fun ReferenceScreen(navController: NavController, urlToLoad: String) {
    // State to hold the WebView instance
    var webView: WebView? by remember { mutableStateOf(null) }
    // State to track if the page is currently loading
    var isLoading by remember { mutableStateOf(true) }
    // State to hold any web resource error encountered
    var webViewError by remember { mutableStateOf<WebResourceError?>(null) }

    // Handle back press: first try navigating back in WebView, then pop screen
    BackHandler(enabled = true) {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        // Top app bar configuration
        topBar = {
            TopAppBar(
                // Set the background color of the top app bar
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColor80),
                // Title of the app bar (currently empty, could be dynamic)
                title = { Text("", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Box layout to overlay loading indicator and error message on top of WebView
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // AndroidView to embed the native Android WebView
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    // Create and configure the WebView instance
                    WebView(context).apply {
                        // Set layout parameters to fill the parent
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true // Enable JavaScript
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                // Called when a page starts loading
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                webViewError = null // Reset error on new page start
                                Log.d("WebViewScreen", "Page loading started: $url")
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                // Called when a page finishes loading
                                super.onPageFinished(view, url)
                                isLoading = false
                                Log.d("WebViewScreen", "Page loading finished: $url")
                            }

                            // Handle URL loading errors
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                // Called when an error occurs during page loading
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    isLoading = false
                                    webViewError = error
                                    Log.e("WebViewScreen", "Error loading page: ${error?.description} for URL: ${request.url}")
                                    // Optionally load a local error HTML or show a message
                                    // view?.loadData("<html><body><h1>Error</h1><p>${error?.description}</p></body></html>", "text/html", "UTF-8")
                                }
                            }

                            // Keep navigation inside the WebView
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                // Intercept URL loading to keep navigation within the WebView
                                val requestedUrl = request?.url?.toString()
                                Log.d("WebViewScreen", "Intercepting URL load: $requestedUrl")
                                if (requestedUrl != null) {
                                    view?.loadUrl(requestedUrl) // Load URL in this WebView
                                    return true // Indicate we handled the URL loading
                                }
                                return false // Let the WebView handle it (or system if unhandled scheme)
                            }
                        }
                        // Set WebChromeClient for features like JavaScript alerts, progress, etc.
                        webChromeClient = WebChromeClient() // Needed for some JS features like alerts
                        // Log the URL being loaded and initiate loading
                        Log.d("WebViewScreen", "Loading URL in WebView: $urlToLoad")
                        loadUrl(urlToLoad) // Load the initial URL
                        webView = this // Assign the created webview to the state variable
                    }
                },
                update = {
                    // Update logic if needed when composable recomposes (e.g., URL changes)
                    // In this basic case, we load URL only in factory
                    Log.d("WebViewScreen", "WebView update called")
                    // Example: If urlToLoad could change via recomposition, you might check here:
                    // if (view.url != urlToLoad) { view.loadUrl(urlToLoad) }
                }
            )

            // Show loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    color = appColor80,
                    modifier = Modifier.align(Alignment.Center))
            }

            // Show error message if a webViewError occurred
            webViewError?.let { error ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Center the error content vertically and horizontally
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Failed to load page", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Error: ${error.errorCode} - ${error.description}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        webViewError = null // Clear error
                        isLoading = true    // Set loading state
                        webView?.reload()   // Try reloading
                    }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}