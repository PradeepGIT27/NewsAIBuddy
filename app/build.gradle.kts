import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp.dev)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.support.tech.newsaibuddy"
    compileSdk = 36

    val file = rootProject.file("local.properties")
    val properties = Properties()
    if (file.exists()) {
        properties.load(file.inputStream())
    }
    fun formatKey(key: String?): String {
        if (key.isNullOrBlank()) return "\"\""
        val trimmed = key.trim()
        return if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) trimmed else "\"$trimmed\""
    }
    val botKey = formatKey(properties.getProperty("botKey"))
    val newsKey = formatKey(properties.getProperty("newsKey"))

    defaultConfig {
        applicationId = "com.support.tech.newsaibuddy"
        minSdk = 24
        targetSdk = 36
        versionCode = 7
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BOT_KEY", botKey)
        buildConfigField("String", "NEWS_API_KEY", newsKey)
    }

    val keystorePropertiesFile = rootProject.file("key.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                val storeFilePath = keystoreProperties["storeFile"] as String
                storeFile = rootProject.file(storeFilePath)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xskip-metadata-version-check")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.21")
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.litertlm.android)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.material)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.converter.moshi)
    implementation(libs.coil.compose)
    implementation(libs.transport.runtime)

    implementation(libs.generativeai)
    implementation(libs.kotlinx.serialization.json)

    // Google Sign-In with Credential Manager
    implementation(libs.credentials)
    implementation(libs.credentials.play)
    implementation(libs.googleid)

    // DataStore for session & saved articles persistence
    implementation(libs.datastore.preferences)
}