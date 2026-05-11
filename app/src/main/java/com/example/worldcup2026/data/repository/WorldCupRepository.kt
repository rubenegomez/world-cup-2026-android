package com.example.worldcup2026.data.repository

import com.example.worldcup2026.data.model.Group
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import kotlinx.coroutines.delay

class WorldCupRepository {

    // Mock data for initial development
    suspend fun getMockGroups(): List<Group> {
        delay(1000) // Simulate network delay
        return listOf(
            Group("A", listOf(
                Team(1, "USA", "https://flagcdn.com/w320/us.png", "A"),
                Team(2, "Mexico", "https://flagcdn.com/w320/mx.png", "A"),
                Team(3, "Canada", "https://flagcdn.com/w320/ca.png", "A"),
                Team(4, "Argentina", "https://flagcdn.com/w320/ar.png", "A")
            )),
            Group("B", listOf(
                Team(5, "Brazil", "https://flagcdn.com/w320/br.png", "B"),
                Team(6, "France", "https://flagcdn.com/w320/fr.png", "B"),
                Team(7, "Spain", "https://flagcdn.com/w320/es.png", "B"),
                Team(8, "Japan", "https://flagcdn.com/w320/jp.png", "B")
            ))
        )
    }

    suspend fun getMockMatches(): List<Match> {
        delay(800)
        val usa = Team(1, "USA", "https://flagcdn.com/w320/us.png", "A")
        val mexico = Team(2, "Mexico", "https://flagcdn.com/w320/mx.png", "A")
        
        return listOf(
            Match(101, usa, mexico, null, null, "2026-06-11 18:00", "Scheduled"),
            Match(102, Team(3, "Canada", "https://flagcdn.com/w320/ca.png", "A"), Team(4, "Argentina", "https://flagcdn.com/w320/ar.png", "A"), null, null, "2026-06-12 20:00", "Scheduled")
        )
    }
}
