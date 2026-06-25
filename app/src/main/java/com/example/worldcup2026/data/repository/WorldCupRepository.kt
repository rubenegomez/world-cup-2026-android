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

    private fun addMatchWithPersistence(targetList: MutableList<Match>, savedEntities: List<MatchEntity>, baseMatch: Match) {
        val saved = savedEntities.find { it.id == baseMatch.id }
        if (saved != null) {
            targetList.add(baseMatch.copy(
                homeScore = saved.homeScore,
                awayScore = saved.awayScore,
                homePenalties = saved.homePenalties,
                awayPenalties = saved.awayPenalties,
                status = saved.status,
                predictedWinner = saved.predictedWinner,
                predictedHomeScore = saved.predictedHomeScore,
                predictedAwayScore = saved.predictedAwayScore,
                homePossession = saved.homePossession,
                awayPossession = saved.awayPossession,
                homeShots = saved.homeShots,
                awayShots = saved.awayShots,
                scorers = if (saved.scorers.isNullOrEmpty()) emptyList() else saved.scorers.split("|"),
                events = if (saved.events.isNullOrEmpty()) emptyList() else saved.events.split("|"),
                vipStats = saved.vipStats,
                clock = saved.clock
            ))
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

                    // 1. Detección de Goles
                    val oldHome = saved.homeScore ?: 0
                    val newHome = liveMatch.homeScore ?: 0
                    val oldAway = saved.awayScore ?: 0
                    val newAway = liveMatch.awayScore ?: 0

                    if (newHome > oldHome || newAway > oldAway) {
                        val scoringTeam = if (newHome > oldHome) homeTeamName else awayTeamName
                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "⚽ ¡GOOOOOL de $scoringTeam! ⚽",
                            message = "$homeTeamName $newHome - $newAway $awayTeamName (Min ${liveMatch.clock ?: ""})"
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
                            message = "Un jugador de $penalizedTeam ha sido expulsado. (Min ${liveMatch.clock ?: ""})"
                        )
                    }

                    // 3. Detección de fin de partido
                    if (saved.status != "Finished" && liveMatch.status == "Finished") {
                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "⏱️ ¡Final del partido! ⏱️",
                            message = "Terminó: $homeTeamName $newHome - $newAway $awayTeamName"
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
                        homePenalties = saved?.homePenalties,
                        awayPenalties = saved?.awayPenalties,
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
