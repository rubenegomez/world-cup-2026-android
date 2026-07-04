package com.example.worldcup2026.data.util

import java.time.LocalDateTime

object TournamentConfig {
    // Flag central para migrar entre el Mundial y el Campeonato Local
    const val IS_WORLD_CUP = true

    // Textos generales del torneo
    val TOURNAMENT_NAME = if (IS_WORLD_CUP) "MUNDIAL 2026" else "CAMPEONATO LOCAL"
    
    // Cuenta regresiva
    val COUNTDOWN_TARGET_DATE: LocalDateTime = if (IS_WORLD_CUP) {
        LocalDateTime.of(2026, 6, 11, 0, 0, 0)
    } else {
        LocalDateTime.of(2026, 8, 1, 0, 0, 0) // Fecha tentativa para campeonato local
    }
    
    val COUNTDOWN_TITLE = if (IS_WORLD_CUP) "CAMINO AL MUNDIAL 2026" else "CAMINO AL CAMPEONATO LOCAL"
    val COUNTDOWN_STARTED_TEXT = if (IS_WORLD_CUP) "⚽ ¡EL MUNDIAL HA COMENZADO! ⚽" else "⚽ ¡EL CAMPEONATO LOCAL HA COMENZADO! ⚽"

    // Fases de eliminación directa y sus rangos de IDs de partido
    data class KnockoutRoundConfig(
        val name: String,
        val startId: Int,
        val endId: Int
    )

    val KNOCKOUT_ROUNDS: List<KnockoutRoundConfig> = if (IS_WORLD_CUP) {
        listOf(
            KnockoutRoundConfig("DIECISEISAVOS", 73, 88),
            KnockoutRoundConfig("OCTAVOS", 89, 96),
            KnockoutRoundConfig("CUARTOS", 97, 100),
            KnockoutRoundConfig("SEMIFINAL", 101, 102),
            KnockoutRoundConfig("FINAL", 103, 104)
        )
    } else {
        listOf(
            // En el campeonato local: "hay octavos cuartos semifinal y final"
            KnockoutRoundConfig("OCTAVOS", 101, 108), // Ajustar según los IDs de partidos en BD local
            KnockoutRoundConfig("CUARTOS", 109, 112),
            KnockoutRoundConfig("SEMIFINAL", 113, 114),
            KnockoutRoundConfig("FINAL", 115, 116)
        )
    }

    // Nombres de ronda para el Diálogo de Recompensa del Prode
    fun getRoundName(round: Int): String {
        return if (IS_WORLD_CUP) {
            when (round) {
                1 -> "Fecha 1 (Grupos)"
                2 -> "Fecha 2 (Grupos)"
                3 -> "Fecha 3 (Grupos)"
                4 -> "Dieciseisavos de Final"
                5 -> "Octavos de Final"
                6 -> "Cuartos de Final"
                7 -> "Semifinales"
                8 -> "Final"
                else -> "Fecha $round"
            }
        } else {
            when (round) {
                1 -> "Fecha 1 (Grupos)"
                2 -> "Fecha 2 (Grupos)"
                3 -> "Fecha 3 (Grupos)"
                4 -> "Fecha 4 (Grupos)"
                5 -> "Fecha 5 (Grupos)"
                6 -> "Fecha 6 (Grupos)"
                10 -> "Octavos de Final"
                11 -> "Cuartos de Final"
                12 -> "Semifinales"
                13 -> "Final"
                else -> "Fecha $round"
            }
        }
    }
}
