plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.ksp.dev) apply false
    alias(libs.plugins.hilt.android) apply false

    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20" apply false

}