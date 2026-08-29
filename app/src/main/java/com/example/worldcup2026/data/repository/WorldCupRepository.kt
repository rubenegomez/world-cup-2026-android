package com.example.worldcup2026.data.repository

import com.example.worldcup2026.data.local.MatchDao
import com.example.worldcup2026.data.local.MatchEntity
import com.example.worldcup2026.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WorldCupRepository(private val matchDao: MatchDao) {
    private var cachedMatches: List<Match>? = null

    private suspend fun getCachedMatch(matchId: Int, tournamentId: Int): Match? {
        if (cachedMatches == null) {
            getMatches(tournamentId)
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

    suspend fun getMockGroups(tournamentId: Int): List<Group> {
        return com.example.worldcup2026.data.api.NetworkModule.apiService.getGroups(tournamentId)
    }

    private fun createTeam(id: Int, name: String, flagCode: String, group: String): Team {
        return Team(id, name, "https://flagcdn.com/w160/$flagCode.png", group, players = emptyList())
    }

    private fun createPlaceholderTeam(name: String): Team {
        return Team(-1, name, "", "Final", players = emptyList())
    }

    suspend fun getAllMatchesGlobal(): List<Match> {
        val savedMatches = matchDao.getAllMatches().first()
        val matches = mutableListOf<Match>()
        
        try {
            val rawRemoteMatches = com.example.worldcup2026.data.api.NetworkModule.apiService.getMatches(null)
            val remoteMatches = rawRemoteMatches.filter { it.tournament_id == 5 || it.tournament_id == 1 || it.tournament_id == null }
            if (remoteMatches.isNotEmpty()) {
                val validIds = remoteMatches.map { it.id }
                matchDao.deleteObsoleteMatches(validIds)
            }

            remoteMatches.forEach { match ->
                val saved = savedMatches.find { it.id == match.id }
                val tournamentId = match.tournament_id ?: saved?.tournamentId ?: 1
                addMatchWithPersistence(matches, savedMatches, match, tournamentId)
            }
            cachedMatches = matches
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return matches
    }

    suspend fun getMatches(tournamentId: Int): List<Match> {
        val savedMatches = matchDao.getMatchesByTournament(tournamentId).first()
        val matches = mutableListOf<Match>()
        
        try {
            val remoteMatches = com.example.worldcup2026.data.api.NetworkModule.apiService.getMatches(tournamentId)
                .filter { it.tournament_id == 5 || it.tournament_id == 1 || it.tournament_id == null }
            remoteMatches.forEach { match ->
                addMatchWithPersistence(matches, savedMatches, match, tournamentId)
            }
            cachedMatches = matches
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return matches
    }

    private suspend fun addMatchWithPersistence(targetList: MutableList<Match>, savedEntities: List<MatchEntity>, baseMatch: Match, tournamentId: Int) {
        val saved = savedEntities.find { it.id == baseMatch.id }
        if (saved != null) {
            // Si el remoto tiene score, preferimos SIEMPRE el remoto. Si no, usamos el local guardado.
            val homeScore = baseMatch.homeScore ?: saved.homeScore
            val awayScore = baseMatch.awayScore ?: saved.awayScore
            val homePenalties = baseMatch.homePenalties ?: saved.homePenalties
            val awayPenalties = baseMatch.awayPenalties ?: saved.awayPenalties
            
            var status = baseMatch.status
            
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                val matchDate = sdf.parse(baseMatch.date)
                if (matchDate != null) {
                    val diff = System.currentTimeMillis() - matchDate.time
                    // Si comenzó hace entre 0 y 2.5 horas y no está finalizado, marcar LIVE
                    if (diff in 0..(2 * 3600 * 1000 + 30 * 60 * 1000)) {
                        if (status != "Finished") {
                            status = "LIVE"
                        }
                    } else if (diff > 2 * 3600 * 1000 + 30 * 60 * 1000) {
                        // Si transcurrieron más de 2.5 horas desde el inicio y estaba LIVE, expira a Finished
                        if (status == "LIVE" || saved.status == "LIVE") {
                            status = "Finished"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (baseMatch.status == "Finished") {
                status = "Finished"
            }

            val homePossession = baseMatch.homePossession ?: saved.homePossession
            val awayPossession = baseMatch.awayPossession ?: saved.awayPossession
            val homeShots = baseMatch.homeShots ?: saved.homeShots
            val awayShots = baseMatch.awayShots ?: saved.awayShots
            val scorers = if (baseMatch.scorers.isNotEmpty()) baseMatch.scorers else (if (saved.scorers.isNullOrEmpty()) emptyList() else saved.scorers.split("|"))
            val events = if (baseMatch.events.isNotEmpty()) baseMatch.events else (if (saved.events.isNullOrEmpty()) emptyList() else saved.events.split("|"))
            val vipStats = baseMatch.vipStats ?: saved.vipStats
            val clock = baseMatch.clock ?: saved.clock

            targetList.add(baseMatch.copy(
                tournament_id = tournamentId,
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
                    tournamentId = tournamentId,
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
            val finalTournamentId = baseMatch.tournament_id ?: tournamentId
            val newMatch = baseMatch.copy(tournament_id = finalTournamentId)
            targetList.add(newMatch)
            
            // FIX: Debemos insertar en la base de datos local si no existía, para que el Prode y el SyncWorker puedan usarlo!
            matchDao.insertMatch(MatchEntity(
                id = newMatch.id,
                tournamentId = finalTournamentId,
                homeScore = newMatch.homeScore,
                awayScore = newMatch.awayScore,
                homePenalties = newMatch.homePenalties,
                awayPenalties = newMatch.awayPenalties,
                status = newMatch.status,
                predictedWinner = null,
                predictedHomeScore = null,
                predictedAwayScore = null,
                homePossession = newMatch.homePossession,
                awayPossession = newMatch.awayPossession,
                homeShots = newMatch.homeShots,
                awayShots = newMatch.awayShots,
                scorers = if (newMatch.scorers.isEmpty()) null else newMatch.scorers.joinToString("|"),
                events = if (newMatch.events.isEmpty()) null else newMatch.events.joinToString("|"),
                vipStats = newMatch.vipStats,
                clock = newMatch.clock
            ))
        }
    }

    suspend fun saveMatchScore(matchId: Int, homeScore: Int?, awayScore: Int?, homePenalties: Int? = null, awayPenalties: Int? = null, status: String? = null) {
        val saved = matchDao.getAllMatches().first().find { it.id == matchId }
        val finalStatus = status ?: if (homeScore != null && awayScore != null) "Finished" else "Scheduled"
        matchDao.insertMatch(MatchEntity(
            id = matchId, 
            tournamentId = saved?.tournamentId ?: 1,
            homeScore = homeScore, 
            awayScore = awayScore, 
            homePenalties = homePenalties, 
            awayPenalties = awayPenalties, 
            status = finalStatus,
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

    suspend fun saveMatchPrediction(matchId: Int, winner: String?, homePredict: Int?, awayPredict: Int?, homePenaltiesPredict: Int? = null, awayPenaltiesPredict: Int? = null) {
        val saved = matchDao.getAllMatches().first().find { it.id == matchId }
        matchDao.insertMatch(MatchEntity(
            id = matchId,
            tournamentId = saved?.tournamentId ?: 1,
            homeScore = saved?.homeScore,
            awayScore = saved?.awayScore,
            homePenalties = saved?.homePenalties,
            awayPenalties = saved?.awayPenalties,
            status = saved?.status ?: "Scheduled",
            predictedWinner = winner,
            predictedHomeScore = homePredict,
            predictedAwayScore = awayPredict,
            predictedHomePenalties = homePenaltiesPredict,
            predictedAwayPenalties = awayPenaltiesPredict,
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
            id = matchId, 
            tournamentId = saved?.tournamentId ?: 1,
            homeScore = home, 
            awayScore = away, 
            homePenalties = saved?.homePenalties, 
            awayPenalties = saved?.awayPenalties, 
            status = status,
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

    suspend fun syncMatchesWithLiveJson(context: android.content.Context, tournamentId: Int): Boolean {
        try {
            val service = com.example.worldcup2026.data.api.NetworkModule.apiService
            val matchesList = service.getLiveMatches(tournamentId)
            
            val savedMatches = matchDao.getAllMatches().first()
            
            matchesList.forEach { liveMatch ->
                val saved = savedMatches.find { it.id == liveMatch.matchId }
                
                var effectiveStatus = liveMatch.status ?: "Scheduled"
                
                // FORZAR ESTADO "LIVE" SI YA PASO LA HORA DE INICIO
                val matchInfoForStatus = getCachedMatch(liveMatch.matchId, saved?.tournamentId ?: tournamentId)
                if (effectiveStatus == "Scheduled" && matchInfoForStatus?.date != null) {
                    try {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        val matchDate = sdf.parse(matchInfoForStatus.date)
                        if (matchDate != null) {
                            val currentTime = System.currentTimeMillis()
                            val matchTimeMs = matchDate.time
                            if (currentTime >= matchTimeMs) {
                                effectiveStatus = "LIVE"
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }

                // --- DETECCION DE INCIDENCIAS EN VIVO ---
                // NOTA: Solo notificar si el partido está actualmente EN VIVO y ya teníamos datos locales (evita notificar partidos viejos al instalar la app)
                val isLiveState = effectiveStatus == "LIVE" || effectiveStatus == "HALFTIME" || saved?.status == "LIVE"
                if (saved != null && saved.homeScore != null && isLiveState) {
                    val allGlobal = cachedMatches ?: getAllMatchesGlobal()
                    val matchInfo = allGlobal.find { it.id == liveMatch.matchId }
                    val homeTeamName = matchInfo?.homeTeam?.name?.takeIf { it.isNotBlank() }
                        ?: "Equipo Local"
                    val awayTeamName = matchInfo?.awayTeam?.name?.takeIf { it.isNotBlank() }
                        ?: "Equipo Visitante"
                    
                    val oldHome = saved.homeScore ?: 0
                    val newHome = liveMatch.homeScore ?: 0
                    val oldAway = saved.awayScore ?: 0
                    val newAway = liveMatch.awayScore ?: 0

                    // 1. Detección de Goles
                    if (newHome > oldHome || newAway > oldAway) {
                        val scoringTeam = if (newHome > oldHome) homeTeamName else awayTeamName
                        val latestScorer = liveMatch.scorers?.lastOrNull()?.trim()
                        val goalMsg = if (!latestScorer.isNullOrBlank()) {
                            "¡Gol de $latestScorer! $homeTeamName $newHome - $newAway $awayTeamName (Min ${liveMatch.clock ?: ""})"
                        } else {
                            "$homeTeamName $newHome - $newAway $awayTeamName (Min ${liveMatch.clock ?: ""})"
                        }

                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "⚽ ¡GOOOOOL de $scoringTeam! ⚽",
                            message = goalMsg,
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
                        val lastEvent = liveMatch.events?.findLast { it.contains("Roja", ignoreCase = true) || it.contains("Expulsi", ignoreCase = true) }
                        val playerName = lastEvent?.substringBefore("(")?.substringBefore(" -")?.trim()

                        val redMsg = if (!playerName.isNullOrBlank()) {
                            "¡Expulsión de $playerName ($penalizedTeam)! (Min ${liveMatch.clock ?: ""})"
                        } else {
                            "Un jugador de $penalizedTeam ha sido expulsado. (Min ${liveMatch.clock ?: ""})"
                        }

                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "🟥 ¡Tarjeta Roja para $penalizedTeam! 🟥",
                            message = redMsg,
                            isGoal = false
                        )
                    }

                    // 2b. Detección de Tarjetas Amarillas
                    val (oldHomeYellow, oldAwayYellow) = parseYellowCardsFromVipStats(saved.vipStats)
                    val newHomeYellow = liveMatch.homeYellowCards ?: 0
                    val newAwayYellow = liveMatch.awayYellowCards ?: 0

                    if (newHomeYellow > oldHomeYellow || newAwayYellow > oldAwayYellow) {
                        val penalizedTeam = if (newHomeYellow > oldHomeYellow) homeTeamName else awayTeamName
                        val lastEvent = liveMatch.events?.findLast { it.contains("Amarilla", ignoreCase = true) || it.contains("Tarjeta", ignoreCase = true) }
                        val playerName = lastEvent?.substringBefore("(")?.substringBefore(" -")?.trim()

                        val yellowMsg = if (!playerName.isNullOrBlank()) {
                            "Amonestación para $playerName ($penalizedTeam). (Min ${liveMatch.clock ?: ""})"
                        } else {
                            "Amonestación para un jugador de $penalizedTeam. (Min ${liveMatch.clock ?: ""})"
                        }

                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "🟨 Tarjeta Amarilla ($penalizedTeam) 🟨",
                            message = yellowMsg,
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
                            title = "🥅 ¡Gol en Penales de $scoringTeam! ⚽",
                            message = "Gol de $scoringTeam. Tanda actual: $homeTeamName $newHomePens - $newAwayPens $awayTeamName",
                            isGoal = false
                        )
                    }

                    // 3. Detección de fin de partido
                    if (saved.status != "Finished" && effectiveStatus == "Finished") {
                        com.example.worldcup2026.data.util.NotificationHelper.showMatchIncidentNotification(
                            context = context,
                            title = "🏁 ¡Final del Partido! 🏁",
                            message = "$homeTeamName $newHome - $newAway $awayTeamName. ¡Encuentro finalizado!",
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

                val scorersList = liveMatch.scorers.orEmpty()
                val eventsList = liveMatch.events.orEmpty()
                val scorersStr = if (scorersList.isEmpty()) null else scorersList.joinToString("|")
                val eventsStr = if (eventsList.isEmpty()) null else eventsList.joinToString("|")

                if (saved == null || 
                    saved.homeScore != liveMatch.homeScore || 
                    saved.awayScore != liveMatch.awayScore || 
                    saved.homePenalties != liveMatch.homePenalties ||
                    saved.awayPenalties != liveMatch.awayPenalties ||
                    saved.status != effectiveStatus ||
                    saved.homePossession != liveMatch.homePossession ||
                    saved.vipStats != vipStatsStr ||
                    saved.scorers != scorersStr ||
                    saved.events != eventsStr ||
                    saved.clock != liveMatch.clock
                ) {
                    val finalTournamentId = liveMatch.tournament_id ?: saved?.tournamentId ?: tournamentId
                    matchDao.insertMatch(MatchEntity(
                        id = liveMatch.matchId,
                        tournamentId = finalTournamentId,
                        homeScore = liveMatch.homeScore,
                        awayScore = liveMatch.awayScore,
                        homePenalties = liveMatch.homePenalties ?: saved?.homePenalties,
                        awayPenalties = liveMatch.awayPenalties ?: saved?.awayPenalties,
                        status = effectiveStatus,
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
            id = matchId,
            tournamentId = saved?.tournamentId ?: 1,
            homeScore = saved?.homeScore,
            awayScore = saved?.awayScore,
            homePenalties = saved?.homePenalties,
            awayPenalties = saved?.awayPenalties,
            status = saved?.status ?: "Scheduled",
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

    suspend fun getAllTeams(tournamentId: Int): List<Team> {
        return getMockGroups(tournamentId).flatMap { it.teams }
    }
}
