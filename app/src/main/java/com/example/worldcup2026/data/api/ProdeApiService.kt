package com.example.worldcup2026.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
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
    val avatarUrl: String,
    val favoriteTournaments: List<Int>? = emptyList(),
    val favoriteTeams: List<String>? = emptyList()
)

data class UpdateFavoritesRequest(
    val favoriteTournaments: List<Int> = emptyList(),
    val favoriteTeams: List<String> = emptyList()
)

data class CreateLeagueRequest(
    val name: String,
    val mode: String? = "FULL_TOURNAMENT",
    val tournament_id: Int? = null,
    val start_matchday: Int? = null,
    val end_matchday: Int? = null,
    val custom_prize: String? = null
)
data class JoinLeagueRequest(val code: String)

data class LeagueDto(
    val id: String,
    val name: String,
    val creatorId: String,
    val code: String,
    val mode: String? = "FULL_TOURNAMENT",
    val tournament_id: Int? = null,
    val start_matchday: Int? = null,
    val end_matchday: Int? = null,
    val custom_prize: String? = null,
    val status: String? = "ACTIVE"
)

data class StandingDto(
    val id: String,
    val name: String,
    val avatar: String,
    val points: Int
)

data class SubmitPredictionRequest(
    val matchId: Int,
    val predictedHomeScore: Int? = 0,
    val predictedAwayScore: Int? = 0,
    val predictedHomePenalties: Int? = null,
    val predictedAwayPenalties: Int? = null,
    val predictedWinner: String? = null,
    val isDoubleChance: Boolean? = false,
    val secHomeScore: Int? = null,
    val secAwayScore: Int? = null,
    val isDoublePointsMultiplier: Boolean? = false
)

data class MatchBreakdownDto(
    val matchId: Int,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val predictedHomeScore: Int? = null,
    val predictedAwayScore: Int? = null,
    val predictedWinner: String? = null,
    val points: Int,
    val status: String,
    val matchday: Int? = null
)

data class GlobalRankingUserDto(
    val id: String,
    val name: String,
    val avatar: String,
    val totalPoints: Int,
    val goldMedals: Int = 0,
    val silverMedals: Int = 0,
    val bronzeMedals: Int = 0,
    val leaguesPlayed: Int = 0
)

data class UserMedalsDto(
    val goldMedals: Int = 0,
    val silverMedals: Int = 0,
    val bronzeMedals: Int = 0,
    val totalLeaguesPlayed: Int = 0,
    val globalRank: Int = 1,
    val totalPoints: Int = 0
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

    @DELETE("api/prode/leagues/{id}")
    suspend fun deleteLeague(
        @Header("Authorization") token: String,
        @Path("id") leagueId: String
    )

    @GET("api/prode/leagues/{id}/standings")
    suspend fun getStandings(
        @Header("Authorization") token: String,
        @Path("id") leagueId: String
    ): List<StandingDto>

    @GET("api/prode/leagues/{league_id}/members/{member_user_id}/breakdown")
    suspend fun getMemberBreakdown(
        @Header("Authorization") token: String,
        @Path("league_id") leagueId: String,
        @Path("member_user_id") memberUserId: String
    ): List<MatchBreakdownDto>

    @POST("api/prode/predictions")
    suspend fun submitPredictions(
        @Header("Authorization") token: String,
        @Body predictions: List<SubmitPredictionRequest>
    )

    @GET("api/prode/predictions")
    suspend fun getMyPredictions(
        @Header("Authorization") token: String
    ): List<SubmitPredictionRequest>

    @GET("api/prode/ranking/global")
    suspend fun getGlobalRanking(
        @Header("Authorization") token: String
    ): List<GlobalRankingUserDto>

    @GET("api/prode/users/me/stats")
    suspend fun getUserStats(
        @Header("Authorization") token: String
    ): UserMedalsDto

    @GET("api/prode/users/me/favorites")
    suspend fun getUserFavorites(
        @Header("Authorization") token: String
    ): UpdateFavoritesRequest

    @PUT("api/prode/users/me/favorites")
    suspend fun updateUserFavorites(
        @Header("Authorization") token: String,
        @Body req: UpdateFavoritesRequest
    ): UpdateFavoritesRequest
}
