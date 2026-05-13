package com.example.worldcup2026.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    val homeScore: Int?,
    val awayScore: Int?,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val status: String,
    // Prode
    val predictedWinner: String? = null,
    val predictedHomeScore: Int? = null,
    val predictedAwayScore: Int? = null,
    // Stats
    val homePossession: Int? = null,
    val awayPossession: Int? = null,
    val homeShots: Int? = null,
    val awayShots: Int? = null
)
