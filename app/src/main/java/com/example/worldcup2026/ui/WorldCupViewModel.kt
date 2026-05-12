package com.example.worldcup2026.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worldcup2026.data.model.Group
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.repository.WorldCupRepository
import kotlinx.coroutines.launch
import com.example.worldcup2026.data.model.Team

sealed class WorldCupUiState {
    object Loading : WorldCupUiState()
    data class Success(val groups: List<Group>, val matches: List<Match>, val teams: List<Team>) : WorldCupUiState()
    data class Error(val message: String) : WorldCupUiState()
}

class WorldCupViewModel(private val repository: WorldCupRepository = WorldCupRepository()) : ViewModel() {

    private val _uiState = mutableStateOf<WorldCupUiState>(WorldCupUiState.Loading)
    val uiState: State<WorldCupUiState> = _uiState

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = WorldCupUiState.Loading
            try {
                val groups = repository.getMockGroups()
                val matches = repository.getMockMatches()
                val teams = repository.getAllTeams()
                _uiState.value = WorldCupUiState.Success(groups, matches, teams)
            } catch (e: Exception) {
                _uiState.value = WorldCupUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
