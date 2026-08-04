package com.example.worldcup2026.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

import com.example.worldcup2026.ui.internacionales
import com.example.worldcup2026.ui.nacionales
import com.example.worldcup2026.data.model.Match

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMatchesScreen(
    date: LocalDate,
    matches: List<Match>,
    viewModel: WorldCupViewModel,
    onNavigateToTournament: (Int) -> Unit,
    onShowVipStats: (Match) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterLiveOnly by remember { mutableStateOf(false) }
    val favTournaments by viewModel.favoriteTournamentIds
    var selectedTournamentId by remember(favTournaments) { 
        mutableIntStateOf(favTournaments.firstOrNull() ?: 0) 
    }

    val allTournamentsList = remember {
        listOf(0 to "🏆 Todos") + (internacionales + nacionales).map { it.id to it.name }
    }

    val matchesForSelectedDate = remember(matches, date, searchQuery, filterLiveOnly, selectedTournamentId, favTournaments) {
        val dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        matches
            .filter { it.date?.startsWith(dateStr) == true }
            .filter { match ->
                val mId = match.tournament_id ?: 1
                if (selectedTournamentId != 0) {
                    mId == selectedTournamentId
                } else {
                    true
                }
            }
            .filter { match ->
                val statusUpper = match.status.uppercase()
                val isLive = statusUpper in listOf("LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE")
                val matchesLive = if (filterLiveOnly) isLive else true
                val matchesSearch = if (searchQuery.isNotBlank()) {
                    match.homeTeam.name.contains(searchQuery, ignoreCase = true) ||
                    match.awayTeam.name.contains(searchQuery, ignoreCase = true)
                } else true
                matchesLive && matchesSearch
            }
            // Deduplicación inteligente por nombres de equipos: da prioridad al partido en vivo / jugado sobre el vacio
            .sortedWith(
                compareByDescending<Match> { it.status != "Scheduled" }
                    .thenByDescending { (it.homeScore ?: -1) + (it.awayScore ?: -1) }
            )
            .distinctBy { "${it.homeTeam.name.lowercase().trim()}_vs_${it.awayTeam.name.lowercase().trim()}" }
            .sortedBy { it.date?.substringAfter(" ", "00:00") ?: "00:00" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de Búsqueda y Filtro "🔴 En Vivo"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar equipo...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            FilterChip(
                selected = filterLiveOnly,
                onClick = { filterLiveOnly = !filterLiveOnly },
                label = { Text("🔴 En Vivo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (filterLiveOnly) Color.White else Color.White.copy(alpha = 0.7f)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE53935),
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                border = null,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Filtro Por Torneo (Chips horizontales scrollables)
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allTournamentsList) { (tId, tName) ->
                val isSelected = selectedTournamentId == tId
                val isFav = tId in favTournaments
                val labelText = if (isFav && tId != 0) "$tName ⭐" else tName
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTournamentId = tId },
                    label = { 
                        Text(
                            text = labelText, 
                            fontSize = 11.sp, 
                            fontWeight = if (isSelected || isFav) FontWeight.Bold else FontWeight.Normal, 
                            color = if (isSelected) Color.White else if (isFav) Color(0xFFFFC107) else Color.White.copy(alpha = 0.7f)
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        containerColor = if (isFav) Color(0xFFFFC107).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.08f)
                    ),
                    border = if (isFav && !isSelected) androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFC107).copy(alpha = 0.5f)) else null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (matchesForSelectedDate.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filterLiveOnly) "No hay partidos en vivo en esta fecha." else "No hay partidos programados con estos filtros.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val allTournaments = internacionales + nacionales
                
                items(matchesForSelectedDate) { match ->
                    val tName = allTournaments.find { it.id == (match.tournament_id ?: 1) }?.name ?: "Torneo Desconocido"

                    MatchCard(
                        match = match,
                        onScoreChange = { matchId, home, away -> viewModel.updateMatchScore(matchId, home, away) },
                        onPenaltiesChange = { matchId, home, away -> viewModel.updateMatchPenalties(matchId, home, away) },
                        onStatusChange = { matchId, status -> viewModel.updateMatchStatus(matchId, status) },
                        onShowVipStats = onShowVipStats,
                        onPredictionChange = { matchId, winner, h, a, hp, ap -> viewModel.updateMatchPrediction(matchId, winner, h, a, hp, ap) },
                        onNavigateToTournament = { id -> onNavigateToTournament(id) },
                        tournamentName = tName,
                        allMatches = matches
                    )
                }
            }
        }
    }
}
