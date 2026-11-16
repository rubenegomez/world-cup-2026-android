package com.example.worldcup2026.data.util

import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import kotlin.math.max
import kotlin.math.min

object KnockoutCalculator {
    private val EXACT_PLACEHOLDERS = setOf(
        "tbd", "tbd home", "tbd away", "por definir", "a confirmar",
        "sin definir", "local", "visitante", "equipo 1", "equipo 2",
        "desconocido", "ganador", "perdedor"
    )

    private val PLACEHOLDER_PREFIXES = listOf(
        "tbd", "por definir", "ganador ", "perdedor ", "winner ", "loser ",
        "semifinalista ", "cuartofinalista ", "clasificado ", "a confirmar"
    )

    private val SEED_REGEX_GROUP = Regex("^[123][a-l]$")
    private val SEED_REGEX_BRACKET = Regex("^[wlgcs]\\d{1,3}$")

    private val KNOCKOUT_SEQUENCES: Map<Int, List<List<Int>>> = mapOf(
        6 to listOf(listOf(1, 2, 3, 4, 5, 6)),
        5 to listOf(listOf(17, 18, 19, 20)),
        3 to listOf(listOf(10, 11, 12, 13)),
        4 to listOf(listOf(8, 9, 10, 11, 12)),
        8 to listOf(listOf(43, 44, 45)),
        15 to listOf(listOf(10, 11, 12, 13, 14), listOf(15, 16, 17))
    )

    fun isPlaceholderTeam(team: Team?): Boolean {
        if (team == null) return true
        if (team.group in listOf("X", "Final", "TBD")) return true
        val lower = team.name.trim().lowercase()
        if (lower.isEmpty() || lower in EXACT_PLACEHOLDERS) return true
        if (PLACEHOLDER_PREFIXES.any { lower.startsWith(it) }) return true
        return SEED_REGEX_GROUP.matches(lower) || SEED_REGEX_BRACKET.matches(lower)
    }

    fun calculateKnockoutMatches(allMatches: List<Match>, tournamentId: Int): List<Match> {
        val knockoutMatches = allMatches.filter { it.id > 72 }.toMutableList()

        if (tournamentId != 1) {
            return knockoutMatches
        }

        fillWinners(knockoutMatches)

        return knockoutMatches
    }

    fun propagateTournamentKnockouts(allMatches: List<Match>): List<Match> {
        if (allMatches.isEmpty()) return allMatches
        val mutable = allMatches.toMutableList()

        for ((tournamentId, sequences) in KNOCKOUT_SEQUENCES) {
            val tournamentMatches = mutable.filter { (it.tournament_id ?: 1) == tournamentId }
            if (tournamentMatches.isEmpty()) continue

            for (seq in sequences) {
                for (i in 0 until seq.size - 1) {
                    val prevMd = seq[i]
                    val nextMd = seq[i + 1]
                    val prevMatches = mutable.filter {
                        (it.tournament_id ?: 1) == tournamentId && it.matchday == prevMd
                    }
                    val nextIndices = mutable.indices.filter { idx ->
                        val m = mutable[idx]
                        (m.tournament_id ?: 1) == tournamentId && m.matchday == nextMd
                    }
                    if (prevMatches.isEmpty() || nextIndices.isEmpty()) continue

                    val seriesGroups = linkedMapOf<Pair<Int, Int>, MutableList<Match>>()
                    for (pm in prevMatches.sortedWith(compareBy({ it.date ?: "" }, { it.id }))) {
                        if (isPlaceholderTeam(pm.homeTeam) || isPlaceholderTeam(pm.awayTeam)) continue
                        val a = min(pm.homeTeam.id, pm.awayTeam.id)
                        val b = max(pm.homeTeam.id, pm.awayTeam.id)
                        seriesGroups.getOrPut(a to b) { mutableListOf() }.add(pm)
                    }

                    data class SeriesOutcome(val winner: Team, val namesLower: List<String>)
                    val outcomes = seriesGroups.values.mapNotNull { sMatches ->
                        val w = getSeriesWinner(sMatches) ?: return@mapNotNull null
                        val tNames = listOf(
                            sMatches.first().homeTeam.name.lowercase(),
                            sMatches.first().awayTeam.name.lowercase()
                        )
                        SeriesOutcome(w, tNames)
                    }
                    if (outcomes.isEmpty()) continue

                    fun findHintWinner(placeholderName: String): Team? {
                        val phLow = placeholderName.trim().lowercase()
                        for (out in outcomes) {
                            for (nm in out.namesLower) {
                                val shortNm = if (nm.contains(" ")) nm.substringBefore(" ") else nm
                                if (nm in phLow || (shortNm.length >= 5 && shortNm in phLow)) {
                                    return out.winner
                                }
                            }
                        }
                        return null
                    }

                    for (nextIdx in nextIndices) {
                        val nm = mutable[nextIdx]
                        if (nm.status.equals("Finished", ignoreCase = true) || nm.status.equals("LIVE", ignoreCase = true)) {
                            continue
                        }
                        var curHome = nm.homeTeam
                        var curAway = nm.awayTeam
                        var hPh = isPlaceholderTeam(curHome)
                        var aPh = isPlaceholderTeam(curAway)
                        if (!hPh && !aPh) continue

                        if (hPh) {
                            val hintW = findHintWinner(curHome.name)
                            if (hintW != null && hintW.id != curAway.id) {
                                curHome = hintW
                                hPh = false
                            }
                        }
                        if (aPh) {
                            val hintW = findHintWinner(curAway.name)
                            if (hintW != null && hintW.id != curHome.id) {
                                curAway = hintW
                                aPh = false
                            }
                        }

                        if (nextIndices.size == 1 && (hPh || aPh)) {
                            val assignedIds = mutableSetOf<Int>()
                            if (!hPh) assignedIds.add(curHome.id)
                            if (!aPh) assignedIds.add(curAway.id)
                            val remaining = outcomes.map { it.winner }.filter { it.id !in assignedIds }.toMutableList()
                            if (hPh && remaining.isNotEmpty()) {
                                curHome = remaining.removeAt(0)
                            }
                            if (aPh && remaining.isNotEmpty()) {
                                curAway = remaining.removeAt(0)
                            }
                        }

                        if (curHome != nm.homeTeam || curAway != nm.awayTeam) {
                            mutable[nextIdx] = nm.copy(homeTeam = curHome, awayTeam = curAway)
                        }
                    }
                }
            }
        }
        return mutable
    }

    private fun hasRealTeam(team: Team): Boolean = !isPlaceholderTeam(team)

    private fun updateKnockoutMatch(matches: MutableList<Match>, id: Int, home: Team?, away: Team?) {
        val index = matches.indexOfFirst { it.id == id }
        if (index != -1) {
            val m = matches[index]
            if (m.status.equals("Finished", ignoreCase = true) || m.status.equals("LIVE", ignoreCase = true)) {
                return
            }
            val needHome = isPlaceholderTeam(m.homeTeam) && home != null && !isPlaceholderTeam(home)
            val needAway = isPlaceholderTeam(m.awayTeam) && away != null && !isPlaceholderTeam(away)
            if (!needHome && !needAway) {
                return
            }
            matches[index] = m.copy(
                homeTeam = if (needHome) home!! else m.homeTeam,
                awayTeam = if (needAway) away!! else m.awayTeam
            )
        }
    }

    private fun fillWinners(matches: MutableList<Match>) {
        // Rango oficial Mundial 2026 (TournamentConfig: 73..104)
        updateKnockoutMatch(matches, 89, getWinner(matches.find { it.id == 73 }), getWinner(matches.find { it.id == 74 }))
        updateKnockoutMatch(matches, 90, getWinner(matches.find { it.id == 75 }), getWinner(matches.find { it.id == 76 }))
        updateKnockoutMatch(matches, 91, getWinner(matches.find { it.id == 77 }), getWinner(matches.find { it.id == 78 }))
        updateKnockoutMatch(matches, 92, getWinner(matches.find { it.id == 79 }), getWinner(matches.find { it.id == 80 }))
        updateKnockoutMatch(matches, 93, getWinner(matches.find { it.id == 81 }), getWinner(matches.find { it.id == 82 }))
        updateKnockoutMatch(matches, 94, getWinner(matches.find { it.id == 83 }), getWinner(matches.find { it.id == 84 }))
        updateKnockoutMatch(matches, 95, getWinner(matches.find { it.id == 85 }), getWinner(matches.find { it.id == 86 }))
        updateKnockoutMatch(matches, 96, getWinner(matches.find { it.id == 87 }), getWinner(matches.find { it.id == 88 }))

        updateKnockoutMatch(matches, 97, getWinner(matches.find { it.id == 89 }), getWinner(matches.find { it.id == 90 }))
        updateKnockoutMatch(matches, 98, getWinner(matches.find { it.id == 91 }), getWinner(matches.find { it.id == 92 }))
        updateKnockoutMatch(matches, 99, getWinner(matches.find { it.id == 93 }), getWinner(matches.find { it.id == 94 }))
        updateKnockoutMatch(matches, 100, getWinner(matches.find { it.id == 95 }), getWinner(matches.find { it.id == 96 }))

        updateKnockoutMatch(matches, 101, getWinner(matches.find { it.id == 97 }), getWinner(matches.find { it.id == 98 }))
        updateKnockoutMatch(matches, 102, getWinner(matches.find { it.id == 99 }), getWinner(matches.find { it.id == 100 }))

        val hasLegacyBracket = matches.any { it.id in 117..132 }
        if (!hasLegacyBracket) {
            val s1Official = matches.find { it.id == 101 }
            val s2Official = matches.find { it.id == 102 }
            updateKnockoutMatch(matches, 103, getLoser(s1Official), getLoser(s2Official))
            updateKnockoutMatch(matches, 104, getWinner(s1Official), getWinner(s2Official))
        } else {
            // Respaldo de llaves legadas 101..132
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
    }

    fun getSeriesWinner(seriesMatches: List<Match>): Team? {
        if (seriesMatches.isEmpty()) return null
        if (seriesMatches.any { !it.status.equals("Finished", ignoreCase = true) }) return null
        if (seriesMatches.size == 1) return getWinner(seriesMatches[0])

        val sorted = seriesMatches.sortedWith(compareBy({ it.date ?: "" }, { it.id }))
        val teamA = sorted.first().homeTeam
        val teamB = sorted.first().awayTeam
        if (isPlaceholderTeam(teamA) || isPlaceholderTeam(teamB)) return null

        var goalsA = 0
        var goalsB = 0
        for (m in sorted) {
            if (m.homeTeam.id == teamA.id) {
                goalsA += m.homeScore ?: 0
                goalsB += m.awayScore ?: 0
            } else {
                goalsB += m.homeScore ?: 0
                goalsA += m.awayScore ?: 0
            }
        }
        return when {
            goalsA > goalsB -> teamA
            goalsB > goalsA -> teamB
            else -> {
                val last = sorted.last()
                val hp = last.homePenalties ?: 0
                val ap = last.awayPenalties ?: 0
                if (hp > ap) last.homeTeam else if (ap > hp) last.awayTeam else getWinner(last)
            }
        }
    }

    fun getWinner(match: Match?): Team? {
        if (match == null || !match.status.equals("Finished", ignoreCase = true)) return null
        val h = match.homeScore ?: 0
        val a = match.awayScore ?: 0
        return if (h > a) match.homeTeam
               else if (a > h) match.awayTeam
               else if ((match.homePenalties ?: 0) > (match.awayPenalties ?: 0)) match.homeTeam
               else match.awayTeam
    }

    private fun getLoser(match: Match?): Team? {
        if (match == null || !match.status.equals("Finished", ignoreCase = true)) return null
        val h = match.homeScore ?: 0
        val a = match.awayScore ?: 0
        return if (h < a) match.homeTeam
               else if (a < h) match.awayTeam
               else if ((match.homePenalties ?: 0) < (match.awayPenalties ?: 0)) match.homeTeam
               else match.awayTeam
    }
}
