package com.example.worldcup2026.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo


@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "tournamentId", defaultValue = "1") val tournamentId: Int = 1,
    val homeScore: Int?,
    val awayScore: Int?,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val status: String,
    // Prode
    val predictedWinner: String? = null,
    val predictedHomeScore: Int? = null,
    val predictedAwayScore: Int? = null,
    val predictedHomePenalties: Int? = null,
    val predictedAwayPenalties: Int? = null,
    // Stats
    val homePossession: Int? = null,
    val awayPossession: Int? = null,
    val homeShots: Int? = null,
    val awayShots: Int? = null,
    // Datos en vivo adicionales
    val scorers: String? = null,
    val events: String? = null,
    val vipStats: String? = null,
    val clock: String? = null
)
