package com.example.worldcup2026.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId")
    fun getMatchesByTournament(tournamentId: Int): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<MatchEntity>)

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Int): MatchEntity?

    @Query("SELECT id FROM matches")
    suspend fun getAllMatchIds(): List<Int>

    @Query("DELETE FROM matches WHERE id IN (:ids)")
    suspend fun deleteMatchesByIds(ids: List<Int>)

    @Query("DELETE FROM matches")
    suspend fun clearAllMatches()
}
