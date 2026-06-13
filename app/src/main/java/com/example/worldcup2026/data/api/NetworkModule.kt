package com.example.worldcup2026.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

interface LiveResultsService {
    @GET
    suspend fun getLiveResults(@Url url: String): List<LiveMatchDto>
}

data class LiveMatchDto(
    val matchId: Int,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: String,
    val homePossession: Int?,
    val awayPossession: Int?,
    val homeShots: Int?,
    val awayShots: Int?,
    val homeFouls: Int?,
    val awayFouls: Int?,
    val homeCorners: Int?,
    val awayCorners: Int?,
    val homeSaves: Int?,
    val awaySaves: Int?,
    val homeYellowCards: Int?,
    val awayYellowCards: Int?,
    val homeRedCards: Int?,
    val awayRedCards: Int?,
    val homePasses: String?,
    val awayPasses: String?,
    val scorers: List<String>,
    val events: List<String>
)

object NetworkModule {
    // URL real de GitHub para descargar los resultados en vivo
    const val DEFAULT_JSON_URL = "https://raw.githubusercontent.com/rubenegomez/world-cup-2026-android/main/fixtures_live.json"

    val apiService: LiveResultsService by lazy {
        Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LiveResultsService::class.java)
    }
}
