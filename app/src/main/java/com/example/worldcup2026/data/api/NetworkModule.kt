package com.example.worldcup2026.data.api

import com.example.worldcup2026.data.model.Group
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface WorldCupApiService {
    @GET("api/teams")
    suspend fun getTeams(): List<Team>

    @GET("api/groups")
    suspend fun getGroups(): List<Group>

    @GET("api/matches")
    suspend fun getMatches(): List<Match>

    @GET("api/matches/live")
    suspend fun getLiveMatches(): List<LiveMatchDto>
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
    val events: List<String>,
    val clock: String?
)

object NetworkModule {
    const val BASE_URL = "http://161.153.196.145:8000/"

    val apiService: WorldCupApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WorldCupApiService::class.java)
    }
}
