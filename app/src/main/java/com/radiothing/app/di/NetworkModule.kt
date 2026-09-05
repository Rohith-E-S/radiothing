package com.radiothing.app.di

import com.radiothing.app.BuildConfig
import com.radiothing.data.api.RadioBrowserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // URLs include query params — don't log them in release builds
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        val fallbackUrls = listOf(
            "https://de1.api.radio-browser.info/",
            "https://nl1.api.radio-browser.info/",
            "https://at1.api.radio-browser.info/",
            "https://all.api.radio-browser.info/"
        )
        
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(FallbackInterceptor(fallbackUrls))
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // Use a stable base URL; FallbackInterceptor handles per-request failover
        return Retrofit.Builder()
            .baseUrl("https://de1.api.radio-browser.info/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRadioBrowserApi(retrofit: Retrofit): RadioBrowserApi {
        return retrofit.create(RadioBrowserApi::class.java)
    }
}
