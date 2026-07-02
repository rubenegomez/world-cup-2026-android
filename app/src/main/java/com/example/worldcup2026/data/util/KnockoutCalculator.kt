package com.example.worldcup2026.data.util

import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team

object KnockoutCalculator {
    fun calculateKnockoutMatches(allMatches: List<Match>, tournamentId: Int): List<Match> {
        val knockoutMatches = allMatches.filter { it.id > 72 }.toMutableList()

        if (tournamentId != 1) {
            return knockoutMatches
        }

        fillWinners(knockoutMatches)

        return knockoutMatches
    }

    private fun hasRealTeam(team: Team): Boolean {
        return team.group != "X" && team.group != "Final" && team.group != "TBD"
    }

    private fun updateKnockoutMatch(matches: MutableList<Match>, id: Int, home: Team?, away: Team?) {
        val index = matches.indexOfFirst { it.id == id }
        if (index != -1) {
            val m = matches[index]
            if (m.status.equals("Finished", ignoreCase = true) || m.status.equals("LIVE", ignoreCase = true)) {
                return
            }
            if (hasRealTeam(m.homeTeam) || hasRealTeam(m.awayTeam)) {
                return
            }
            matches[index] = m.copy(
                homeTeam = home ?: m.homeTeam,
                awayTeam = away ?: m.awayTeam
            )
        }
    }

    private fun fillWinners(matches: MutableList<Match>) {
        updateKnockoutMatch(matches, 117, getWinner(matches.find { it.id == 102 }), getWinner(matches.find { it.id == 105 }))
        updateKnockoutMatch(matches, 118, getWinner(matches.find { it.id == 101 }), getWinner(matches.find { it.id == 103 }))
        updateKnockoutMatch(matches, 119, getWinner(matches.find { it.id == 104 }), getWinner(matches.find { it.id == 106 }))
        updateKnockoutMatch(matches, 120, getWinner(matches.find { it.id == 107 }), getWinner(matches.find { it.id == 108 }))
        updateKnockoutMatch(matches, 121, getWinner(matches.find { it.id == 111 }), getWinner(matches.find { it.id == 112 }))
        updateKnockoutMatch(matches, 122, getWinner(matches.find { it.id == 109 }), getWinner(matches.find { it.id == 110 }))
        updateKnockoutMatch(matches, 123, getWinner(matches.find { it.id == 114 }), getWinner(matches.find { it.id == 116 }))
        updateKnockoutMatch(matches, 124, getWinner(matches.find { it.id == 113 }), getWinner(matches.find { it.id == 115 }))

        updateKnockoutMatch(matches, 125, getWinner(matches.find { it.id == 117 }), getWinner(matches.find { it.id == 118 }))
        updateKnockoutMatch(matches, 126, getWinner(matches.find { it.id == 121 }), getWinner(matches.find { it.id == 122 }))
        updateKnockoutMatch(matches, 127, getWinner(matches.find { it.id == 119 }), getWinner(matches.find { it.id == 120 }))
        updateKnockoutMatch(matches, 128, getWinner(matches.find { it.id == 123 }), getWinner(matches.find { it.id == 124 }))

        updateKnockoutMatch(matches, 129, getWinner(matches.find { it.id == 125 }), getWinner(matches.find { it.id == 126 }))
        updateKnockoutMatch(matches, 130, getWinner(matches.find { it.id == 127 }), getWinner(matches.find { it.id == 128 }))

        val s1 = matches.find { it.id == 129 }
        val s2 = matches.find { it.id == 130 }
        updateKnockoutMatch(matches, 132, getLoser(s1), getLoser(s2))
        updateKnockoutMatch(matches, 131, getWinner(s1), getWinner(s2))
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
