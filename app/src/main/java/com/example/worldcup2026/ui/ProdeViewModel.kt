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
    
    private val _currentUser = MutableStateFlow<com.example.worldcup2026.data.api.UserDto?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    val leagues = prodeRepository.getLocalLeagues()
    
    private val _allMatches = MutableStateFlow<List<com.example.worldcup2026.data.model.Match>>(emptyList())
    val allMatches = _allMatches.asStateFlow()

    init {
        loadMatches()
    }

    private fun loadMatches() {
        viewModelScope.launch {
            _allMatches.value = worldCupRepository.getMatches(1)
        }
    }

    fun handleSignIn(idToken: String) {
        viewModelScope.launch {
            val success = prodeRepository.authenticateWithFirebase(idToken)
            if (success) {
                _isAuthenticated.value = true
                _currentUser.value = prodeRepository.currentUser
                prodeRepository.fetchMyLeagues()
                // Descargar y restaurar predicciones guardadas en el servidor
                launch {
                    try {
                        prodeRepository.fetchMyPredictions(worldCupRepository)
                        loadMatches()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // Sincronizar pronósticos locales existentes con el servidor al iniciar sesión si hubiera nuevos
                launch {
                    try {
                        val matches = worldCupRepository.getMatches(1)
                        val localPredictions = matches.filter { it.predictedHomeScore != null && it.predictedAwayScore != null }
                            .map { SubmitPredictionRequest(it.id, it.predictedHomeScore ?: 0, it.predictedAwayScore ?: 0, it.predictedHomePenalties, it.predictedAwayPenalties) }
                        if (localPredictions.isNotEmpty()) {
                            prodeRepository.submitPredictions(localPredictions)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun signOut() {
        prodeRepository.logout()
        _isAuthenticated.value = false
        _currentUser.value = null
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

    suspend fun getStandings(leagueId: String): List<com.example.worldcup2026.data.api.StandingDto> {
        return prodeRepository.getStandings(leagueId)
    }
}
