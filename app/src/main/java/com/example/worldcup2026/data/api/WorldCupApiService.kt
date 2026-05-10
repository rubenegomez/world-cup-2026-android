package com.example.worldcup2026.data.api

import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import retrofit2.http.GET
import retrofit2.http.Query

interface WorldCupApiService {
    @GET("teams")
    suspend fun getTeams(@Query("league") leagueId: Int = 1, @Query("season") season: Int = 2026): List<Team>

    @GET("fixtures")
    suspend fun getMatches(@Query("league") leagueId: Int = 1, @Query("season") season: Int = 2026): List<Match>
}
