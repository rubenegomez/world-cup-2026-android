package com.example.worldcup2026.data.api

import com.example.worldcup2026.data.model.Group
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

interface WorldCupApiService {
    @GET("api/teams")
    suspend fun getTeams(@Query("tournament_id") tournamentId: Int? = null): List<Team>

    @GET("api/groups")
    suspend fun getGroups(@Query("tournament_id") tournamentId: Int? = null): List<Group>

    @GET("api/matches")
    suspend fun getMatches(@Query("tournament_id") tournamentId: Int? = null): List<Match>

    @GET("api/matches/live")
    suspend fun getLiveMatches(@Query("tournament_id") tournamentId: Int? = null): List<LiveMatchDto>

    @GET("api/tournaments/{id}/annual-standings")
    suspend fun getAnnualStandings(@Path("id") tournamentId: Int): List<AnnualStandingDto>

    @GET("api/tournaments/{id}/descenso-standings")
    suspend fun getDescensoStandings(@Path("id") tournamentId: Int): List<DescensoStandingDto>

    @GET("api/tournaments/{id}/goleadores")
    suspend fun getGoleadores(@Path("id") tournamentId: Int): List<GoleadorDto>
}

data class AnnualStandingDto(
    val pos: Int,
    val team_id: Int,
    val team_name: String,
    val logo_url: String,
    val pj: Int,
    val g: Int,
    val e: Int,
    val p: Int,
    val gf: Int,
    val gc: Int,
    val dg: Int,
    val pts: Int
)

data class DescensoStandingDto(
    val pos: Int,
    val team_id: Int,
    val team_name: String,
    val logo_url: String,
    val historical_pts: Int,
    val historical_pj: Int,
    val current_pts: Int,
    val current_pj: Int,
    val total_pts: Int,
    val total_pj: Int,
    val promedio: Double
)

data class GoleadorDto(
    val pos: Int,
    val player_name: String,
    val team_name: String,
    val logo_url: String,
    val goals: Int
)

data class LiveMatchDto(
    val matchId: Int,
    val homeScore: Int?,
    val awayScore: Int?,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
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


class NullTeamInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (request.url.encodedPath.contains("api/matches")) {
            val bodyString = response.body?.string()
            if (bodyString != null) {
                // Reemplazamos los objetos nulos por el equipo TBD (ID 180 para que concuerde) usando Regex para ignorar espacios
                val homeRegex = "\"homeTeam\"\\s*:\\s*null".toRegex()
                val awayRegex = "\"awayTeam\"\\s*:\\s*null".toRegex()
                
                val fixedBody = bodyString
                    .replace(homeRegex, "\"homeTeam\":{\"id\":180,\"name\":\"Por definirse\",\"flagUrl\":\"\",\"group\":\"TBD\",\"players\":[]}")
                    .replace(awayRegex, "\"awayTeam\":{\"id\":180,\"name\":\"Por definirse\",\"flagUrl\":\"\",\"group\":\"TBD\",\"players\":[]}")
                
                return response.newBuilder()
                    .body(fixedBody.toResponseBody(response.body?.contentType()))
                    .build()
            }
        }
        return response
    }
}

object NetworkModule {
    const val BASE_URL = "http://ellocodelpedal.duckdns.org:8000/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(NullTeamInterceptor())
        .build()

    val apiService: WorldCupApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WorldCupApiService::class.java)
    }

    const val PRODE_BASE_URL = "https://ellocodelpedal.duckdns.org/"

    val prodeApiService: ProdeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PRODE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProdeApiService::class.java)
    }
}
