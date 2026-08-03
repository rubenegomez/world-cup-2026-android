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
    @ColumnInfo(name = "tournamentId", defaultValue = "5") val tournamentId: Int = 5,
    @ColumnInfo(name = "mode") val mode: String? = "FULL_TOURNAMENT",
    @ColumnInfo(name = "startMatchday") val startMatchday: Int? = null,
    @ColumnInfo(name = "endMatchday") val endMatchday: Int? = null,
    @ColumnInfo(name = "customPrize") val customPrize: String? = null,
    @ColumnInfo(name = "status") val status: String? = "ACTIVE"
)
