package com.example.worldcup2026.data.repository

import com.example.worldcup2026.data.local.MatchDao
import com.example.worldcup2026.data.local.MatchEntity
import com.example.worldcup2026.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WorldCupRepository(private val matchDao: MatchDao) {

    suspend fun getMockGroups(): List<Group> {
        delay(100)
        return listOf(
            Group("A", listOf(createTeam(1, "México", "mx", "A"), createTeam(2, "Sudáfrica", "za", "A"), createTeam(3, "Corea Rep.", "kr", "A"), createTeam(4, "N. Zelanda", "nz", "A"))),
            Group("B", listOf(createTeam(5, "Canadá", "ca", "B"), createTeam(6, "Bosnia", "ba", "B"), createTeam(7, "Qatar", "qa", "B"), createTeam(8, "Irak", "iq", "B"))),
            Group("C", listOf(createTeam(9, "EE.UU.", "us", "C"), createTeam(10, "Paraguay", "py", "C"), createTeam(11, "Australia", "au", "C"), createTeam(12, "Jordania", "jo", "C"))),
            Group("D", listOf(createTeam(13, "Francia", "fr", "D"), createTeam(14, "Senegal", "sn", "D"), createTeam(15, "Irán", "ir", "D"), createTeam(16, "Noruega", "no", "D"))),
            Group("E", listOf(createTeam(17, "Alemania", "de", "E"), createTeam(18, "Ghana", "gh", "E"), createTeam(19, "Rep. Checa", "cz", "E"), createTeam(20, "Jamaica", "jm", "E"))),
            Group("F", listOf(createTeam(21, "Brasil", "br", "F"), createTeam(22, "Marruecos", "ma", "F"), createTeam(23, "Suecia", "se", "F"), createTeam(24, "Túnez", "tn", "F"))),
            Group("G", listOf(createTeam(25, "Argentina", "ar", "G"), createTeam(26, "Argelia", "dz", "G"), createTeam(27, "Austria", "at", "G"), createTeam(28, "Turquía", "tr", "G"))),
            Group("H", listOf(createTeam(29, "Bélgica", "be", "H"), createTeam(30, "Egipto", "eg", "H"), createTeam(31, "Eslovaquia", "sk", "H"), createTeam(32, "Honduras", "hn", "H")))
        )
    }

    private fun createTeam(id: Int, name: String, flagCode: String, group: String): Team {
        return Team(id, name, "https://flagcdn.com/w160/$flagCode.png", group, emptyList())
    }

    private fun createPlaceholderTeam(name: String): Team {
        return Team(-1, name, "", "Final", emptyList())
    }

    suspend fun getMatches(): List<Match> {
        val allTeams = getAllTeams()
        val savedMatches = matchDao.getAllMatches().first()
        val matches = mutableListOf<Match>()
        fun findTeam(name: String) = allTeams.find { it.name == name } ?: Team(0, name, "", "", emptyList())

        // FASE DE GRUPOS
        addMatchWithPersistence(matches, savedMatches, Match(1, findTeam("México"), findTeam("Sudáfrica"), null, null, null, null, "2026-06-11 CDMX", "Scheduled"))
        addMatchWithPersistence(matches, savedMatches, Match(2, findTeam("Canadá"), findTeam("Bosnia"), null, null, null, null, "2026-06-12 Toronto", "Scheduled"))
        addMatchWithPersistence(matches, savedMatches, Match(3, findTeam("EE.UU."), findTeam("Paraguay"), null, null, null, null, "2026-06-12 LA", "Scheduled"))

        // DIECISEISAVOS (IDs 101-116)
        for (i in 1..16) {
            val date = if (i == 1) "2026-06-28 Los Ángeles" else "Por definir"
            val home = if (i == 1) createPlaceholderTeam("2° Grupo A") else createPlaceholderTeam("Ganador $i")
            val away = if (i == 1) createPlaceholderTeam("2° Grupo B") else createPlaceholderTeam("Rival $i")
            addMatchWithPersistence(matches, savedMatches, Match(100 + i, home, away, null, null, null, null, date, "Scheduled"))
        }

        // OCTAVOS (IDs 117-124)
        for (i in 1..8) {
            addMatchWithPersistence(matches, savedMatches, Match(116 + i, createPlaceholderTeam("Ganador 16avo $i"), createPlaceholderTeam("Rival 16avo $i"), null, null, null, null, "Por definir", "Scheduled"))
        }

        // CUARTOS (IDs 125-128)
        for (i in 1..4) {
            addMatchWithPersistence(matches, savedMatches, Match(124 + i, createPlaceholderTeam("Ganador Octavos $i"), createPlaceholderTeam("Rival Octavos $i"), null, null, null, null, "Por definir", "Scheduled"))
        }

        // SEMIFINALES (IDs 129-130)
        for (i in 1..2) {
            addMatchWithPersistence(matches, savedMatches, Match(128 + i, createPlaceholderTeam("Ganador Cuartos $i"), createPlaceholderTeam("Rival Cuartos $i"), null, null, null, null, "Por definir", "Scheduled"))
        }

        // FINAL (131)
        addMatchWithPersistence(matches, savedMatches, Match(131, createPlaceholderTeam("Ganador Semi 1"), createPlaceholderTeam("Ganador Semi 2"), null, null, null, null, "2026-07-19 NY/NJ", "Scheduled"))

        // TERCER PUESTO (132)
        addMatchWithPersistence(matches, savedMatches, Match(132, createPlaceholderTeam("Perdedor Semi 1"), createPlaceholderTeam("Perdedor Semi 2"), null, null, null, null, "2026-07-18 Miami", "Scheduled"))

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
                status = saved.status
            ))
        } else {
            targetList.add(baseMatch)
        }
    }

    suspend fun saveMatchScore(matchId: Int, homeScore: Int?, awayScore: Int?, homePenalties: Int? = null, awayPenalties: Int? = null) {
        val status = if (homeScore != null && awayScore != null) "Finished" else "Scheduled"
        matchDao.insertMatch(MatchEntity(matchId, homeScore, awayScore, homePenalties, awayPenalties, status))
    }

    suspend fun getAllTeams(): List<Team> {
        return getMockGroups().flatMap { it.teams }
    }
}
