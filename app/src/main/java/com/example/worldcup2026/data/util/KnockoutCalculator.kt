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

        // DIECISEISAVOS (101-116)
        // Regla: 1° y 2° de cada grupo (24 equipos) + 8 mejores 3° (8 equipos) = 32 equipos.
        // Por ahora haremos una lógica simplificada para los primeros cruces conocidos.
        
        // Ejemplo: Match 101: 2° Grupo A vs 2° Grupo B
        updateKnockoutMatch(knockoutMatches, 101, getTeamAt(standings, "A", 1), getTeamAt(standings, "B", 1)) 
        // Nota: El usuario puso "2° Grupo A vs 2° Grupo B" en su código original, pero lo corregiré según FIFA si puedo.
        // Dejaremos la estructura pero llenando con equipos reales si el grupo terminó.

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
            matches[index] = m.copy(
                homeTeam = home ?: m.homeTeam,
                awayTeam = away ?: m.awayTeam
            )
        }
    }

    private fun fillWinners(matches: MutableList<Match>) {
        // IDs: 101..116 (Dieciseisavos), 117..124 (Octavos), 125..128 (Cuartos), 129..130 (Semis), 131 (Final), 132 (3er Puesto)
        
        // Octavos (117-124) dependen de Dieciseisavos
        for (i in 0..7) {
            val m1 = matches.find { it.id == 101 + i * 2 }
            val m2 = matches.find { it.id == 102 + i * 2 }
            val winner1 = getWinner(m1)
            val winner2 = getWinner(m2)
            updateKnockoutMatch(matches, 117 + i, winner1, winner2)
        }

        // Cuartos (125-128)
        for (i in 0..3) {
            val m1 = matches.find { it.id == 117 + i * 2 }
            val m2 = matches.find { it.id == 118 + i * 2 }
            updateKnockoutMatch(matches, 125 + i, getWinner(m1), getWinner(m2))
        }

        // Semis (129-130)
        for (i in 0..1) {
            val m1 = matches.find { it.id == 125 + i * 2 }
            val m2 = matches.find { it.id == 126 + i * 2 }
            updateKnockoutMatch(matches, 129 + i, getWinner(m1), getWinner(m2))
        }

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
