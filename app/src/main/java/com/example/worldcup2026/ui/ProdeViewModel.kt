package com.example.worldcup2026.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.worldcup2026.data.api.SubmitPredictionRequest
import com.example.worldcup2026.data.local.WorldCupDatabase
import com.example.worldcup2026.data.repository.ProdeRepository
import com.example.worldcup2026.data.repository.WorldCupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProdeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = WorldCupDatabase.getDatabase(application)
    private val prodeRepository = ProdeRepository(database.leagueDao())
    private val worldCupRepository = WorldCupRepository(database.matchDao())

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()
    
    val leagues = prodeRepository.getLocalLeagues()
    
    private val _allMatches = MutableStateFlow<List<com.example.worldcup2026.data.model.Match>>(emptyList())
    val allMatches = _allMatches.asStateFlow()

    private val _predictions = MutableStateFlow<Map<Int, SubmitPredictionRequest>>(emptyMap())
    val predictions = _predictions.asStateFlow()

    init {
        loadMatches()
    }

    private fun loadMatches() {
        viewModelScope.launch {
            _allMatches.value = worldCupRepository.getMatches()
        }
    }

    fun updatePrediction(matchId: Int, homeScore: Int, awayScore: Int) {
        val currentMap = _predictions.value.toMutableMap()
        currentMap[matchId] = SubmitPredictionRequest(matchId, homeScore, awayScore)
        _predictions.value = currentMap
    }

    fun handleSignIn(idToken: String) {
        viewModelScope.launch {
            val success = prodeRepository.authenticateWithFirebase(idToken)
            if (success) {
                _isAuthenticated.value = true
                prodeRepository.fetchMyLeagues()
            }
        }
    }

    fun createLeague(name: String) {
        viewModelScope.launch {
            prodeRepository.createLeague(name)
        }
    }

    fun joinLeague(code: String) {
        viewModelScope.launch {
            prodeRepository.joinLeague(code)
        }
    }

    fun syncPredictions(predictions: List<SubmitPredictionRequest>) {
        viewModelScope.launch {
            prodeRepository.submitPredictions(predictions)
        }
    }

    suspend fun getStandings(leagueId: String): List<com.example.worldcup2026.data.api.StandingDto> {
        return prodeRepository.getStandings(leagueId)
    }
}
