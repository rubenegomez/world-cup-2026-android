package com.example.worldcup2026.data.model

object HistoricalH2HCatalog {

    private fun normalize(name: String): String {
        return name.trim().lowercase()
            .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
    }

    // Registro canónico de cruces oficiales previos entre equipos
    private val HISTORICAL_MATCHES = listOf(
        // Superclásicos Boca Juniors vs River Plate
        Match(
            id = 99001,
            tournament_id = 17, // Torneo Apertura Liga Profesional 2026
            homeTeam = Team(id = 8302, name = "River Plate", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/16.png", group = "Zona B", players = emptyList()),
            awayTeam = Team(id = 8301, name = "Boca Juniors", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/5.png", group = "Zona A", players = emptyList()),
            homeScore = 0,
            awayScore = 1,
            date = "2026-04-19 17:00",
            status = "Finished",
            stadium = "Estadio Mâs Monumental",
            city = "Buenos Aires"
        ),
        Match(
            id = 99002,
            tournament_id = 5, // Liga Profesional 2025
            homeTeam = Team(id = 8301, name = "Boca Juniors", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/5.png", group = "Zona A", players = emptyList()),
            awayTeam = Team(id = 8302, name = "River Plate", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/16.png", group = "Zona B", players = emptyList()),
            homeScore = 2,
            awayScore = 0,
            date = "2025-11-09 17:00",
            status = "Finished",
            stadium = "Estadio Alberto J. Armando (La Bombonera)",
            city = "Buenos Aires"
        ),
        Match(
            id = 99003,
            tournament_id = 17, // Copa de la Liga / Apertura 2025
            homeTeam = Team(id = 8302, name = "River Plate", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/16.png", group = "Zona B", players = emptyList()),
            awayTeam = Team(id = 8301, name = "Boca Juniors", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/5.png", group = "Zona A", players = emptyList()),
            homeScore = 2,
            awayScore = 1,
            date = "2025-04-27 16:30",
            status = "Finished",
            stadium = "Estadio Mâs Monumental",
            city = "Buenos Aires"
        ),
        Match(
            id = 99004,
            tournament_id = 5, // Liga Profesional 2024
            homeTeam = Team(id = 8301, name = "Boca Juniors", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/5.png", group = "Zona A", players = emptyList()),
            awayTeam = Team(id = 8302, name = "River Plate", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/16.png", group = "Zona B", players = emptyList()),
            homeScore = 0,
            awayScore = 1,
            date = "2024-09-21 16:00",
            status = "Finished",
            stadium = "Estadio Alberto J. Armando (La Bombonera)",
            city = "Buenos Aires"
        ),
        Match(
            id = 99005,
            tournament_id = 17, // Copa de la Liga 2024 (Cuartos)
            homeTeam = Team(id = 8302, name = "River Plate", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/16.png", group = "Zona B", players = emptyList()),
            awayTeam = Team(id = 8301, name = "Boca Juniors", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/5.png", group = "Zona A", players = emptyList()),
            homeScore = 2,
            awayScore = 3,
            date = "2024-04-21 15:30",
            status = "Finished",
            stadium = "Estadio Mario Alberto Kempes",
            city = "Córdoba"
        )
    )

    fun getHistoricalMatches(team1: String, team2: String): List<Match> {
        val n1 = normalize(team1)
        val n2 = normalize(team2)
        if (n1.isBlank() || n2.isBlank()) return emptyList()

        return HISTORICAL_MATCHES.filter { m ->
            val hn = normalize(m.homeTeam.name)
            val an = normalize(m.awayTeam.name)
            (hn.contains(n1) || n1.contains(hn)) && (an.contains(n2) || n2.contains(an)) ||
            (hn.contains(n2) || n2.contains(hn)) && (an.contains(n1) || n1.contains(an))
        }
    }
}
