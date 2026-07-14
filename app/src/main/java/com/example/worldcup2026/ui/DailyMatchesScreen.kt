package com.example.worldcup2026.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate

import com.example.worldcup2026.ui.internacionales
import com.example.worldcup2026.ui.nacionales
import com.example.worldcup2026.data.model.Match
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.text.font.FontWeight

@Composable
fun DailyMatchesScreen(
    date: LocalDate,
    matches: List<Match>,
    viewModel: WorldCupViewModel,
    onNavigateToTournament: (Int) -> Unit
) {
    val matchesForSelectedDate = remember(matches, date) {
        val dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        matches.filter { it.date == dateStr }.sortedBy { it.clock ?: "00:00" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (matchesForSelectedDate.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay partidos programados para este día.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val allTournaments = internacionales + nacionales
            val sortedMatches = matchesForSelectedDate.sortedBy { it.date }
            
            items(sortedMatches) { match ->
                val tName = allTournaments.find { it.id == (match.tournament_id ?: 1) }?.name ?: "Torneo Desconocido"

                MatchCard(
                    match = match,
                    onScoreChange = { matchId, home, away -> viewModel.updateMatchScore(matchId, home, away) },
                    onPenaltiesChange = { matchId, home, away -> viewModel.updateMatchPenalties(matchId, home, away) },
                    onStatusChange = { matchId, status -> viewModel.updateMatchStatus(matchId, status) },
                    onShowVipStats = { },
                    onPredictionChange = { matchId, winner, h, a, hp, ap -> viewModel.updateMatchPrediction(matchId, winner, h, a, hp, ap) },
                    onNavigateToTournament = { id -> onNavigateToTournament(id) },
                    tournamentName = tName
                )
            }
        }
    }
}
