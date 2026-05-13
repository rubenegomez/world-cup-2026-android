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
            Group("A", listOf(createTeam(1, "México", "mx", "A"), createTeam(2, "Sudáfrica", "za", "A"), createTeam(3, "Corea del Sur", "kr", "A"), createTeam(4, "República Checa", "cz", "A"))),
            Group("B", listOf(createTeam(5, "Canadá", "ca", "B"), createTeam(6, "Bosnia", "ba", "B"), createTeam(7, "Qatar", "qa", "B"), createTeam(8, "Suiza", "ch", "B"))),
            Group("C", listOf(createTeam(9, "Brasil", "br", "C"), createTeam(10, "Marruecos", "ma", "C"), createTeam(11, "Haití", "ht", "C"), createTeam(12, "Escocia", "gb-sct", "C"))),
            Group("D", listOf(createTeam(13, "Estados Unidos", "us", "D"), createTeam(14, "Paraguay", "py", "D"), createTeam(15, "Australia", "au", "D"), createTeam(16, "Turquía", "tr", "D"))),
            Group("E", listOf(createTeam(17, "Alemania", "de", "E"), createTeam(18, "Curazao", "cw", "E"), createTeam(19, "Costa de Marfil", "ci", "E"), createTeam(20, "Ecuador", "ec", "E"))),
            Group("F", listOf(createTeam(21, "Países Bajos", "nl", "F"), createTeam(22, "Japón", "jp", "F"), createTeam(23, "Suecia", "se", "F"), createTeam(24, "Túnez", "tn", "F"))),
            Group("G", listOf(createTeam(25, "Bélgica", "be", "G"), createTeam(26, "Egipto", "eg", "G"), createTeam(27, "Irán", "ir", "G"), createTeam(28, "Nueva Zelanda", "nz", "G"))),
            Group("H", listOf(createTeam(29, "España", "es", "H"), createTeam(30, "Cabo Verde", "cv", "H"), createTeam(31, "Arabia Saudita", "sa", "H"), createTeam(32, "Uruguay", "uy", "H"))),
            Group("I", listOf(createTeam(33, "Francia", "fr", "I"), createTeam(34, "Senegal", "sn", "I"), createTeam(35, "Irak", "iq", "I"), createTeam(36, "Noruega", "no", "I"))),
            Group("J", listOf(createTeam(37, "Argentina", "ar", "J"), createTeam(38, "Argelia", "dz", "J"), createTeam(39, "Austria", "at", "J"), createTeam(40, "Jordania", "jo", "J"))),
            Group("K", listOf(createTeam(41, "Portugal", "pt", "K"), createTeam(42, "RD Congo", "cd", "K"), createTeam(43, "Uzbekistán", "uz", "K"), createTeam(44, "Colombia", "co", "K"))),
            Group("L", listOf(createTeam(45, "Inglaterra", "gb-eng", "L"), createTeam(46, "Croacia", "hr", "L"), createTeam(47, "Ghana", "gh", "L"), createTeam(48, "Panamá", "pa", "L")))
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

        // FASE DE GRUPOS (IDs 1-72)
        MatchData.groupMatches.forEach { info ->
            addMatchWithPersistence(
                matches, 
                savedMatches, 
                Match(
                    info.id, 
                    findTeam(info.home), 
                    findTeam(info.away), 
                    null, null, null, null, 
                    info.date, 
                    "Scheduled",
                    info.stadium,
                    info.city
                )
            )
        }

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

    suspend fun saveMatchScore(matchId: Int, homeScore: Int?, awayScore: Int?, homePenalties: Int? = null, awayPenalties: Int? = null, status: String? = null) {
        val finalStatus = status ?: if (homeScore != null && awayScore != null) "Finished" else "Scheduled"
        matchDao.insertMatch(MatchEntity(matchId, homeScore, awayScore, homePenalties, awayPenalties, finalStatus))
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
            status
        ))
    }

    suspend fun getAllTeams(): List<Team> {
        return getMockGroups().flatMap { it.teams }
    }
}
