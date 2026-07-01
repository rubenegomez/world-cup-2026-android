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
import com.example.worldcup2026.data.util.AnalyticsManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class WorldCupUiState {
    object Loading : WorldCupUiState()
    data class Success(val matches: List<Match>, val champion: Team? = null) : WorldCupUiState()
    data class Error(val message: String) : WorldCupUiState()
}

class WorldCupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorldCupRepository
    private val _uiState = mutableStateOf<WorldCupUiState>(WorldCupUiState.Loading)
    val uiState: State<WorldCupUiState> = _uiState

    val currentTournamentId = mutableStateOf(1)

    private val _isServerConnected = mutableStateOf(true)
    val isServerConnected: State<Boolean> = _isServerConnected

    private val _adFreeUntil = mutableStateOf(0L)
    val adFreeUntil: State<Long> = _adFreeUntil

    data class RewardDialogInfo(val round: Int, val points: Int, val hours: Int)
    private val _pendingRewardDialog = mutableStateOf<RewardDialogInfo?>(null)
    val pendingRewardDialog: State<RewardDialogInfo?> = _pendingRewardDialog
    
    private var autoSyncJob: kotlinx.coroutines.Job? = null

    init {
        val database = WorldCupDatabase.getDatabase(application)
        repository = WorldCupRepository(database.matchDao())
        val prefs = application.getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        _adFreeUntil.value = prefs.getLong("ad_free_until", 0L)
        loadData()
        checkPendingRewardDialog()
    }

    fun setTournament(id: Int) {
        currentTournamentId.value = id
        _uiState.value = WorldCupUiState.Loading
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Sincronización automática con el JSON remoto de GitHub en segundo plano
                launch {
                    val success = repository.syncMatchesWithLiveJson(getApplication(), currentTournamentId.value)
                    _isServerConnected.value = success
                    if (success) {
                        val matches = repository.getMatches(currentTournamentId.value)
                        val finalMatches = KnockoutCalculator.calculateKnockoutMatches(matches, currentTournamentId.value)
                        val allMatches = groupMatchesPlusKnockout(matches, finalMatches)
                        _uiState.value = WorldCupUiState.Success(allMatches, getChampion(allMatches))
                        checkRoundRewards(allMatches)
                        startAutoSync(allMatches)
                    }
                }

                val matches = repository.getMatches(currentTournamentId.value)
                val finalMatches = KnockoutCalculator.calculateKnockoutMatches(matches, currentTournamentId.value)
                val allMatches = groupMatchesPlusKnockout(matches, finalMatches)
                _uiState.value = WorldCupUiState.Success(allMatches, getChampion(allMatches))
                checkRoundRewards(allMatches)
                startAutoSync(allMatches)
            } catch (e: Exception) {
                e.printStackTrace()
                _isServerConnected.value = false
                _uiState.value = WorldCupUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun syncLiveResults(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val success = repository.syncMatchesWithLiveJson(getApplication(), currentTournamentId.value)
                _isServerConnected.value = success
                if (success) {
                    val matches = repository.getMatches(currentTournamentId.value)
                    val finalMatches = KnockoutCalculator.calculateKnockoutMatches(matches, currentTournamentId.value)
                    val allMatches = groupMatchesPlusKnockout(matches, finalMatches)
                    _uiState.value = WorldCupUiState.Success(allMatches, getChampion(allMatches))
                    checkRoundRewards(allMatches)
                    startAutoSync(allMatches)
                }
                onComplete(success)
            } catch (e: Exception) {
                e.printStackTrace()
                _isServerConnected.value = false
                onComplete(false)
            }
        }
    }

    private fun getChampion(matches: List<Match>): Team? {
        val finalMatch = matches.find { it.id == 103 }
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
            
            AnalyticsManager.logMatchAction("score_updated", matchId, "$homeScore-$awayScore")
            
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
            
            val finalKnockout = KnockoutCalculator.calculateKnockoutMatches(updatedList, currentTournamentId.value)
            val allMatches = groupMatchesPlusKnockout(updatedList, finalKnockout)
            _uiState.value = currentState.copy(matches = allMatches, champion = getChampion(allMatches))
            checkRoundRewards(allMatches)
        }
    }

    fun updateMatchStatus(matchId: Int, status: String) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            
            AnalyticsManager.logMatchAction("status_updated", matchId, status)
            repository.saveMatchStatus(matchId, status)
            
            val updatedList = currentState.matches.map {
                if (it.id == matchId) {
                    val home = if (status == "Finished") (it.homeScore ?: 0) else it.homeScore
                    val away = if (status == "Finished") (it.awayScore ?: 0) else it.awayScore
                    it.copy(status = status, homeScore = home, awayScore = away)
                } else it
            }
            val finalKnockout = KnockoutCalculator.calculateKnockoutMatches(updatedList, currentTournamentId.value)
            val allMatches = groupMatchesPlusKnockout(updatedList, finalKnockout)
            _uiState.value = currentState.copy(matches = allMatches, champion = getChampion(allMatches))
            checkRoundRewards(allMatches)
        }
    }

    private fun getMatchRound(matchId: Int): Int {
        if (matchId <= 0) return 0
        if (matchId <= 72) {
            val relativeId = (matchId - 1) % 6
            return when (relativeId) {
                0, 1 -> 1 // Fecha 1
                2, 3 -> 2 // Fecha 2
                else -> 3 // Fecha 3
            }
        }
        return when {
            matchId in 73..88 -> 4 // 16avos
            matchId in 89..96 -> 5 // Octavos
            matchId in 97..100 -> 6 // Cuartos
            matchId in 101..102 -> 7 // Semis
            matchId == 103 || matchId == 104 -> 8 // Final y 3er puesto
            else -> 9
        }
    }

    private fun calculatePointsForMatch(match: Match): Int {
        if (match.status != "Finished") return 0
        val h = match.homeScore ?: 0
        val a = match.awayScore ?: 0
        val realWinner = when {
            match.id > 100 && h == a -> {
                val hp = match.homePenalties ?: 0
                val ap = match.awayPenalties ?: 0
                if (hp > ap) "L" else if (hp < ap) "V" else "E"
            }
            h > a -> "L"
            h < a -> "V"
            else -> "E"
        }
        var points = 0
        if (match.predictedWinner == realWinner) points += 1
        if (match.predictedHomeScore != null && match.predictedAwayScore != null &&
            match.homeScore == match.predictedHomeScore && match.awayScore == match.predictedAwayScore) {
            points += 2
        }
        return points
    }

    private fun checkRoundRewards(matches: List<Match>) {
        val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        val matchesByRound = matches.groupBy { getMatchRound(it.id) }
        var adFreeTimeToAdd = 0L
        val editor = prefs.edit()

        for (round in 1..8) {
            val roundMatches = matchesByRound[round] ?: continue
            val key = "round_rewarded_$round"
            if (prefs.getBoolean(key, false)) continue

            val allFinished = roundMatches.all { it.status == "Finished" }
            if (allFinished && roundMatches.isNotEmpty()) {
                var roundPoints = 0
                roundMatches.forEach { match ->
                    roundPoints += calculatePointsForMatch(match)
                }
                
                // Premiar: 12 horas por punto
                if (roundPoints > 0) {
                    adFreeTimeToAdd += roundPoints * 12 * 60 * 60 * 1000L
                }
                editor.putBoolean(key, true)
                editor.putBoolean("round_reward_shown_$round", false)
                editor.putInt("round_points_$round", roundPoints)
            }
        }

        if (adFreeTimeToAdd > 0L) {
            val currentAdFreeUntil = prefs.getLong("ad_free_until", System.currentTimeMillis())
            val baseTime = if (currentAdFreeUntil > System.currentTimeMillis()) currentAdFreeUntil else System.currentTimeMillis()
            editor.putLong("ad_free_until", baseTime + adFreeTimeToAdd)
        }
        editor.apply()
        _adFreeUntil.value = prefs.getLong("ad_free_until", 0L)
        checkPendingRewardDialog()
    }

    private fun checkPendingRewardDialog() {
        val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        for (round in 1..8) {
            val rewarded = prefs.getBoolean("round_rewarded_$round", false)
            val shown = prefs.getBoolean("round_reward_shown_$round", true)
            if (rewarded && !shown) {
                val points = prefs.getInt("round_points_$round", 0)
                _pendingRewardDialog.value = RewardDialogInfo(round, points, points * 12)
                break
            }
        }
    }

    fun dismissRewardDialog(round: Int) {
        val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("round_reward_shown_$round", true).apply()
        _pendingRewardDialog.value = null
        checkPendingRewardDialog()
    }

    fun updateMatchPenalties(matchId: Int, homePenalties: Int?, awayPenalties: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            val match = currentState.matches.find { it.id == matchId } ?: return@launch
            
            repository.saveMatchScore(matchId, match.homeScore, match.awayScore, homePenalties, awayPenalties, match.status)
            
            val updatedList = currentState.matches.map {
                if (it.id == matchId) it.copy(homePenalties = homePenalties, awayPenalties = awayPenalties) else it
            }
            val finalKnockout = KnockoutCalculator.calculateKnockoutMatches(updatedList, currentTournamentId.value)
            val allMatches = groupMatchesPlusKnockout(updatedList, finalKnockout)
            _uiState.value = currentState.copy(matches = allMatches, champion = getChampion(allMatches))
            checkRoundRewards(allMatches)
        }
    }

    fun updateMatchPrediction(matchId: Int, winner: String?, homePredict: Int?, awayPredict: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            
            AnalyticsManager.logMatchAction("prediction_updated", matchId, "winner=$winner, score=$homePredict-$awayPredict")
            repository.saveMatchPrediction(matchId, winner, homePredict, awayPredict)
            
            // Sincronizar automáticamente con el servidor si está autenticado
            if (com.example.worldcup2026.data.repository.ProdeRepository.authToken != null) {
                launch {
                    try {
                        val prodeRepo = com.example.worldcup2026.data.repository.ProdeRepository(
                            com.example.worldcup2026.data.local.WorldCupDatabase.getDatabase(getApplication()).leagueDao()
                        )
                        prodeRepo.submitPredictions(listOf(
                            com.example.worldcup2026.data.api.SubmitPredictionRequest(
                                matchId = matchId,
                                predictedHomeScore = homePredict ?: 0,
                                predictedAwayScore = awayPredict ?: 0
                            )
                        ))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val updatedList = currentState.matches.map {
                if (it.id == matchId) it.copy(predictedWinner = winner, predictedHomeScore = homePredict, predictedAwayScore = awayPredict) else it
            }
            _uiState.value = currentState.copy(matches = updatedList)
        }
    }

    fun downloadVipStats(matchId: Int) {
        viewModelScope.launch {
            AnalyticsManager.logMatchAction("vip_stats_opened", matchId)
            loadData()
        }
    }

    private fun groupMatchesPlusKnockout(all: List<Match>, knockout: List<Match>): List<Match> {
        val groupOnes = all.filter { it.id <= 72 }
        return groupOnes + knockout
    }

    private fun startAutoSync(matches: List<Match>) {
        autoSyncJob?.cancel()
        val hasLiveMatches = matches.any { it.status.equals("LIVE", ignoreCase = true) }
        if (hasLiveMatches) {
            autoSyncJob = viewModelScope.launch {
                while (true) {
                    delay(60000) // Cada 60 segundos
                    try {
                        val success = repository.syncMatchesWithLiveJson(getApplication(), currentTournamentId.value)
                        _isServerConnected.value = success
                        if (success) {
                            val updatedMatches = repository.getMatches(currentTournamentId.value)
                            val finalMatches = KnockoutCalculator.calculateKnockoutMatches(updatedMatches, currentTournamentId.value)
                            val allMatches = groupMatchesPlusKnockout(updatedMatches, finalMatches)
                            _uiState.value = WorldCupUiState.Success(allMatches, getChampion(allMatches))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _isServerConnected.value = false
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSyncJob?.cancel()
    }
}
