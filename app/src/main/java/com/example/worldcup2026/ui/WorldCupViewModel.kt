package com.example.worldcup2026.ui

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.worldcup2026.data.local.WorldCupDatabase
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import com.example.worldcup2026.data.repository.WorldCupRepository
import com.example.worldcup2026.data.util.KnockoutCalculator
import kotlinx.coroutines.launch

sealed class WorldCupUiState {
    object Loading : WorldCupUiState()
    data class Success(val matches: List<Match>, val champion: Team? = null) : WorldCupUiState()
    data class Error(val message: String) : WorldCupUiState()
}

class WorldCupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorldCupRepository
    private val _uiState = mutableStateOf<WorldCupUiState>(WorldCupUiState.Loading)
    val uiState: State<WorldCupUiState> = _uiState

    init {
        val database = WorldCupDatabase.getDatabase(application)
        repository = WorldCupRepository(database.matchDao())
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val matches = repository.getMatches()
                val finalMatches = KnockoutCalculator.calculateKnockoutMatches(matches)
                val allMatches = groupMatchesPlusKnockout(matches, finalMatches)
                _uiState.value = WorldCupUiState.Success(allMatches, getChampion(allMatches))
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WorldCupUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    private fun getChampion(matches: List<Match>): Team? {
        val finalMatch = matches.find { it.id == 131 }
        if (finalMatch == null || finalMatch.status != "Finished") return null
        val h = finalMatch.homeScore ?: 0
        val a = finalMatch.awayScore ?: 0
        return if (h > a) finalMatch.homeTeam 
               else if (a > h) finalMatch.awayTeam 
               else if ((finalMatch.homePenalties ?: 0) > (finalMatch.awayPenalties ?: 0)) finalMatch.homeTeam 
               else finalMatch.awayTeam
    }

    fun updateMatchScore(matchId: Int, homeScore: Int?, awayScore: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            val match = currentState.matches.find { it.id == matchId } ?: return@launch
            
            val isClearing = homeScore == null && awayScore == null
            val newStatus = if (isClearing) "Scheduled" else match.status
            
            if (isClearing) {
                repository.saveMatchPrediction(matchId, null, null, null)
            }
            
            repository.saveMatchScore(matchId, homeScore, awayScore, match.homePenalties, match.awayPenalties, newStatus)
            
            val updatedList = currentState.matches.map {
                if (it.id == matchId) {
                    it.copy(
                        homeScore = homeScore, 
                        awayScore = awayScore, 
                        status = newStatus,
                        predictedWinner = if (isClearing) null else it.predictedWinner,
                        predictedHomeScore = if (isClearing) null else it.predictedHomeScore,
                        predictedAwayScore = if (isClearing) null else it.predictedAwayScore
                    )
                } else it
            }
            
            val finalKnockout = KnockoutCalculator.calculateKnockoutMatches(updatedList)
            val allMatches = groupMatchesPlusKnockout(updatedList, finalKnockout)
            _uiState.value = currentState.copy(matches = allMatches, champion = getChampion(allMatches))
        }
    }

    fun updateMatchStatus(matchId: Int, status: String) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            repository.saveMatchStatus(matchId, status)
            
            val updatedList = currentState.matches.map {
                if (it.id == matchId) {
                    val home = if (status == "Finished") (it.homeScore ?: 0) else it.homeScore
                    val away = if (status == "Finished") (it.awayScore ?: 0) else it.awayScore
                    val updatedMatch = it.copy(status = status, homeScore = home, awayScore = away)
                    if (status == "Finished") {
                        checkProdeAndReward(updatedMatch)
                    }
                    updatedMatch
                } else it
            }
            val finalKnockout = KnockoutCalculator.calculateKnockoutMatches(updatedList)
            val allMatches = groupMatchesPlusKnockout(updatedList, finalKnockout)
            _uiState.value = currentState.copy(matches = allMatches, champion = getChampion(allMatches))
        }
    }

    private fun checkProdeAndReward(match: Match) {
        val realWinner = when {
            (match.homeScore ?: 0) > (match.awayScore ?: 0) -> "L"
            (match.homeScore ?: 0) < (match.awayScore ?: 0) -> "V"
            else -> "E"
        }
        
        var points = 0
        if (match.predictedWinner == realWinner) points += 1
        if (match.predictedHomeScore != null && match.predictedAwayScore != null &&
            match.homeScore == match.predictedHomeScore && match.awayScore == match.predictedAwayScore) {
            points += 2
        }
        
        if (points > 0) {
            val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
            val currentAdFreeUntil = prefs.getLong("ad_free_until", System.currentTimeMillis())
            val baseTime = if (currentAdFreeUntil > System.currentTimeMillis()) currentAdFreeUntil else System.currentTimeMillis()
            
            // 1 punto = 12 horas | 3 puntos = 36 horas
            val addedTime = points * 12 * 60 * 60 * 1000L
            prefs.edit().putLong("ad_free_until", baseTime + addedTime).apply()
        }
    }

    fun updateMatchPenalties(matchId: Int, homePenalties: Int?, awayPenalties: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            val match = currentState.matches.find { it.id == matchId } ?: return@launch
            
            repository.saveMatchScore(matchId, match.homeScore, match.awayScore, homePenalties, awayPenalties, match.status)
            
            val updatedList = currentState.matches.map {
                if (it.id == matchId) it.copy(homePenalties = homePenalties, awayPenalties = awayPenalties) else it
            }
            val finalKnockout = KnockoutCalculator.calculateKnockoutMatches(updatedList)
            val allMatches = groupMatchesPlusKnockout(updatedList, finalKnockout)
            _uiState.value = currentState.copy(matches = allMatches, champion = getChampion(allMatches))
        }
    }

    fun updateMatchPrediction(matchId: Int, winner: String?, homePredict: Int?, awayPredict: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            val finalWinner = if (homePredict == null && awayPredict == null) null else winner
            
            repository.saveMatchPrediction(matchId, finalWinner, homePredict, awayPredict)
            val updatedList = currentState.matches.map {
                if (it.id == matchId) it.copy(predictedWinner = finalWinner, predictedHomeScore = homePredict, predictedAwayScore = awayPredict) else it
            }
            _uiState.value = currentState.copy(matches = updatedList)
        }
    }

    private fun groupMatchesPlusKnockout(all: List<Match>, knockout: List<Match>): List<Match> {
        val groupOnes = all.filter { it.id <= 100 }
        return groupOnes + knockout
    }
}
