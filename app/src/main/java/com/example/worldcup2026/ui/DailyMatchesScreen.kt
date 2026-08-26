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
    var selectedTournamentIds by remember { 
        mutableStateOf(setOf(0)) 
    }

    val allTournamentsList = remember {
        listOf(0 to "🏆 Todos") + (internacionales + nacionales).map { it.id to it.name }
    }

    val matchesForSelectedDate = remember(matches, date, searchQuery, filterLiveOnly, selectedTournamentIds, favTournaments) {
        val dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        matches
            .filter { it.date?.startsWith(dateStr) == true }
            .filter { match ->
                val mId = match.tournament_id ?: 1
                if (selectedTournamentIds.contains(0) || selectedTournamentIds.isEmpty()) {
                    true
                } else {
                    selectedTournamentIds.contains(mId)
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

        // Filtro Por Torneo en 2 Renglones Compactos (Internacionales y Regionales)
        val intlList = remember { listOf(0 to "🏆 Todos") + internacionales.map { it.id to it.name } }
        val nacList = remember { nacionales.map { it.id to it.name } }

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            // Renglón 1: Internacionales
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(intlList) { (tId, tName) ->
                    val isSelected = (tId == 0 && (selectedTournamentIds.contains(0) || selectedTournamentIds.isEmpty())) || 
                                     (tId != 0 && selectedTournamentIds.contains(tId))
                    val isFav = tId in favTournaments
                    val labelText = if (isFav && tId != 0) "$tName ⭐" else tName
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            com.example.worldcup2026.data.util.SoundManager.playTic()
                            if (tId == 0) {
                                selectedTournamentIds = setOf(0)
                            } else {
                                val newSet = selectedTournamentIds.toMutableSet()
                                newSet.remove(0)
                                if (newSet.contains(tId)) {
                                    newSet.remove(tId)
                                    if (newSet.isEmpty()) newSet.add(0)
                                } else {
                                    newSet.add(tId)
                                }
                                selectedTournamentIds = newSet
                            }
                        },
                        label = { 
                            Text(
                                text = labelText, 
                                fontSize = 10.sp, 
                                fontWeight = if (isSelected || isFav) FontWeight.Bold else FontWeight.Normal, 
                                color = if (isSelected) Color.Black else if (isFav) Color(0xFFFFC107) else Color.White.copy(alpha = 0.8f)
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFC107),
                            containerColor = if (isFav) Color(0xFFFFC107).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.08f)
                        ),
                        border = if (isFav && !isSelected) androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFC107).copy(alpha = 0.5f)) else null,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // Renglón 2: Regionales / Locales
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(nacList) { (tId, tName) ->
                    val isSelected = selectedTournamentIds.contains(tId)
                    val isFav = tId in favTournaments
                    val labelText = if (isFav) "$tName ⭐" else tName
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            com.example.worldcup2026.data.util.SoundManager.playTic()
                            val newSet = selectedTournamentIds.toMutableSet()
                            newSet.remove(0)
                            if (newSet.contains(tId)) {
                                newSet.remove(tId)
                                if (newSet.isEmpty()) newSet.add(0)
                            } else {
                                newSet.add(tId)
                            }
                            selectedTournamentIds = newSet
                        },
                        label = { 
                            Text(
                                text = labelText, 
                                fontSize = 10.sp, 
                                fontWeight = if (isSelected || isFav) FontWeight.Bold else FontWeight.Normal, 
                                color = if (isSelected) Color.Black else if (isFav) Color(0xFFFFC107) else Color.White.copy(alpha = 0.8f)
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFC107),
                            containerColor = if (isFav) Color(0xFFFFC107).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.08f)
                        ),
                        border = if (isFav && !isSelected) androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFC107).copy(alpha = 0.5f)) else null,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }

        val tournamentMatches = remember(matches, selectedTournamentIds, searchQuery, filterLiveOnly) {
            matches.filter { match ->
                val mId = match.tournament_id ?: 1
                val matchesTournament = selectedTournamentIds.contains(0) || selectedTournamentIds.isEmpty() || selectedTournamentIds.contains(mId)
                val matchesSearch = if (searchQuery.isNotBlank()) {
                    match.homeTeam.name.contains(searchQuery, ignoreCase = true) ||
                    match.awayTeam.name.contains(searchQuery, ignoreCase = true)
                } else true
                matchesTournament && matchesSearch
            }.sortedByDescending { it.date ?: "" }
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
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (filterLiveOnly) "No hay partidos en vivo en esta fecha." else "No hay partidos programados para la fecha seleccionada.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (tournamentMatches.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = Color(0xFFFFC107).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "📌 Partidos disponibles de este torneo en otras fechas:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFC107),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }

                if (tournamentMatches.isNotEmpty()) {
                    items(tournamentMatches) { match ->
                        val tName = resolveTournamentName(match)
                        MatchCard(
                            match = match,
                            onScoreChange = { matchId, hScore, aScore ->
                                viewModel.updateMatchScore(matchId, hScore, aScore)
                            },
                            onPenaltiesChange = { matchId, hPen, aPen ->
                                viewModel.updateMatchPenalties(matchId, hPen, aPen)
                            },
                            onStatusChange = { matchId, status ->
                                viewModel.updateMatchStatus(matchId, status)
                            },
                            onShowVipStats = onShowVipStats,
                            onPredictionChange = { matchId, winner, hScore, aScore, hPen, aPen ->
                                viewModel.updateMatchPrediction(matchId, winner, hScore, aScore, hPen, aPen)
                            },
                            onNavigateToTournament = onNavigateToTournament,
                            tournamentName = tName,
                            allMatches = matches,
                            onToggleComodin = { matchId ->
                                viewModel.toggleComodin(matchId)
                            }
                        )
                    }
                }
            } else {
                items(matchesForSelectedDate) { match ->
                    val tName = resolveTournamentName(match)

                    MatchCard(
                        match = match,
                        onScoreChange = { matchId, hScore, aScore ->
                            viewModel.updateMatchScore(matchId, hScore, aScore)
                        },
                        onPenaltiesChange = { matchId, hPen, aPen ->
                            viewModel.updateMatchPenalties(matchId, hPen, aPen)
                        },
                        onStatusChange = { matchId, status ->
                            viewModel.updateMatchStatus(matchId, status)
                        },
                        onShowVipStats = onShowVipStats,
                        onPredictionChange = { matchId, winner, hScore, aScore, hPen, aPen ->
                            viewModel.updateMatchPrediction(matchId, winner, hScore, aScore, hPen, aPen)
                        },
                        onNavigateToTournament = onNavigateToTournament,
                        tournamentName = tName,
                        allMatches = matches,
                        onToggleComodin = { matchId ->
                            viewModel.toggleComodin(matchId)
                        }
                    )
                }
            }
        }
    }
}

private fun resolveTournamentName(match: Match): String {
    val allTournaments = internacionales + nacionales
    val tId = match.tournament_id
    if (tId != null) {
        val found = allTournaments.find { it.id == tId }?.name
        if (found != null) return found
    }
    val contextText = "${match.homeTeam.group} ${match.stadium} ${match.city}".lowercase()
    return when {
        contextText.contains("primera c") || contextText.contains("metro c") -> "Primera C Metropolitana"
        contextText.contains("promocional") || contextText.contains("amateur") -> "Torneo Promocional Amateur"
        contextText.contains("primera b") -> "Primera B Metropolitana"
        contextText.contains("nacional") -> "Primera Nacional"
        contextText.contains("libertadores") -> "Copa CONMEBOL Libertadores"
        contextText.contains("sudamericana") -> "Copa CONMEBOL Sudamericana"
        contextText.contains("argentina") -> "Copa Argentina"
        else -> "Liga Profesional"
    }
}
