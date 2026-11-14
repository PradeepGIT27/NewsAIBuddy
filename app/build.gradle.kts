import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp.dev)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.support.tech.newsaibuddy"
    compileSdk = 37

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
    val botKey = formatKey(System.getenv("BOT_KEY") ?: properties.getProperty("botKey"))
    val newsKey = formatKey(System.getenv("NEWS_KEY") ?: properties.getProperty("newsKey"))

    val envVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull()
        ?: project.findProperty("versionCode")?.toString()?.toIntOrNull()
        ?: 7

    val envVersionName = System.getenv("VERSION_NAME")
        ?: project.findProperty("versionName")?.toString()
        ?: "1.0.3"

    defaultConfig {
        applicationId = "com.support.tech.newsaibuddy"
        minSdk = 24
        targetSdk = 37
        versionCode = envVersionCode
        versionName = envVersionName

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
            val envStoreFile = System.getenv("KEYSTORE_FILE")
            val envStorePassword = System.getenv("KEYSTORE_PASSWORD")
            val envKeyAlias = System.getenv("KEY_ALIAS")
            val envKeyPassword = System.getenv("KEY_PASSWORD")

            if (!envStoreFile.isNullOrBlank() && !envStorePassword.isNullOrBlank()) {
                storeFile = file(envStoreFile)
                storePassword = envStorePassword
                keyAlias = envKeyAlias
                keyPassword = envKeyPassword
            } else if (keystorePropertiesFile.exists()) {
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
                "proguard-rules.pro",
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

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
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
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.metadata.jvm)
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

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}