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
    val status: String
)
