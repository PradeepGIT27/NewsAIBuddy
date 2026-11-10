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
-keep class androidx.credentials.Credential { *; }
-keep class androidx.credentials.GetCredentialResponse { *; }
-keep class androidx.credentials.playservices.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
