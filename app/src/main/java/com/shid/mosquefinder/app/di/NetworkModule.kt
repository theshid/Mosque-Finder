package com.shid.mosquefinder.app.di

import com.shid.mosquefinder.app.utils.helper_class.singleton.Common
import com.shid.mosquefinder.data.api.DeeplApiInterface
import com.shid.mosquefinder.data.api.GoogleApiInterface
import com.shid.mosquefinder.data.api.QuranApiInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named(Common.GOOGLE)
    fun provideGoogleRetrofit(client: OkHttpClient): Retrofit =
        provideGoogleRetrofitConfiguration(client)

    @Provides
    @Singleton
    @Named(Common.QURAN)
    fun provideQuranRetrofit(client: OkHttpClient): Retrofit =
        provideQuranRetrofitConfiguration(client)

    @Provides
    @Singleton
    @Named(Common.DEEPL)
    fun provideDeeplRetrofit(client: OkHttpClient): Retrofit =
        provideDeeplRetrofitConfiguration(client)

    private fun provideGoogleRetrofitConfiguration(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Common.GOOGLE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideQuranRetrofitConfiguration(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Common.QURAN_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideDeeplRetrofitConfiguration(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Common.DEEPL_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun loggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.readTimeout(10, TimeUnit.SECONDS)
        httpClient.connectTimeout(10, TimeUnit.SECONDS)
        httpClient.writeTimeout(10, TimeUnit.SECONDS)
        httpClient.addInterceptor(loggingInterceptor())
        return httpClient.build()
    }

    @Provides
    @Singleton
    fun provideGoogleApiService(@Named(Common.GOOGLE) retrofit: Retrofit): GoogleApiInterface =
        retrofit.create(
            GoogleApiInterface::class.java
        )

    @Provides
    @Singleton
    fun provideDeeplApiService(@Named(Common.DEEPL) retrofit: Retrofit): DeeplApiInterface =
        retrofit.create(DeeplApiInterface::class.java)

    @Provides
    @Singleton
    fun provideQuranApiService(@Named(Common.QURAN) retrofit: Retrofit): QuranApiInterface =
        retrofit.create(
            QuranApiInterface::class.java
        )
}