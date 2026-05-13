package com.example.worldcup2026.ui

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.worldcup2026.data.local.WorldCupDatabase
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.repository.WorldCupRepository
import kotlinx.coroutines.launch

sealed class WorldCupUiState {
    object Loading : WorldCupUiState()
    data class Success(val matches: List<Match>) : WorldCupUiState()
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
                _uiState.value = WorldCupUiState.Success(matches)
            } catch (e: Exception) {
                _uiState.value = WorldCupUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun updateMatchScore(matchId: Int, homeScore: Int?, awayScore: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            val match = currentState.matches.find { it.id == matchId } ?: return@launch
            
            repository.saveMatchScore(matchId, homeScore, awayScore, match.homePenalties, match.awayPenalties)
            
            // Actualizamos el estado UI localmente para respuesta instantánea
            val updatedMatches = currentState.matches.map {
                if (it.id == matchId) {
                    it.copy(homeScore = homeScore, awayScore = awayScore, status = if (homeScore != null && awayScore != null) "Finished" else "Scheduled")
                } else it
            }
            _uiState.value = currentState.copy(matches = updatedMatches)
        }
    }

    fun updateMatchPenalties(matchId: Int, homePenalties: Int?, awayPenalties: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            val match = currentState.matches.find { it.id == matchId } ?: return@launch
            
            repository.saveMatchScore(matchId, match.homeScore, match.awayScore, homePenalties, awayPenalties)
            
            val updatedMatches = currentState.matches.map {
                if (it.id == matchId) {
                    it.copy(homePenalties = homePenalties, awayPenalties = awayPenalties)
                } else it
            }
            _uiState.value = currentState.copy(matches = updatedMatches)
        }
    }
}
