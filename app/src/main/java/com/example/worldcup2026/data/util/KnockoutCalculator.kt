package com.example.worldcup2026.data.util

import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team

object KnockoutCalculator {
    fun calculateKnockoutMatches(allMatches: List<Match>, tournamentId: Int): List<Match> {
        val knockoutMatches = allMatches.filter { it.id > 72 }.toMutableList()

        if (tournamentId != 1) {
            return knockoutMatches // Solo calculamos llaves localmente para el Mundial de 104 partidos
        }

        // Lógica de ganadores de llaves anteriores
        fillWinners(knockoutMatches)

        return knockoutMatches
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
        // Octavos (89-96)
        updateKnockoutMatch(matches, 89, getWinner(matches.find { it.id == 74 }), getWinner(matches.find { it.id == 77 }))
        updateKnockoutMatch(matches, 90, getWinner(matches.find { it.id == 73 }), getWinner(matches.find { it.id == 75 }))
        updateKnockoutMatch(matches, 91, getWinner(matches.find { it.id == 76 }), getWinner(matches.find { it.id == 78 }))
        updateKnockoutMatch(matches, 92, getWinner(matches.find { it.id == 79 }), getWinner(matches.find { it.id == 80 }))
        updateKnockoutMatch(matches, 93, getWinner(matches.find { it.id == 83 }), getWinner(matches.find { it.id == 84 }))
        updateKnockoutMatch(matches, 94, getWinner(matches.find { it.id == 81 }), getWinner(matches.find { it.id == 82 }))
        updateKnockoutMatch(matches, 95, getWinner(matches.find { it.id == 86 }), getWinner(matches.find { it.id == 88 }))
        updateKnockoutMatch(matches, 96, getWinner(matches.find { it.id == 85 }), getWinner(matches.find { it.id == 87 }))

        // Cuartos (97-100)
        updateKnockoutMatch(matches, 97, getWinner(matches.find { it.id == 89 }), getWinner(matches.find { it.id == 90 }))
        updateKnockoutMatch(matches, 98, getWinner(matches.find { it.id == 93 }), getWinner(matches.find { it.id == 94 }))
        updateKnockoutMatch(matches, 99, getWinner(matches.find { it.id == 91 }), getWinner(matches.find { it.id == 92 }))
        updateKnockoutMatch(matches, 100, getWinner(matches.find { it.id == 95 }), getWinner(matches.find { it.id == 96 }))

        // Semis (101-102)
        updateKnockoutMatch(matches, 101, getWinner(matches.find { it.id == 97 }), getWinner(matches.find { it.id == 98 }))
        updateKnockoutMatch(matches, 102, getWinner(matches.find { it.id == 99 }), getWinner(matches.find { it.id == 100 }))

        // Final (103)
        val s1 = matches.find { it.id == 101 }
        val s2 = matches.find { it.id == 102 }
        updateKnockoutMatch(matches, 103, getWinner(s1), getWinner(s2))
        
        // 3er Puesto (104)
        updateKnockoutMatch(matches, 104, getLoser(s1), getLoser(s2))
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
