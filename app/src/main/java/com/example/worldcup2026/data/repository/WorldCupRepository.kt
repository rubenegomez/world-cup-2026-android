package com.example.worldcup2026.data.repository

import com.example.worldcup2026.data.local.MatchDao
import com.example.worldcup2026.data.local.MatchEntity
import com.example.worldcup2026.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WorldCupRepository(private val matchDao: MatchDao) {

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

    suspend fun syncMatchesWithLiveJson(): Boolean {
        try {
            val service = com.example.worldcup2026.data.api.NetworkModule.apiService
            val matchesList = service.getLiveMatches()
            
            val savedMatches = matchDao.getAllMatches().first()
            
            matchesList.forEach { liveMatch ->
                val saved = savedMatches.find { it.id == liveMatch.matchId }
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
