package com.example.worldcup2026.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface WorldCupApiService {
    @GET("fixtures/statistics")
    suspend fun getFixtureStatistics(
        @Query("fixture") fixtureId: Int
    ): ApiResponse<TeamStatsDto>

    @GET("fixtures/events")
    suspend fun getFixtureEvents(
        @Query("fixture") fixtureId: Int,
        @Query("type") type: String = "Goal"
    ): ApiResponse<EventDto>
}

data class ApiResponse<T>(
    val response: List<T>
)

data class TeamStatsDto(
    val team: TeamDto,
    val statistics: List<StatDto>
)

data class TeamDto(
    val id: Int,
    val name: String
)

data class StatDto(
    val type: String,
    val value: Any?
)

data class EventDto(
    val time: TimeDto,
    val team: TeamDto,
    val player: PlayerDto,
    val type: String,
    val detail: String
)

data class TimeDto(
    val elapsed: Int
)

data class PlayerDto(
    val id: Int?,
    val name: String
)
