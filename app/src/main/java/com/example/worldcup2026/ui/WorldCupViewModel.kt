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
    
    private val _isVip = mutableStateOf(false)
    val isVip: State<Boolean> = _isVip

    private val _pendingClaimableRounds = mutableStateOf<List<Int>>(emptyList())
    val pendingClaimableRounds: State<List<Int>> = _pendingClaimableRounds

    // Estado para registrar torneos en vivo (ID del torneo -> si tiene partidos en vivo)
    private val _liveTournaments = mutableStateOf<Map<Int, Boolean>>(emptyMap())
    val liveTournaments: State<Map<Int, Boolean>> = _liveTournaments

    data class RewardDialogInfo(val round: Int, val points: Int, val hours: Int)
    private val _pendingRewardDialog = mutableStateOf<RewardDialogInfo?>(null)
    val pendingRewardDialog: State<RewardDialogInfo?> = _pendingRewardDialog
    
    private val _celebrationMatch = mutableStateOf<Match?>(null)
    val celebrationMatch: State<Match?> = _celebrationMatch
    
    private var autoSyncJob: kotlinx.coroutines.Job? = null

    private val _favoriteTournamentIds = mutableStateOf<Set<Int>>(setOf(5))
    val favoriteTournamentIds: State<Set<Int>> = _favoriteTournamentIds

    private val _favoriteTeamNames = mutableStateOf<Set<String>>(emptySet())
    val favoriteTeamNames: State<Set<String>> = _favoriteTeamNames

    init {
        val database = WorldCupDatabase.getDatabase(application)
        repository = WorldCupRepository(database.matchDao())
        val prefs = application.getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        _adFreeUntil.value = prefs.getLong("ad_free_until", 0L)
        _isVip.value = prefs.getBoolean("is_vip_status", false)
        
        val favTournamentsSaved = prefs.getStringSet("favorite_tournament_ids", null)
        if (favTournamentsSaved != null) {
            _favoriteTournamentIds.value = favTournamentsSaved.mapNotNull { it.toIntOrNull() }.toSet()
        } else {
            val singleFav = prefs.getInt("favorite_tournament_id", 5)
            _favoriteTournamentIds.value = setOf(singleFav)
        }
        
        _favoriteTeamNames.value = prefs.getStringSet("favorite_team_names", emptySet()) ?: emptySet()

        val primaryFav = _favoriteTournamentIds.value.firstOrNull() ?: 5
        currentTournamentId.value = primaryFav
        loadData()
        checkPendingRewardDialog()
        checkClaimableRounds()
        startLiveTournamentsChecker()
    }

    fun toggleFavoriteTournament(tournamentId: Int) {
        val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        val currentSet = _favoriteTournamentIds.value.toMutableSet()
        if (currentSet.contains(tournamentId)) {
            currentSet.remove(tournamentId)
        } else {
            currentSet.add(tournamentId)
        }
        _favoriteTournamentIds.value = currentSet
        prefs.edit().putStringSet("favorite_tournament_ids", currentSet.map { it.toString() }.toSet()).apply()
    }

    fun toggleFavoriteTeam(teamName: String) {
        val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        val currentSet = _favoriteTeamNames.value.toMutableSet()
        if (currentSet.contains(teamName)) {
            currentSet.remove(teamName)
        } else {
            currentSet.add(teamName)
        }
        _favoriteTeamNames.value = currentSet
        prefs.edit().putStringSet("favorite_team_names", currentSet).apply()
    }

    private var cachedGlobalMatches: List<Match> = emptyList()

    fun getCurrentMatchdayForTournament(tournamentId: Int): Int {
        val matches = if (cachedGlobalMatches.isNotEmpty()) {
            cachedGlobalMatches
        } else {
            val state = _uiState.value
            if (state is WorldCupUiState.Success) state.matches else emptyList()
        }

        val tournamentMatches = matches.filter { it.tournament_id == tournamentId }

        val upcoming = tournamentMatches
            .filter { it.status == "Scheduled" || it.status == "LIVE" }
            .sortedBy { it.date }
        
        val firstUpcoming = upcoming.firstOrNull()
        if (firstUpcoming != null && firstUpcoming.matchday != null && firstUpcoming.matchday > 0) {
            return firstUpcoming.matchday
        }

        // Estimación dinámica a partir de partidos finalizados si matchday no viene explícito
        if (tournamentId == 5) {
            val finishedCount = tournamentMatches.count { it.status == "Finished" }
            val estimatedMatchday = (finishedCount / 15) + 1
            if (estimatedMatchday in 1..30) {
                return estimatedMatchday
            }
        }

        // Defaults actualizados con las fechas activas reales por torneo:
        return when (tournamentId) {
            5 -> 5   // Liga Profesional (Fecha 5)
            7, 8 -> 25  // Primera Nacional (Fecha 25)
            8, 9 -> 30  // Primera B Metropolitana (Fecha 30)
            9, 10 -> 24 // Primera C (Fecha 24)
            13 -> 11 // Promocional Amateur (Fecha 11 Zona A / 14 Zona B)
            3 -> 7   // Copa Libertadores (Octavos de Final)
            4 -> 7   // Copa Sudamericana (Octavos de Final)
            6 -> 4   // Copa Argentina (Octavos)
            2 -> 7   // Eliminatorias Conmebol
            else -> 1
        }
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
                        val globalMatches = repository.getAllMatchesGlobal()
                        cachedGlobalMatches = globalMatches
                        val worldCupMatches = globalMatches.filter { it.tournament_id == currentTournamentId.value }
                        val finalMatches = KnockoutCalculator.calculateKnockoutMatches(worldCupMatches, currentTournamentId.value)
                        val allMatches = groupMatchesPlusKnockout(globalMatches, finalMatches, currentTournamentId.value)
                        _uiState.value = WorldCupUiState.Success(allMatches, getChampion(allMatches))
                        checkRoundRewards(allMatches)
                        com.example.worldcup2026.data.util.MatchReminderScheduler.scheduleRemindersForMatches(getApplication(), allMatches)
                        startAutoSync(allMatches)
                    }
                }

                val globalMatches = repository.getAllMatchesGlobal()
                cachedGlobalMatches = globalMatches
                val worldCupMatches = globalMatches.filter { it.tournament_id == currentTournamentId.value }
                val finalMatches = KnockoutCalculator.calculateKnockoutMatches(worldCupMatches, currentTournamentId.value)
                val allMatches = groupMatchesPlusKnockout(globalMatches, finalMatches, currentTournamentId.value)
                _uiState.value = WorldCupUiState.Success(allMatches, getChampion(allMatches))
                checkRoundRewards(allMatches)
                com.example.worldcup2026.data.util.MatchReminderScheduler.scheduleRemindersForMatches(getApplication(), allMatches)
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
                    val globalMatches = repository.getAllMatchesGlobal()
                    val worldCupMatches = globalMatches.filter { it.tournament_id == currentTournamentId.value }
                    val finalMatches = KnockoutCalculator.calculateKnockoutMatches(worldCupMatches, currentTournamentId.value)
                    val allMatches = groupMatchesPlusKnockout(globalMatches, finalMatches, currentTournamentId.value)
                    _uiState.value = WorldCupUiState.Success(allMatches, getChampion(allMatches))
                    checkRoundRewards(allMatches)
                    com.example.worldcup2026.data.util.MatchReminderScheduler.scheduleRemindersForMatches(getApplication(), allMatches)
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
        val currentT = currentTournamentId.value
        // El festejo de Campeón a pantalla completa es EXCLUSIVO para la Final del Mundial o Libertadores
        if (currentT != 1 && currentT != 2) return null

        val finalRound = com.example.worldcup2026.data.util.TournamentConfig.KNOCKOUT_ROUNDS.find { it.name.equals("FINAL", ignoreCase = true) }
        val finalMatchId = finalRound?.endId ?: return null
        val finalMatch = matches.find { it.id == finalMatchId && it.tournament_id == currentT }
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
            val worldCupMatches = updatedList.filter { it.tournament_id == currentTournamentId.value }
            val finalKnockout = KnockoutCalculator.calculateKnockoutMatches(worldCupMatches, currentTournamentId.value)
            val allMatches = groupMatchesPlusKnockout(updatedList, finalKnockout, currentTournamentId.value)
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
            val worldCupMatches = updatedList.filter { it.tournament_id == currentTournamentId.value }
            val finalKnockout = KnockoutCalculator.calculateKnockoutMatches(worldCupMatches, currentTournamentId.value)
            val allMatches = groupMatchesPlusKnockout(updatedList, finalKnockout, currentTournamentId.value)
            _uiState.value = currentState.copy(matches = allMatches, champion = getChampion(allMatches))
            checkRoundRewards(allMatches)
        }
    }

    private fun getMatchRound(matchId: Int): Int {
        if (matchId <= 0) return 0
        
        val config = com.example.worldcup2026.data.util.TournamentConfig.KNOCKOUT_ROUNDS
        val roundIndex = config.indexOfFirst { matchId in it.startId..it.endId }
        
        if (roundIndex != -1) {
            return if (com.example.worldcup2026.data.util.TournamentConfig.IS_WORLD_CUP) {
                4 + roundIndex
            } else {
                10 + roundIndex
            }
        }

        val groupMatchesEnd = config.firstOrNull()?.startId?.minus(1) ?: 72
        if (matchId <= groupMatchesEnd) {
            val perRound = groupMatchesEnd / 3
            return when {
                matchId <= perRound -> 1
                matchId <= perRound * 2 -> 2
                else -> 3
            }
        }
        return 9
    }    private fun calculatePointsForMatch(match: Match): Int {
        if (match.status != "Finished") return 0
        val h = match.homeScore ?: 0
        val a = match.awayScore ?: 0
        val realWinner = when {
            match.id > 116 && h == a -> {
                val hp = match.homePenalties ?: 0
                val ap = match.awayPenalties ?: 0
                if (hp > ap) "L" else if (hp < ap) "V" else "E"
            }
            h > a -> "L"
            h < a -> "V"
            else -> "E"
        }
        var points = 0
        var isWinnerHit = false
        val predW = match.predictedWinner ?: ""
        if (predW.isNotEmpty()) {
            if (predW.contains(",")) {
                val choices = predW.split(",").map { it.trim() }
                if (choices.contains(realWinner)) {
                    points += 1 // 1 punto por acierto en apuesta doble
                    isWinnerHit = true
                }
            } else if (predW.trim() == realWinner) {
                points += 2 // 2 puntos por acierto en apuesta simple
                isWinnerHit = true
            }
        }

        if (match.predictedHomeScore != null && match.predictedAwayScore != null) {
            if (match.homeScore == match.predictedHomeScore && match.awayScore == match.predictedAwayScore) {
                points += 3 // 3 puntos por resultado de marcador exacto
            } else if (isWinnerHit) {
                // +2 puntos por misma diferencia de gol al acertar el ganador o empate
                val diffReal = h - a
                val diffPred = match.predictedHomeScore!! - match.predictedAwayScore!!
                if (diffReal == diffPred) {
                    points += 2
                }
            }
        }

        if (match.is_featured) {
            points *= 2
        }

        return points
    }

    private fun checkRoundRewards(matches: List<Match>) {
        val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        val matchesByRound = matches.groupBy { getMatchRound(it.id) }
        val editor = prefs.edit()
        var hasChanges = false
        
        matchesByRound.forEach { (round, roundMatches) ->
            if (round <= 0) return@forEach
            val keyReady = "round_ready_to_claim_$round"
            val keyRewarded = "round_rewarded_$round"
            
            // Ya fue reclamado
            if (prefs.getBoolean(keyRewarded, false)) return@forEach
            
            // Ya está listo para reclamar (pero no lo reclamó aún)
            if (prefs.getBoolean(keyReady, false)) return@forEach

            val allFinished = roundMatches.all { it.status == "Finished" }
            if (allFinished && roundMatches.isNotEmpty()) {
                var roundPoints = 0
                roundMatches.forEach { match ->
                    roundPoints += calculatePointsForMatch(match)
                }
                
                editor.putBoolean(keyReady, true)
                editor.putInt("round_points_$round", roundPoints)
                hasChanges = true
            }
        }
        
        if (hasChanges) {
            editor.apply()
            checkClaimableRounds()
        }
    }

    private fun checkClaimableRounds() {
        val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        val claimables = mutableListOf<Int>()
        for (round in 1..8) {
            if (prefs.getBoolean("round_ready_to_claim_$round", false) && !prefs.getBoolean("round_rewarded_$round", false)) {
                claimables.add(round)
            }
        }
        _pendingClaimableRounds.value = claimables
    }
    
    fun claimReward(round: Int) {
        val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        val points = prefs.getInt("round_points_$round", 0)
        val maxPossible = prefs.getInt("round_max_points_$round", 0)
        
        val editor = prefs.edit()
        var adFreeTimeToAdd = 0L
        if (maxPossible > 0 && points >= maxPossible) {
            adFreeTimeToAdd = 7 * 24 * 60 * 60 * 1000L // 1 semana (7 días) sin publicidad por fecha perfecta
        } else if (points > 0) {
            adFreeTimeToAdd = points * 22 * 60 * 1000L // 22 minutos sin publicidad por cada punto obtenido
        }
        
        editor.putBoolean("round_rewarded_$round", true)
        editor.putBoolean("round_reward_shown_$round", false)
        
        if (adFreeTimeToAdd > 0L) {
            val currentAdFreeUntil = prefs.getLong("ad_free_until", System.currentTimeMillis())
            val baseTime = if (currentAdFreeUntil > System.currentTimeMillis()) currentAdFreeUntil else System.currentTimeMillis()
            editor.putLong("ad_free_until", baseTime + adFreeTimeToAdd)
        }
        editor.apply()
        
        _adFreeUntil.value = prefs.getLong("ad_free_until", 0L)
        checkClaimableRounds()
        checkPendingRewardDialog()
    }
    
    fun toggleVipStatus() {
        val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        val newVipStatus = !_isVip.value
        prefs.edit().putBoolean("is_vip_status", newVipStatus).apply()
        _isVip.value = newVipStatus
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
            val worldCupMatches = updatedList.filter { it.tournament_id == currentTournamentId.value }
            val finalKnockout = KnockoutCalculator.calculateKnockoutMatches(worldCupMatches, currentTournamentId.value)
            val allMatches = groupMatchesPlusKnockout(updatedList, finalKnockout, currentTournamentId.value)
            _uiState.value = currentState.copy(matches = allMatches, champion = getChampion(allMatches))
            checkRoundRewards(allMatches)
        }
    }

    fun toggleComodin(matchId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            val targetMatch = currentState.matches.find { it.id == matchId } ?: return@launch
            val targetMatchday = targetMatch.matchday ?: 1
            val targetTournament = targetMatch.tournament_id ?: currentTournamentId.value

            val willBeFeatured = !targetMatch.is_featured

            // Persistir comodín en SharedPreferences
            val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
            val keyDate = "comodin_${targetTournament}_${targetMatchday}"
            if (willBeFeatured) {
                prefs.edit()
                    .putInt(keyDate, matchId)
                    .putBoolean("comodin_match_${matchId}", true)
                    .apply()
            } else {
                prefs.edit()
                    .remove(keyDate)
                    .remove("comodin_match_${matchId}")
                    .apply()
            }

            val updatedList = currentState.matches.map { m ->
                val mDay = m.matchday ?: 1
                val mT = m.tournament_id ?: currentTournamentId.value
                if (mT == targetTournament && mDay == targetMatchday) {
                    if (m.id == matchId) {
                        m.copy(is_featured = willBeFeatured)
                    } else {
                        if (willBeFeatured && m.is_featured) {
                            prefs.edit().remove("comodin_match_${m.id}").apply()
                        }
                        m.copy(is_featured = false)
                    }
                } else m
            }

            _uiState.value = currentState.copy(matches = updatedList)

            if (com.example.worldcup2026.data.repository.ProdeRepository.authToken != null) {
                launch {
                    try {
                        val prodeRepo = com.example.worldcup2026.data.repository.ProdeRepository(
                            com.example.worldcup2026.data.local.WorldCupDatabase.getDatabase(getApplication()).leagueDao()
                        )
                        val matchToSync = updatedList.find { it.id == matchId } ?: targetMatch
                        prodeRepo.submitPredictions(listOf(
                            com.example.worldcup2026.data.api.SubmitPredictionRequest(
                                matchId = matchId,
                                predictedHomeScore = matchToSync.predictedHomeScore ?: 0,
                                predictedAwayScore = matchToSync.predictedAwayScore ?: 0,
                                predictedHomePenalties = matchToSync.predictedHomePenalties,
                                predictedAwayPenalties = matchToSync.predictedAwayPenalties,
                                predictedWinner = matchToSync.predictedWinner,
                                isDoublePointsMultiplier = willBeFeatured
                            )
                        ))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun updateMatchPrediction(matchId: Int, winner: String?, homePredict: Int?, awayPredict: Int?, homePenaltiesPredict: Int? = null, awayPenaltiesPredict: Int? = null) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success ?: return@launch
            val currentMatch = currentState.matches.find { it.id == matchId }
            val isFeatured = currentMatch?.is_featured ?: false
            
            AnalyticsManager.logMatchAction("prediction_updated", matchId, "winner=$winner, score=$homePredict-$awayPredict, pens=$homePenaltiesPredict-$awayPenaltiesPredict")
            repository.saveMatchPrediction(matchId, winner, homePredict, awayPredict, homePenaltiesPredict, awayPenaltiesPredict)
            
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
                                predictedAwayScore = awayPredict ?: 0,
                                predictedHomePenalties = homePenaltiesPredict,
                                predictedAwayPenalties = awayPenaltiesPredict,
                                predictedWinner = winner,
                                isDoublePointsMultiplier = isFeatured
                            )
                        ))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val updatedList = currentState.matches.map {
                if (it.id == matchId) it.copy(
                    predictedWinner = winner, 
                    predictedHomeScore = homePredict, 
                    predictedAwayScore = awayPredict, 
                    predictedHomePenalties = homePenaltiesPredict, 
                    predictedAwayPenalties = awayPenaltiesPredict, 
                    is_featured = isFeatured
                ) else it
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
    
    fun triggerCelebration(matchId: Int, fallbackMatch: Match? = null) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorldCupUiState.Success
            val currentMatches = currentState?.matches ?: emptyList()
            var match = currentMatches.find { it.id == matchId }
            if (match == null) {
                val global = repository.getAllMatchesGlobal()
                match = global.find { it.id == matchId }
            }
            if (match == null) {
                match = fallbackMatch
            }
            if (match != null) {
                _celebrationMatch.value = match
            }
        }
    }
    
    fun dismissCelebration() {
        _celebrationMatch.value = null
    }

    private fun applyComodinPersistence(matches: List<Match>): List<Match> {
        val prefs = getApplication<Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
        return matches.map { match ->
            val tId = match.tournament_id ?: currentTournamentId.value
            val mDay = match.matchday ?: 1
            val savedComodinMatchId = prefs.getInt("comodin_${tId}_${mDay}", -1)
            val isMatchComodin = prefs.getBoolean("comodin_match_${match.id}", false)
            if (savedComodinMatchId == match.id || isMatchComodin) {
                match.copy(is_featured = true)
            } else if (savedComodinMatchId != -1 && match.is_featured) {
                match.copy(is_featured = false)
            } else {
                match
            }
        }
    }

    private fun groupMatchesPlusKnockout(all: List<Match>, knockout: List<Match>, tournamentId: Int): List<Match> {
        val combined = if (tournamentId == 1) {
            val nonWorldCupKnockouts = all.filter { it.tournament_id != 1 || it.id <= 72 }
            nonWorldCupKnockouts + knockout
        } else {
            all
        }
        return applyComodinPersistence(combined.distinctBy { it.id })
    }

    private fun startAutoSync(matches: List<Match>) {
        autoSyncJob?.cancel()
        val hasLiveMatches = matches.any { it.status.equals("LIVE", ignoreCase = true) }
        val hasUnfinishedMatches = matches.any { !it.status.equals("Finished", ignoreCase = true) }
        
        if (hasUnfinishedMatches) {
            autoSyncJob = viewModelScope.launch {
                while (true) {
                    // Poll cada 15 segundos si hay en vivo, o cada 2 minutos si no
                    delay(if (hasLiveMatches) 15000L else 120000L) 
                    try {
                        val oldList = (_uiState.value as? WorldCupUiState.Success)?.matches ?: emptyList()
                        val success = repository.syncMatchesWithLiveJson(getApplication(), currentTournamentId.value)
                        _isServerConnected.value = success
                        if (success) {
                            val globalMatches = repository.getAllMatchesGlobal()
                            val worldCupMatches = globalMatches.filter { it.tournament_id == currentTournamentId.value }
                            val finalMatches = KnockoutCalculator.calculateKnockoutMatches(worldCupMatches, currentTournamentId.value)
                            val allMatches = groupMatchesPlusKnockout(globalMatches, finalMatches, currentTournamentId.value)
                            _uiState.value = WorldCupUiState.Success(allMatches, getChampion(allMatches))
                            
                            // Detectar automáticamente goles o final de partido para disparar los festejos
                            for (newM in allMatches) {
                                val oldM = oldList.find { it.id == newM.id } ?: continue
                                val oldH = oldM.homeScore ?: 0
                                val oldA = oldM.awayScore ?: 0
                                val newH = newM.homeScore ?: 0
                                val newA = newM.awayScore ?: 0
                                val goalScored = newH > oldH || newA > oldA
                                val matchJustFinished = oldM.status != "Finished" && newM.status == "Finished"
                                if (goalScored || matchJustFinished) {
                                    triggerCelebration(newM.id)
                                }
                            }

                            // Re-evaluar intervalo
                            startAutoSync(allMatches)
                            break
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _isServerConnected.value = false
                    }
                }
            }
        }
    }

    private fun startLiveTournamentsChecker() {
        viewModelScope.launch {
            val database = WorldCupDatabase.getDatabase(getApplication())
            // Escuchar cambios de todos los partidos para actualizar los indicadores "EN VIVO" de los torneos
            database.matchDao().getAllMatches().collect { allEntities ->
                val liveMap = mutableMapOf<Int, Boolean>()
                // Clasificamos si hay algún partido LIVE en los torneos
                val grouped = allEntities.groupBy { it.tournamentId }
                for (tourneyId in listOf(1, 3, 5, 6, 8)) {
                    val hasLive = grouped[tourneyId]?.any { it.status.equals("LIVE", ignoreCase = true) } ?: false
                    liveMap[tourneyId] = hasLive
                }
                _liveTournaments.value = liveMap
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSyncJob?.cancel()
    }
}

