# Line numbers & Generic Signatures for Retrofit / Moshi Reflection
-keepattributes SourceFile, LineNumberTable, Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations, AnnotationDefault

# Keep Kotlin Metadata, Reflection & Coroutines
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlin.coroutines.Continuation { *; }
-keep class kotlin.coroutines.** { *; }

# Retrofit service interfaces and methods
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep @retrofit2.http.* class * { *; }
-keep class retrofit2.Response { *; }
-keep class retrofit2.** { *; }
-keepclassmembers class retrofit2.** { *; }
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Keep ApiService interface
-keep interface com.support.tech.newsaibuddy.data.api.** { *; }
-keepclassmembers interface com.support.tech.newsaibuddy.data.api.** { *; }

# Keep Moshi & OkHttp
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class com.squareup.moshi.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.squareup.moshi.**

-keep class * extends com.squareup.moshi.JsonAdapter {
    public <init>(...);
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <init>(...);
}
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
}

# Data transfer objects & Domain Models
-keep class com.support.tech.newsaibuddy.data.remote.dto.** { *; }
-keepclassmembers class com.support.tech.newsaibuddy.data.remote.dto.** { *; }
-keep class com.support.tech.newsaibuddy.domain.model.** { *; }
-keepclassmembers class com.support.tech.newsaibuddy.domain.model.** { *; }

# Credential Manager & Google Identity
-keep class androidx.credentials.** { *; }
-keepclassmembers class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keepclassmembers class com.google.android.libraries.identity.googleid.** { *; }

# Google Play Services Auth & Tasks
-keep class com.google.android.gms.auth.api.identity.** { *; }
-keepclassmembers class com.google.android.gms.auth.api.identity.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keepclassmembers class com.google.android.gms.tasks.** { *; }
-dontwarn com.google.android.gms.**

# LiteRT-LM (On-Device LLM & JNI Callbacks)
-keep class com.google.ai.edge.litertlm.** { *; }
-keepclassmembers class com.google.ai.edge.litertlm.** { *; }
-dontwarn com.google.ai.edge.litertlm.**

# Keep all native methods across the app
-keepclasseswithmembernames class * {
    native <methods>;
}

# Gson (used by LiteRT-LM JNI for message and tool payloads)
-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.** { *; }
-dontwarn com.google.gson.**

