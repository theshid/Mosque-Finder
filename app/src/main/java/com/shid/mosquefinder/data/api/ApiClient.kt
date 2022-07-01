package com.shid.mosquefinder.data.api



import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var okHttpClient: OkHttpClient? = null

    fun provideOkHttpClient():OkHttpClient {
        if (okHttpClient ==null){
           okHttpClient = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build()
        }
       return okHttpClient!!
    }

    fun getClient(baseUrl:String):Retrofit{
        if (retrofit == null){
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(provideOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

}