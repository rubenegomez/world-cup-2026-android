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
            
            val newStatus = if (homeScore == null && awayScore == null) "Scheduled" else match.status
            repository.saveMatchScore(matchId, homeScore, awayScore, match.homePenalties, match.awayPenalties, newStatus)
            
            val updatedList = currentState.matches.map {
                if (it.id == matchId) it.copy(homeScore = homeScore, awayScore = awayScore, status = newStatus) else it
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
                    it.copy(status = status, homeScore = home, awayScore = away)
                } else it
            }
            val finalKnockout = KnockoutCalculator.calculateKnockoutMatches(updatedList)
            val allMatches = groupMatchesPlusKnockout(updatedList, finalKnockout)
            _uiState.value = currentState.copy(matches = allMatches, champion = getChampion(allMatches))
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
            repository.saveMatchPrediction(matchId, winner, homePredict, awayPredict)
            val updatedList = currentState.matches.map {
                if (it.id == matchId) it.copy(predictedWinner = winner, predictedHomeScore = homePredict, predictedAwayScore = awayPredict) else it
            }
            _uiState.value = currentState.copy(matches = updatedList)
        }
    }

    private fun groupMatchesPlusKnockout(all: List<Match>, knockout: List<Match>): List<Match> {
        val groupOnes = all.filter { it.id <= 100 }
        return groupOnes + knockout
    }
}
