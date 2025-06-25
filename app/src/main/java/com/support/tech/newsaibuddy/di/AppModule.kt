package com.support.tech.newsaibuddy.di

// Imports necessary classes for dependency injection and network operations.
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.support.tech.newsaibuddy.data.AppConstants
import com.support.tech.newsaibuddy.data.api.ApiService
import com.support.tech.newsaibuddy.data.datasource.news.NewsDataSource
import com.support.tech.newsaibuddy.data.datasource.news.NewsDataSourceImpl
import com.support.tech.newsaibuddy.ui.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.support.tech.newsaibuddy.BuildConfig

// @Module indicates that this class provides dependencies.
// @InstallIn(SingletonComponent::class) specifies that the dependencies provided by this module will live as long as the application.
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    // @Provides marks this function as a provider of a dependency.
    // @Singleton ensures that only one instance of Retrofit is created and shared throughout the app.
    @Provides
    @Singleton
    // This function creates and configures a Retrofit instance for making network requests.
    fun providesRetrofit(): Retrofit {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val httpClient = OkHttpClient().newBuilder().apply {
            addInterceptor(httpLoggingInterceptor)
        } // Configures an OkHttpClient with a logging interceptor to log network request and response details.

        httpClient.apply {
            readTimeout(60, TimeUnit.SECONDS)
        } // Sets a read timeout of 60 seconds for network requests.

        // Configures Moshi for JSON parsing, enabling Kotlin reflection for data classes.
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory()).build()

        // Builds and returns the Retrofit instance.
        return Retrofit.Builder()
            .baseUrl(AppConstants.NEWS_URL)
            .client(httpClient.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // Provides an instance of ApiService.
    @Provides
    @Singleton
    // This function creates an instance of the ApiService interface using the provided Retrofit instance.
    fun providesApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // Provides an instance of NewsDataSource.
    @Provides
    @Singleton
    // This function creates an instance of NewsDataSourceImpl, which implements NewsDataSource,
    // and injects the ApiService into it.
    fun providesNewsDataSource(apiService: ApiService): NewsDataSource {
        return NewsDataSourceImpl(apiService)
    }

    // Provides an instance of GenerativeModel from the Google AI SDK.
    @Provides
    @Singleton
    // This function configures and returns a GenerativeModel.
    fun getGenerativeModel(): GenerativeModel {
        // Configures generation parameters for the AI model, like temperature.
        val config = generationConfig {
            temperature = 0.7f
        }

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash-latest",
            apiKey = BuildConfig.BOT_KEY,
            generationConfig = config
        ) // Initializes the GenerativeModel with a specific model name, API key (from BuildConfig), and the defined configuration.

        return generativeModel
    }

    // Provides an instance of NewsRepository.
    @Provides
    @Singleton
    // This function creates an instance of NewsRepository and injects the NewsDataSource into it.
    fun providesNewsRepository(newsDataSource: NewsDataSource): NewsRepository {
        return NewsRepository(newsDataSource)
    }
}
