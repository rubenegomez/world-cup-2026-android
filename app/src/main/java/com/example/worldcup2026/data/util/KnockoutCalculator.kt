package com.example.worldcup2026.data.util

import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team

object KnockoutCalculator {
    fun calculateKnockoutMatches(allMatches: List<Match>): List<Match> {
        val groupMatches = allMatches.filter { it.id <= 100 }
        val knockoutMatches = allMatches.filter { it.id > 100 }.toMutableList()

        val allTeams = groupMatches.flatMap { listOf(it.homeTeam, it.awayTeam) }.distinctBy { it.id }.filter { it.id > 0 }
        val groups = allTeams.groupBy { it.group }.toSortedMap()

        val standings = groups.mapValues { (_, teams) ->
            StandingsCalculator.calculateStandings(teams, groupMatches)
        }

        // Calcular el ranking de mejores terceros
        val bestThirds = standings.mapNotNull { (_, groupStandings) ->
            if (groupStandings.all { it.pj >= 3 }) {
                groupStandings.getOrNull(2) // El 3º puesto
            } else {
                null
            }
        }.sortedWith(
            compareByDescending<TeamStats> { it.pts }
                .thenByDescending { it.gd }
                .thenByDescending { it.gf }
        ).map { it.team }

        // Mapear los 32 clasificados a las 16 llaves de Dieciseisavos (101-116) según fixture exacto de Canchallena
        updateKnockoutMatch(knockoutMatches, 101, bestThirds.getOrNull(0), getTeamAt(standings, "B", 1))
        updateKnockoutMatch(knockoutMatches, 102, getTeamAt(standings, "C", 0), getTeamAt(standings, "F", 1))
        updateKnockoutMatch(knockoutMatches, 103, getTeamAt(standings, "E", 0), getTeamAt(standings, "D", 1))
        updateKnockoutMatch(knockoutMatches, 104, getTeamAt(standings, "F", 0), getTeamAt(standings, "C", 1))
        updateKnockoutMatch(knockoutMatches, 105, getTeamAt(standings, "E", 1), getTeamAt(standings, "I", 1))
        updateKnockoutMatch(knockoutMatches, 106, getTeamAt(standings, "I", 0), bestThirds.getOrNull(1))
        updateKnockoutMatch(knockoutMatches, 107, getTeamAt(standings, "A", 0), getTeamAt(standings, "E", 2) ?: getTeamAt(standings, "E", 1))
        updateKnockoutMatch(knockoutMatches, 108, getTeamAt(standings, "L", 0), bestThirds.getOrNull(3))
        updateKnockoutMatch(knockoutMatches, 109, getTeamAt(standings, "G", 0), getTeamAt(standings, "A", 1))
        updateKnockoutMatch(knockoutMatches, 110, getTeamAt(standings, "D", 0), bestThirds.getOrNull(2))
        updateKnockoutMatch(knockoutMatches, 111, getTeamAt(standings, "H", 0), getTeamAt(standings, "J", 1))
        updateKnockoutMatch(knockoutMatches, 112, getTeamAt(standings, "K", 0), getTeamAt(standings, "L", 1))
        updateKnockoutMatch(knockoutMatches, 113, getTeamAt(standings, "D", 2) ?: getTeamAt(standings, "D", 0), getTeamAt(standings, "G", 1))
        updateKnockoutMatch(knockoutMatches, 114, getTeamAt(standings, "J", 0), bestThirds.getOrNull(4))
        updateKnockoutMatch(knockoutMatches, 115, getTeamAt(standings, "K", 1), bestThirds.getOrNull(6))
        updateKnockoutMatch(knockoutMatches, 116, getTeamAt(standings, "B", 0), bestThirds.getOrNull(5))

        // Lógica de ganadores de llaves anteriores
        fillWinners(knockoutMatches)

        return knockoutMatches
    }

    private fun getTeamAt(standings: Map<String, List<TeamStats>>, group: String, pos: Int): Team? {
        val groupStandings = standings[group] ?: return null
        if (groupStandings.size <= pos) return null
        
        // Verificamos si el grupo terminó (todos jugaron 3 partidos)
        val groupFinished = groupStandings.all { it.pj >= 3 }
        return if (groupFinished) groupStandings[pos].team else null
    }

    private fun updateKnockoutMatch(matches: MutableList<Match>, id: Int, home: Team?, away: Team?) {
        val index = matches.indexOfFirst { it.id == id }
        if (index != -1) {
            val m = matches[index]
            if (m.status.equals("Finished", ignoreCase = true) || m.status.equals("LIVE", ignoreCase = true)) {
                return
            }
            if (m.homeTeam.group != "X" && m.homeTeam.group != "Final" &&
                m.awayTeam.group != "X" && m.awayTeam.group != "Final") {
                return
            }
            matches[index] = m.copy(
                homeTeam = home ?: m.homeTeam,
                awayTeam = away ?: m.awayTeam
            )
        }
    }

    private fun fillWinners(matches: MutableList<Match>) {
        // Octavos (117-124) según cascada FIFA oficial
        updateKnockoutMatch(matches, 117, getWinner(matches.find { it.id == 101 }), getWinner(matches.find { it.id == 104 })) // FIFA P90
        updateKnockoutMatch(matches, 118, getWinner(matches.find { it.id == 103 }), getWinner(matches.find { it.id == 106 })) // FIFA P89
        updateKnockoutMatch(matches, 119, getWinner(matches.find { it.id == 102 }), getWinner(matches.find { it.id == 105 })) // FIFA P91
        updateKnockoutMatch(matches, 120, getWinner(matches.find { it.id == 107 }), getWinner(matches.find { it.id == 108 })) // FIFA P92
        updateKnockoutMatch(matches, 121, getWinner(matches.find { it.id == 112 }), getWinner(matches.find { it.id == 111 })) // FIFA P93
        updateKnockoutMatch(matches, 122, getWinner(matches.find { it.id == 110 }), getWinner(matches.find { it.id == 109 })) // FIFA P94
        updateKnockoutMatch(matches, 123, getWinner(matches.find { it.id == 114 }), getWinner(matches.find { it.id == 113 })) // FIFA P95
        updateKnockoutMatch(matches, 124, getWinner(matches.find { it.id == 116 }), getWinner(matches.find { it.id == 115 })) // FIFA P96

        // Cuartos (125-128) según FIFA oficial
        updateKnockoutMatch(matches, 125, getWinner(matches.find { it.id == 117 }), getWinner(matches.find { it.id == 118 })) // FIFA P97
        updateKnockoutMatch(matches, 126, getWinner(matches.find { it.id == 121 }), getWinner(matches.find { it.id == 122 })) // FIFA P98 - CORREGIDO
        updateKnockoutMatch(matches, 127, getWinner(matches.find { it.id == 119 }), getWinner(matches.find { it.id == 120 })) // FIFA P99 - CORREGIDO
        updateKnockoutMatch(matches, 128, getWinner(matches.find { it.id == 123 }), getWinner(matches.find { it.id == 124 })) // FIFA P100

        // Semis (129-130)
        updateKnockoutMatch(matches, 129, getWinner(matches.find { it.id == 125 }), getWinner(matches.find { it.id == 126 }))
        updateKnockoutMatch(matches, 130, getWinner(matches.find { it.id == 127 }), getWinner(matches.find { it.id == 128 }))

        // Final (131)
        val s1 = matches.find { it.id == 129 }
        val s2 = matches.find { it.id == 130 }
        updateKnockoutMatch(matches, 131, getWinner(s1), getWinner(s2))
        
        // 3er Puesto (132)
        updateKnockoutMatch(matches, 132, getLoser(s1), getLoser(s2))
    }

    private fun getWinner(match: Match?): Team? {
        if (match == null || match.status != "Finished") return null
        val h = match.homeScore ?: 0
        val a = match.awayScore ?: 0
        return if (h > a) match.homeTeam 
               else if (a > h) match.awayTeam 
               else if ((match.homePenalties ?: 0) > (match.awayPenalties ?: 0)) match.homeTeam 
               else match.awayTeam
    }

    private fun getLoser(match: Match?): Team? {
        if (match == null || match.status != "Finished") return null
        val h = match.homeScore ?: 0
        val a = match.awayScore ?: 0
        return if (h < a) match.homeTeam 
               else if (a < h) match.awayTeam 
               else if ((match.homePenalties ?: 0) < (match.awayPenalties ?: 0)) match.homeTeam 
               else match.awayTeam
    }
}
