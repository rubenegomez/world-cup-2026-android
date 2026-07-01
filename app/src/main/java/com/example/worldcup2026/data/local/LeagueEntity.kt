package com.example.worldcup2026.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leagues")
data class LeagueEntity(
    @PrimaryKey val id: String,
    val name: String,
    val creatorId: String,
    val code: String,
    @ColumnInfo(name = "tournamentId", defaultValue = "1") val tournamentId: Int = 1
)
