package com.example.worldcup2026.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// --- DTOs ---

data class FirebaseTokenRequest(val idToken: String)

data class AuthResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val firebaseUid: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String
)

data class CreateLeagueRequest(val name: String)
data class JoinLeagueRequest(val code: String)

data class LeagueDto(
    val id: String,
    val name: String,
    val creatorId: String,
    val code: String
)

data class StandingDto(
    val id: String,
    val name: String,
    val avatar: String,
    val points: Int
)

data class SubmitPredictionRequest(
    val matchId: Int,
    val predictedHomeScore: Int,
    val predictedAwayScore: Int,
    val predictedHomePenalties: Int? = null,
    val predictedAwayPenalties: Int? = null
)

// --- API Service ---

interface ProdeApiService {
    @POST("api/prode/auth/firebase")
    suspend fun authFirebase(@Body request: FirebaseTokenRequest): AuthResponse

    @POST("api/prode/leagues")
    suspend fun createLeague(
        @Header("Authorization") token: String,
        @Body request: CreateLeagueRequest
    ): LeagueDto

    @POST("api/prode/leagues/join")
    suspend fun joinLeague(
        @Header("Authorization") token: String,
        @Body request: JoinLeagueRequest
    ): LeagueDto

    @GET("api/prode/leagues")
    suspend fun getMyLeagues(
        @Header("Authorization") token: String
    ): List<LeagueDto>

    @GET("api/prode/leagues/{id}/standings")
    suspend fun getStandings(
        @Header("Authorization") token: String,
        @Path("id") leagueId: String
    ): List<StandingDto>

    @POST("api/prode/predictions")
    suspend fun submitPredictions(
        @Header("Authorization") token: String,
        @Body predictions: List<SubmitPredictionRequest>
    )

    @GET("api/prode/predictions")
    suspend fun getMyPredictions(
        @Header("Authorization") token: String
    ): List<SubmitPredictionRequest>
}
