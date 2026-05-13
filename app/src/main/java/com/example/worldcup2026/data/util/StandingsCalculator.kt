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
    fun calculateStandings(groupTeams: List<Team>, allMatches: List<Match>): List<TeamStats> {
        return groupTeams.map { team ->
            calculateTeamStats(team, allMatches)
        }.sortedWith(
            compareByDescending<TeamStats> { it.pts }
                .thenByDescending { it.gd }
                .thenByDescending { it.gf }
        )
    }

    private fun calculateTeamStats(team: Team, matches: List<Match>): TeamStats {
        var pj = 0
        var g = 0
        var e = 0
        var p = 0
        var gf = 0
        var ga = 0
        var pts = 0

        matches.filter { it.id <= 100 }.forEach { match ->
            if (match.homeTeam.id == team.id || match.awayTeam.id == team.id) {
                val hScore = match.homeScore
                val aScore = match.awayScore
                
                if (hScore != null && aScore != null) {
                    pj++
                    val (teamScore, opponentScore) = if (match.homeTeam.id == team.id) hScore to aScore else aScore to hScore
                    
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
