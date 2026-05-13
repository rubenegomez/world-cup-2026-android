package com.example.worldcup2026.data.model

data class Team(
    val id: Int,
    val name: String,
    val flagUrl: String,
    val group: String,
    val players: List<Player> = emptyList()
)

data class Player(
    val id: Int,
    val name: String,
    val photoUrl: String,
    val age: Int,
    val position: String,
    val club: String
)

data class Group(
    val name: String,
    val teams: List<Team>
)

data class Match(
    val id: Int,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int?,
    val awayScore: Int?,
    val homePenalties: Int? = null, // Goles en tanda de penales
    val awayPenalties: Int? = null, // Goles en tanda de penales
    val date: String,
    val status: String
)
