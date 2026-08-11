package com.example.worldcup2026.data.util

import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team

data class TeamStats(
    val team: Team,
    val pj: Int,
    val g: Int,
    val e: Int,
    val p: Int,
    val gf: Int,
    val ga: Int,
    val gd: Int,
    val pts: Int
)

object StandingsCalculator {
    fun calculateStandings(groupTeams: List<Team>, allMatches: List<Match>, isWorldCup: Boolean = true): List<TeamStats> {
        return groupTeams.filterNotNull().map { team ->
            calculateTeamStats(team, allMatches, isWorldCup)
        }.sortedWith(
            compareByDescending<TeamStats> { it.pts }
                .thenByDescending { it.gd }
                .thenByDescending { it.gf }
        )
    }

    private fun calculateTeamStats(team: Team, matches: List<Match>, isWorldCup: Boolean): TeamStats {
        var pj = 0
        var g = 0
        var e = 0
        var p = 0
        var gf = 0
        var ga = 0
        var pts = 0

        val groupMatches = if (isWorldCup) {
            matches.filter { it.id <= 72 }
        } else {
            matches
        }

        val seenFixtureKeys = mutableSetOf<String>()
        val targetTeamName = team.name.trim().lowercase()

        groupMatches.forEach { match ->
            val statusUpper = match.status.uppercase()
            if (statusUpper == "SCHEDULED" || statusUpper.contains("POSTP") || statusUpper.contains("SUSPEND") || statusUpper.contains("CANCEL")) return@forEach
            if (match.status != "Finished" && !statusUpper.contains("LIVE") && !statusUpper.contains("HALFTIME")) return@forEach
            
            val homeName = match.homeTeam?.name?.trim()?.lowercase() ?: ""
            val awayName = match.awayTeam?.name?.trim()?.lowercase() ?: ""
            val homeId = match.homeTeam?.id
            val awayId = match.awayTeam?.id

            val isHome = homeId == team.id || (homeName.isNotEmpty() && homeName == targetTeamName)
            val isAway = awayId == team.id || (awayName.isNotEmpty() && awayName == targetTeamName)

            if (isHome || isAway) {
                val hScore = match.homeScore
                val aScore = match.awayScore
                
                if (hScore != null && aScore != null) {
                    val opponentName = if (isHome) awayName else homeName
                    val datePrefix = match.date?.take(7) ?: ""
                    val fixtureKey = "${opponentName}_$datePrefix"
                    if (!seenFixtureKeys.add(fixtureKey)) {
                        return@forEach // Evitar computar duplicados del mismo partido
                    }

                    pj++
                    val (teamScore, opponentScore) = if (isHome) hScore to aScore else aScore to hScore
                    
                    gf += teamScore
                    ga += opponentScore
                    
                    when {
                        teamScore > opponentScore -> {
                            g++
                            pts += 3
                        }
                        teamScore == opponentScore -> {
                            e++
                            pts += 1
                        }
                        else -> p++
                    }
                }
            }
        }
        return TeamStats(team, pj, g, e, p, gf, ga, gf - ga, pts)
    }
}
