package com.example.worldcup2026.data.model

data class Team(
    val id: Int,
    val name: String,
    val flagUrl: String,
    val group: String
)

data class Group(
    val letter: String,
    val teams: List<Team>
)

data class Match(
    val id: Int,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int?,
    val awayScore: Int?,
    val date: String,
    val status: String
)
