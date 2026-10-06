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

    private val _globalRanking = MutableStateFlow<List<com.example.worldcup2026.data.api.GlobalRankingUserDto>>(emptyList())
    val globalRanking = _globalRanking.asStateFlow()

    private val _userStats = MutableStateFlow<com.example.worldcup2026.data.api.UserMedalsDto?>(null)
    val userStats = _userStats.asStateFlow()

    private val _isRankingLoading = MutableStateFlow(false)
    val isRankingLoading = _isRankingLoading.asStateFlow()

    private val _bonusNotification = MutableStateFlow<String?>(null)
    val bonusNotification = _bonusNotification.asStateFlow()

    fun dismissBonusNotification() {
        _bonusNotification.value = null
    }

    init {
        loadMatches()
        checkExistingSession()
    }

    fun loadGlobalRanking() {
        viewModelScope.launch {
            _isRankingLoading.value = true
            try {
                _globalRanking.value = prodeRepository.getGlobalRanking()
                _userStats.value = prodeRepository.getUserStats()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRankingLoading.value = false
            }
        }
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    firebaseUser.getIdToken(true).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result?.token
                            if (token != null) {
                                handleSignIn(token)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
                val user = prodeRepository.currentUser
                _currentUser.value = user
                prodeRepository.fetchMyLeagues()
                loadGlobalRanking()
                
                // Restaurar favoritos desde la nube al perfil del usuario
                if (user != null) {
                    val prefs = getApplication<android.app.Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
                    val cloudTournaments = user.favoriteTournaments
                    val cloudTeams = user.favoriteTeams
                    if (!cloudTournaments.isNullOrEmpty()) {
                        prefs.edit().putStringSet("favorite_tournament_ids", cloudTournaments.map { it.toString() }.toSet()).apply()
                    }
                    if (!cloudTeams.isNullOrEmpty()) {
                        prefs.edit().putStringSet("favorite_team_names", cloudTeams.toSet()).apply()
                    }
                }

                // Descargar y restaurar predicciones guardadas en el servidor
                launch {
                    try {
                        prodeRepository.fetchMyPredictions(worldCupRepository, getApplication())
                        loadMatches()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // Sincronizar pronósticos locales existentes con el servidor al iniciar sesión si hubiera nuevos
                syncAllLocalPredictions()

                // Bono de bienvenida (24 horas sin anuncios, único por cuenta)
                launch {
                    val bonusRes = prodeRepository.claimWelcomeBonus()
                    if (bonusRes != null && bonusRes.success && bonusRes.grantedHours > 0) {
                        val prefs = getApplication<android.app.Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
                        val curAdFree = prefs.getLong("ad_free_until", System.currentTimeMillis())
                        val baseTime = if (curAdFree > System.currentTimeMillis()) curAdFree else System.currentTimeMillis()
                        val newUntil = baseTime + (bonusRes.grantedHours * 3600 * 1000L)
                        prefs.edit().putLong("ad_free_until", newUntil).apply()
                        _bonusNotification.value = "🎁 ¡Bono de Bienvenida! Se activaron ${bonusRes.grantedHours} horas sin anuncios en tu cuenta."
                    }
                }

                // Referido pendiente desde deep-link / link de descarga
                launch {
                    val prefs = getApplication<android.app.Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
                    val pendingRef = prefs.getString("pending_referral_code", null)
                    if (!pendingRef.isNullOrBlank()) {
                        val refRes = prodeRepository.applyReferral(pendingRef)
                        if (refRes != null && refRes.success) {
                            prefs.edit().remove("pending_referral_code").apply()
                            if (refRes.userBonusHours > 0) {
                                val curAdFree = prefs.getLong("ad_free_until", System.currentTimeMillis())
                                val baseTime = if (curAdFree > System.currentTimeMillis()) curAdFree else System.currentTimeMillis()
                                val newUntil = baseTime + (refRes.userBonusHours * 3600 * 1000L)
                                prefs.edit().putLong("ad_free_until", newUntil).apply()
                                _bonusNotification.value = "🎉 ¡Código de referido aplicado! Recibiste +${refRes.userBonusHours} horas sin anuncios."
                            }
                        }
                    }
                }
            }
        }
    }

    fun syncAllLocalPredictions() {
        viewModelScope.launch {
            try {
                val matches = worldCupRepository.getAllMatchesGlobal()
                val localPredictions = matches.filter { it.predictedHomeScore != null && it.predictedAwayScore != null }
                    .map { SubmitPredictionRequest(it.id, it.predictedHomeScore ?: 0, it.predictedAwayScore ?: 0, it.predictedHomePenalties, it.predictedAwayPenalties, it.predictedWinner) }
                if (localPredictions.isNotEmpty()) {
                    prodeRepository.submitPredictions(localPredictions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _leagueSummaryDialog = MutableStateFlow<com.example.worldcup2026.data.api.LeagueDto?>(null)
    val leagueSummaryDialog = _leagueSummaryDialog.asStateFlow()

    fun dismissLeagueSummary() {
        _leagueSummaryDialog.value = null
    }

    fun logout() {
        prodeRepository.logout()
        _isAuthenticated.value = false
        _currentUser.value = null
    }

    fun signOut() {
        logout()
    }

    fun createLeague(
        name: String,
        mode: String = "FULL_TOURNAMENT",
        tournamentId: Int? = 5,
        startMatchday: Int? = 1,
        endMatchday: Int? = 5,
        startDate: String? = null,
        endDate: String? = null,
        customPrize: String? = null,
        tournamentConfigs: String? = null,
        includePostponed: Boolean = true,
        onSuccess: ((com.example.worldcup2026.data.api.LeagueDto) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val dto = prodeRepository.createLeague(name, mode, tournamentId, startMatchday, endMatchday, startDate, endDate, customPrize, tournamentConfigs, includePostponed)
            if (dto != null) {
                _leagueSummaryDialog.value = dto
                onSuccess?.invoke(dto)
            }
        }
    }

    fun joinLeague(code: String, onSuccess: ((com.example.worldcup2026.data.api.LeagueDto) -> Unit)? = null) {
        viewModelScope.launch {
            val dto = prodeRepository.joinLeague(code)
            if (dto != null) {
                _leagueSummaryDialog.value = dto
                onSuccess?.invoke(dto)
            }
        }
    }

    fun claimPoints(points: Int, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val res = prodeRepository.claimPoints(points)
            if (res != null && res.success) {
                loadGlobalRanking()
                onSuccess?.invoke()
            }
        }
    }

    fun claimWelcomeBonus(onResult: ((Boolean, String, Int) -> Unit)? = null) {
        viewModelScope.launch {
            val res = prodeRepository.claimWelcomeBonus()
            if (res != null) {
                onResult?.invoke(res.success, res.message, res.grantedHours)
            }
        }
    }

    fun applyReferral(code: String, onResult: ((Boolean, String, Int) -> Unit)? = null) {
        viewModelScope.launch {
            val res = prodeRepository.applyReferral(code)
            if (res != null) {
                if (res.success && res.userBonusHours > 0) {
                    val prefs = getApplication<android.app.Application>().getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE)
                    val curAdFree = prefs.getLong("ad_free_until", System.currentTimeMillis())
                    val baseTime = if (curAdFree > System.currentTimeMillis()) curAdFree else System.currentTimeMillis()
                    val newUntil = baseTime + (res.userBonusHours * 3600 * 1000L)
                    prefs.edit().putLong("ad_free_until", newUntil).apply()
                }
                onResult?.invoke(res.success, res.message, res.userBonusHours)
            }
        }
    }

    fun deleteLeague(leagueId: String) {
        viewModelScope.launch {
            prodeRepository.deleteLeague(leagueId)
        }
    }

    suspend fun getStandings(leagueId: String): List<com.example.worldcup2026.data.api.StandingDto> {
        return prodeRepository.getStandings(leagueId)
    }

    suspend fun getMemberBreakdown(leagueId: String, memberUserId: String): List<com.example.worldcup2026.data.api.MatchBreakdownDto> {
        return prodeRepository.getMemberBreakdown(leagueId, memberUserId)
    }
}
