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
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val date: String,
    val status: String,
    val stadium: String = "",
    val city: String = "",
    // Campos para el Prode
    val predictedWinner: String? = null, // "L", "E", "V"
    val predictedHomeScore: Int? = null,
    val predictedAwayScore: Int? = null,
    // Estadísticas VIP
    val homePossession: Int? = null,
    val awayPossession: Int? = null,
    val homeShots: Int? = null,
    val awayShots: Int? = null,
    val scorers: List<String> = emptyList()
)
