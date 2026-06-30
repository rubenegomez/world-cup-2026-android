package com.example.worldcup2026.data.repository

import com.example.worldcup2026.data.local.MatchDao
import com.example.worldcup2026.data.local.MatchEntity
import com.example.worldcup2026.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WorldCupRepository(private val matchDao: MatchDao) {
    private var cachedMatches: List<Match>? = null

    private suspend fun getCachedMatch(matchId: Int): Match? {
        if (cachedMatches == null) {
            getMatches()
        }
        return cachedMatches?.find { it.id == matchId }
    }

    private fun parseRedCardsFromVipStats(vipStats: String?): Pair<Int, Int> {
        if (vipStats.isNullOrEmpty()) return Pair(0, 0)
        try {
            val parts = vipStats.split("|")
            val redPart = parts.find { it.startsWith("red:") } ?: return Pair(0, 0)
            val values = redPart.substringAfter("red:").split(",")
            if (values.size >= 2) {
                val homeRed = values[0].toIntOrNull() ?: 0
                val awayRed = values[1].toIntOrNull() ?: 0
                return Pair(homeRed, awayRed)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(0, 0)
    }

    private fun parseYellowCardsFromVipStats(vipStats: String?): Pair<Int, Int> {
        if (vipStats.isNullOrEmpty()) return Pair(0, 0)
        try {
            val parts = vipStats.split("|")
            val yellowPart = parts.find { it.startsWith("yellow:") } ?: return Pair(0, 0)
            val values = yellowPart.substringAfter("yellow:").split(",")
            if (values.size >= 2) {
                val homeYellow = values[0].toIntOrNull() ?: 0
                val awayYellow = values[1].toIntOrNull() ?: 0
                return Pair(homeYellow, awayYellow)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(0, 0)
    }

    suspend fun getMockGroups(): List<Group> {
        return com.example.worldcup2026.data.api.NetworkModule.apiService.getGroups()
    }

    private fun createTeam(id: Int, name: String, flagCode: String, group: String): Team {
        return Team(id, name, "https://flagcdn.com/w160/$flagCode.png", group, emptyList())
    }

    private fun createPlaceholderTeam(name: String): Team {
        return Team(-1, name, "", "Final", emptyList())
    }

    suspend fun getMatches(): List<Match> {
        val savedMatches = matchDao.getAllMatches().first()
        val matches = mutableListOf<Match>()
        
        try {
            val remoteMatches = com.example.worldcup2026.data.api.NetworkModule.apiService.getMatches()
            remoteMatches.forEach { match ->
                addMatchWithPersistence(matches, savedMatches, match)
            }
            cachedMatches = matches
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return matches
    }

    private suspend fun addMatchWithPersistence(targetList: MutableList<Match>, savedEntities: List<MatchEntity>, baseMatch: Match) {
        val saved = savedEntities.find { it.id == baseMatch.id }
        if (saved != null) {
            val homeScore = baseMatch.homeScore ?: saved.homeScore
            val awayScore = baseMatch.awayScore ?: saved.awayScore
            val homePenalties = baseMatch.homePenalties ?: saved.homePenalties
            val awayPenalties = baseMatch.awayPenalties ?: saved.awayPenalties
            val status = if (baseMatch.status != "Scheduled") baseMatch.status else saved.status

            val homePossession = baseMatch.homePossession ?: saved.homePossession
            val awayPossession = baseMatch.awayPossession ?: saved.awayPossession
            val homeShots = baseMatch.homeShots ?: saved.homeShots
            val awayShots = baseMatch.awayShots ?: saved.awayShots
            val scorers = if (baseMatch.scorers.isNotEmpty()) baseMatch.scorers else (if (saved.scorers.isNullOrEmpty()) emptyList() else saved.scorers.split("|"))
            val events = if (baseMatch.events.isNotEmpty()) baseMatch.events else (if (saved.events.isNullOrEmpty()) emptyList() else saved.events.split("|"))
            val vipStats = baseMatch.vipStats ?: saved.vipStats
            val clock = baseMatch.clock ?: saved.clock

            targetList.add(baseMatch.copy(
                homeScore = homeScore,
                awayScore = awayScore,
                homePenalties = homePenalties,
                awayPenalties = awayPenalties,
                status = status,
                predictedWinner = saved.predictedWinner,
                predictedHomeScore = saved.predictedHomeScore,
                predictedAwayScore = saved.predictedAwayScore,
                homePossession = homePossession,
                awayPossession = awayPossession,
                homeShots = homeShots,
                awayShots = awayShots,
                scorers = scorers,
                events = events,
                vipStats = vipStats,
                clock = clock
            ))

            // Si detectamos discrepancia o datos nuevos de resultado real/penales, actualizamos en la DB de Room
            if (saved.homeScore != homeScore ||
                saved.awayScore != awayScore ||
                saved.homePenalties != homePenalties ||
                saved.awayPenalties != awayPenalties ||
                saved.status != status ||
                saved.vipStats != vipStats ||
                saved.clock != clock
            ) {
                matchDao.insertMatch(MatchEntity(
                    id = baseMatch.id,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    homePenalties = homePenalties,
                    awayPenalties = awayPenalties,
                    status = status,
                    predictedWinner = saved.predictedWinner,
                    predictedHomeScore = saved.predictedHomeScore,
                    predictedAwayScore = saved.predictedAwayScore,
                    homePossession = homePossession,
                    awayPossession = awayPossession,
                    homeShots = homeShots,
                    awayShots = awayShots,
                    scorers = if (scorers.isEmpty()) null else scorers.joinToString("|"),
                    events = if (events.isEmpty()) null else events.joinToString("|"),
                    vipStats = vipStats,
                    clock = clock
                ))
            }
        } else {
            targetList.add(baseMatch)
        }
    }

    suspend fun saveMatchScore(matchId: Int, homeScore: Int?, awayScore: Int?, homePenalties: Int? = null, awayPenalties: Int? = null, status: String? = null) {
        val saved = matchDao.getAllMatches().first().find { it.id == matchId }
        val finalStatus = status ?: if (homeScore != null && awayScore != null) "Finished" else "Scheduled"
        matchDao.insertMatch(MatchEntity(
            matchId, 
            homeScore, 
            awayScore, 
            homePenalties, 
            awayPenalties, 
            finalStatus,
            predictedWinner = saved?.predictedWinner,
            predictedHomeScore = saved?.predictedHomeScore,
            predictedAwayScore = saved?.predictedAwayScore,
            homePossession = saved?.homePossession,
            awayPossession = saved?.awayPossession,
            homeShots = saved?.homeShots,
            awayShots = saved?.awayShots,
            scorers = saved?.scorers,
            events = saved?.events,
            vipStats = saved?.vipStats,
            clock = saved?.clock
        ))
    }

    suspend fun saveMatchPrediction(matchId: Int, winner: String?, homePredict: Int?, awayPredict: Int?) {
        val saved = matchDao.getAllMatches().first().find { it.id == matchId }
        matchDao.insertMatch(MatchEntity(
            matchId,
            saved?.homeScore,
            saved?.awayScore,
            saved?.homePenalties,
            saved?.awayPenalties,
            saved?.status ?: "Scheduled",
            predictedWinner = winner,
            predictedHomeScore = homePredict,
            predictedAwayScore = awayPredict,
            homePossession = saved?.homePossession,
            awayPossession = saved?.awayPossession,
            homeShots = saved?.homeShots,
            awayShots = saved?.awayShots,
            scorers = saved?.scorers,
            events = saved?.events,
            vipStats = saved?.vipStats,
            clock = saved?.clock
        ))
    }

    suspend fun saveMatchStatus(matchId: Int, status: String) {
        val saved = matchDao.getAllMatches().first().find { it.id == matchId }
        val home = if (status == "Finished") (saved?.homeScore ?: 0) else saved?.homeScore
        val away = if (status == "Finished") (saved?.awayScore ?: 0) else saved?.awayScore
        matchDao.insertMatch(MatchEntity(
            matchId, 
            home, 
            away, 
            saved?.homePenalties, 
            saved?.awayPenalties, 
            status,
            predictedWinner = saved?.predictedWinner,
            predictedHomeScore = saved?.predictedHomeScore,
            predictedAwayScore = saved?.predictedAwayScore,
            homePossession = saved?.homePossession,
            awayPossession = saved?.awayPossession,
            homeShots = saved?.homeShots,
            awayShots = saved?.awayShots,
            scorers = saved?.scorers,
            events = saved?.events,
            vipStats = saved?.vipStats,
            clock = saved?.clock
        ))
    }

    suspend fun syncMatchesWithLiveJson(context: android.content.Context): Boolean {
        try {
            val service = com.example.worldcup2026.data.api.NetworkModule.apiService
            val matchesList = service.getLiveMatches()
            
            val savedMatches = matchDao.getAllMatches().first()
            
            matchesList.forEach { liveMatch ->
                val saved = savedMatches.find { it.id == liveMatch.matchId }
                
                // --- DETECCION DE INCIDENCIAS EN VIVO ---
                if (saved != null) {
                    val matchInfo = getCachedMatch(liveMatch.matchId)
                    val homeTeamName = matchInfo?.homeTeam?.name ?: "Local"
                    val awayTeamName = matchInfo?.awayTeam?.name ?: "Visitante"

                    val oldHome = saved.homeScore ?: 0
                    val newHome = liveMatch.homeScore ?: 0
                    val oldAway = saved.awayScore ?: 0
                    val newAway = liveMatch.awayScore ?: 0

                    // 1. Detección de Goles
                    if (newHome > oldHome || newAway > oldAway) {
                        val scoringTeam = if (newHome > oldHome) homeTeamName else awayTeamName
                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "⚽ ¡GOOOOOL de $scoringTeam! ⚽",
                            message = "$homeTeamName $newHome - $newAway $awayTeamName (Min ${liveMatch.clock ?: ""})",
                            isGoal = true
                        )
                    }

                    // 1b. Detección de Goles Anulados (VAR)
                    if (newHome < oldHome || newAway < oldAway) {
                        val annulledTeam = if (newHome < oldHome) homeTeamName else awayTeamName
                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "❌ ¡GOL ANULADO (VAR)! ❌",
                            message = "El VAR anuló el gol de $annulledTeam. El marcador vuelve a: $homeTeamName $newHome - $newAway $awayTeamName.",
                            isGoal = false
                        )
                    }

                    // 2. Detección de Tarjetas Rojas
                    val (oldHomeRed, oldAwayRed) = parseRedCardsFromVipStats(saved.vipStats)
                    val newHomeRed = liveMatch.homeRedCards ?: 0
                    val newAwayRed = liveMatch.awayRedCards ?: 0

                    if (newHomeRed > oldHomeRed || newAwayRed > oldAwayRed) {
                        val penalizedTeam = if (newHomeRed > oldHomeRed) homeTeamName else awayTeamName
                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "🟥 ¡Tarjeta Roja para $penalizedTeam! 🟥",
                            message = "Un jugador de $penalizedTeam ha sido expulsado. (Min ${liveMatch.clock ?: ""})",
                            isGoal = false
                        )
                    }

                    // 2b. Detección de Tarjetas Amarillas
                    val (oldHomeYellow, oldAwayYellow) = parseYellowCardsFromVipStats(saved.vipStats)
                    val newHomeYellow = liveMatch.homeYellowCards ?: 0
                    val newAwayYellow = liveMatch.awayYellowCards ?: 0

                    if (newHomeYellow > oldHomeYellow || newAwayYellow > oldAwayYellow) {
                        val penalizedTeam = if (newHomeYellow > oldHomeYellow) homeTeamName else awayTeamName
                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "🟨 Tarjeta Amarilla 🟨",
                            message = "Amonestación para un jugador de $penalizedTeam. (Min ${liveMatch.clock ?: ""})",
                            isGoal = false
                        )
                    }

                    // 2c. Detección de Goles en la Tanda de Penales
                    val oldHomePens = saved.homePenalties ?: 0
                    val newHomePens = liveMatch.homePenalties ?: 0
                    val oldAwayPens = saved.awayPenalties ?: 0
                    val newAwayPens = liveMatch.awayPenalties ?: 0

                    if (newHomePens > oldHomePens || newAwayPens > oldAwayPens) {
                        val scoringTeam = if (newHomePens > oldHomePens) homeTeamName else awayTeamName
                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "🥅 ¡Gol en la Tanda de Penales! ⚽",
                            message = "Gol de $scoringTeam. Tanda actual: $newHomePens - $newAwayPens",
                            isGoal = false
                        )
                    }

                    // 3. Detección de fin de partido
                    if (saved.status != "Finished" && liveMatch.status == "Finished") {
                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "⏱️ ¡Final del partido! ⏱️",
                            message = "Terminó: $homeTeamName $newHome - $newAway $awayTeamName",
                            isGoal = false
                        )
                    }
                }
                // ----------------------------------------
                
                val vipStatsStr = if (liveMatch.homeFouls != null) {
                    "fouls:${liveMatch.homeFouls},${liveMatch.awayFouls}|" +
                    "corners:${liveMatch.homeCorners},${liveMatch.awayCorners}|" +
                    "saves:${liveMatch.homeSaves},${liveMatch.awaySaves}|" +
                    "yellow:${liveMatch.homeYellowCards},${liveMatch.awayYellowCards}|" +
                    "red:${liveMatch.homeRedCards},${liveMatch.awayRedCards}|" +
                    "passes:${liveMatch.homePasses ?: ""},${liveMatch.awayPasses ?: ""}"
                } else null

                val scorersStr = if (liveMatch.scorers.isEmpty()) null else liveMatch.scorers.joinToString("|")
                val eventsStr = if (liveMatch.events.isEmpty()) null else liveMatch.events.joinToString("|")

                if (saved == null || 
                    saved.homeScore != liveMatch.homeScore || 
                    saved.awayScore != liveMatch.awayScore || 
                    saved.homePenalties != liveMatch.homePenalties ||
                    saved.awayPenalties != liveMatch.awayPenalties ||
                    saved.status != liveMatch.status ||
                    saved.homePossession != liveMatch.homePossession ||
                    saved.vipStats != vipStatsStr ||
                    saved.scorers != scorersStr ||
                    saved.events != eventsStr ||
                    saved.clock != liveMatch.clock
                ) {
                    matchDao.insertMatch(MatchEntity(
                        id = liveMatch.matchId,
                        homeScore = liveMatch.homeScore,
                        awayScore = liveMatch.awayScore,
                        homePenalties = liveMatch.homePenalties ?: saved?.homePenalties,
                        awayPenalties = liveMatch.awayPenalties ?: saved?.awayPenalties,
                        status = liveMatch.status,
                        predictedWinner = saved?.predictedWinner,
                        predictedHomeScore = saved?.predictedHomeScore,
                        predictedAwayScore = saved?.predictedAwayScore,
                        homePossession = liveMatch.homePossession,
                        awayPossession = liveMatch.awayPossession,
                        homeShots = liveMatch.homeShots,
                        awayShots = liveMatch.awayShots,
                        scorers = scorersStr,
                        events = eventsStr,
                        vipStats = vipStatsStr,
                        clock = liveMatch.clock
                    ))
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    suspend fun saveMatchVipStats(matchId: Int, homePossession: Int, awayPossession: Int, homeShots: Int, awayShots: Int) {
        val saved = matchDao.getAllMatches().first().find { it.id == matchId }
        matchDao.insertMatch(MatchEntity(
            matchId,
            saved?.homeScore,
            saved?.awayScore,
            saved?.homePenalties,
            saved?.awayPenalties,
            saved?.status ?: "Scheduled",
            predictedWinner = saved?.predictedWinner,
            predictedHomeScore = saved?.predictedHomeScore,
            predictedAwayScore = saved?.predictedAwayScore,
            homePossession = homePossession,
            awayPossession = awayPossession,
            homeShots = homeShots,
            awayShots = awayShots,
            scorers = saved?.scorers,
            events = saved?.events,
            vipStats = saved?.vipStats,
            clock = saved?.clock
        ))
    }

    suspend fun getAllTeams(): List<Team> {
        return getMockGroups().flatMap { it.teams }
    }
}
