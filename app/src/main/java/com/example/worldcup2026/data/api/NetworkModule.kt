package com.example.worldcup2026.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://v3.football.api-sports.io/"
    private const val API_KEY = "13f1fe9cefbb89f9b748728c80582fa4"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-apisports-key", API_KEY)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val apiService: WorldCupApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WorldCupApiService::class.java)
    }
}
