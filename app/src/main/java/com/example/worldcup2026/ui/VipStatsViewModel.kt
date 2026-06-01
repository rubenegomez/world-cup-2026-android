package com.example.worldcup2026.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worldcup2026.data.api.NetworkModule
import com.example.worldcup2026.data.api.TeamStatsDto
import com.example.worldcup2026.data.api.EventDto
import kotlinx.coroutines.launch

sealed class VipStatsUiState {
    object Loading : VipStatsUiState()
    data class Success(
        val homeStats: TeamStatsDto?,
        val awayStats: TeamStatsDto?,
        val events: List<EventDto>
    ) : VipStatsUiState()
    data class Error(val message: String) : VipStatsUiState()
}

class VipStatsViewModel : ViewModel() {
    private val _uiState = mutableStateOf<VipStatsUiState>(VipStatsUiState.Loading)
    val uiState: State<VipStatsUiState> = _uiState

    fun loadStats(fixtureId: Int) {
        _uiState.value = VipStatsUiState.Loading
        viewModelScope.launch {
            try {
                // Fetch stats
                val statsResponse = NetworkModule.apiService.getFixtureStatistics(fixtureId)
                val homeStats = statsResponse.response.getOrNull(0)
                val awayStats = statsResponse.response.getOrNull(1)

                // Fetch events
                val eventsResponse = NetworkModule.apiService.getFixtureEvents(fixtureId, "Goal")
                
                _uiState.value = VipStatsUiState.Success(
                    homeStats = homeStats,
                    awayStats = awayStats,
                    events = eventsResponse.response
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = VipStatsUiState.Error(e.message ?: "Error al cargar estadísticas VIP")
            }
        }
    }
}
