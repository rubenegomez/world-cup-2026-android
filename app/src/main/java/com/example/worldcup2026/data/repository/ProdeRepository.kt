package com.example.worldcup2026.data.repository

import android.util.Log
import com.example.worldcup2026.data.api.CreateLeagueRequest
import com.example.worldcup2026.data.api.FirebaseTokenRequest
import com.example.worldcup2026.data.api.JoinLeagueRequest
import com.example.worldcup2026.data.api.NetworkModule
import com.example.worldcup2026.data.api.SubmitPredictionRequest
import com.example.worldcup2026.data.api.UserDto
import com.example.worldcup2026.data.local.LeagueDao
import com.example.worldcup2026.data.local.LeagueEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ProdeRepository(private val leagueDao: LeagueDao) {
    private val api = NetworkModule.prodeApiService
    
    companion object {
        var authToken: String? = null
    }
        
    var currentUser: UserDto? = null
        private set

    fun logout() {
        authToken = null
        currentUser = null
    }

    suspend fun authenticateWithFirebase(firebaseToken: String): Boolean {
        return try {
            val response = api.authFirebase(FirebaseTokenRequest(firebaseToken))
            authToken = "Bearer ${response.token}"
            currentUser = response.user
            true
        } catch (e: Exception) {
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("ProdeRepo", "Http Error: Code ${e.code()}, Body: $errorBody", e)
            } else {
                Log.e("ProdeRepo", "Error authenticating", e)
            }
            false
        }
    }

    suspend fun fetchMyLeagues() {
        val token = authToken ?: return
        try {
            val leaguesDto = api.getMyLeagues(token)
            val entities = leaguesDto.map { LeagueEntity(it.id, it.name, it.creatorId, it.code) }
            leagueDao.insertAll(entities)
        } catch (e: Exception) {
            Log.e("ProdeRepo", "Error fetching leagues", e)
        }
    }

    fun getLocalLeagues(): Flow<List<LeagueEntity>> = leagueDao.getAllLeagues()

    suspend fun createLeague(name: String): Boolean {
        val token = authToken ?: return false
        return try {
            val dto = api.createLeague(token, CreateLeagueRequest(name))
            leagueDao.insertLeague(LeagueEntity(dto.id, dto.name, dto.creatorId, dto.code))
            true
        } catch (e: Exception) {
            Log.e("ProdeRepo", "Error creating league", e)
            false
        }
    }

    suspend fun joinLeague(code: String): Boolean {
        val token = authToken ?: return false
        return try {
            val dto = api.joinLeague(token, JoinLeagueRequest(code))
            leagueDao.insertLeague(LeagueEntity(dto.id, dto.name, dto.creatorId, dto.code))
            true
        } catch (e: Exception) {
            Log.e("ProdeRepo", "Error joining league", e)
            false
        }
    }

    suspend fun getStandings(leagueId: String) = try {
        authToken?.let { api.getStandings(it, leagueId) } ?: emptyList()
    } catch (e: Exception) {
        Log.e("ProdeRepo", "Error fetching standings", e)
        emptyList()
    }

    suspend fun submitPredictions(predictions: List<SubmitPredictionRequest>): Boolean {
        val token = authToken ?: return false
        return try {
            api.submitPredictions(token, predictions)
            true
        } catch (e: Exception) {
            Log.e("ProdeRepo", "Error submitting predictions", e)
            false
        }
    }

    suspend fun fetchMyPredictions(worldCupRepository: WorldCupRepository): Boolean {
        val token = authToken ?: return false
        return try {
            val serverPredictions = api.getMyPredictions(token)
            serverPredictions.forEach { pred ->
                worldCupRepository.saveMatchPrediction(
                    matchId = pred.matchId,
                    winner = if (pred.predictedHomeScore > pred.predictedAwayScore) "home" else if (pred.predictedHomeScore < pred.predictedAwayScore) "away" else "draw",
                    homePredict = pred.predictedHomeScore,
                    awayPredict = pred.predictedAwayScore
                )
            }
            true
        } catch (e: Exception) {
            Log.e("ProdeRepo", "Error fetching predictions from server", e)
            false
        }
    }
}
