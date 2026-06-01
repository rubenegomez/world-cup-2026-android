package com.example.worldcup2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worldcup2026.data.api.StatDto
import com.example.worldcup2026.data.api.TeamStatsDto

@Composable
fun VipStatsScreen(
    fixtureId: Int,
    viewModel: VipStatsViewModel = viewModel()
) {
    LaunchedEffect(fixtureId) {
        viewModel.loadStats(fixtureId)
    }

    val state by viewModel.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        when (val uiState = state) {
            is VipStatsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFFFD700)
                )
            }
            is VipStatsUiState.Error -> {
                Text(
                    text = uiState.message,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is VipStatsUiState.Success -> {
                val homeStats = uiState.homeStats
                val awayStats = uiState.awayStats
                
                if (homeStats == null || awayStats == null) {
                    Text(
                        text = "Estadísticas no disponibles aún para este partido.",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            TeamsHeader(homeStats.team.name, awayStats.team.name)
                        }
                        
                        item {
                            Divider(color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("GOLES", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }

                        if (uiState.events.isEmpty()) {
                            item {
                                Text("No hay goles registrados.", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            items(uiState.events) { event ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(text = "${event.time.elapsed}'", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                                    Text(text = "⚽ ${event.player.name} (${event.team.name})", color = Color.White)
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("ESTADÍSTICAS", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        val statsMap = mutableMapOf<String, Pair<String, String>>()
                        homeStats.statistics.forEach { stat ->
                            statsMap[stat.type] = Pair(stat.value?.toString() ?: "0", "0")
                        }
                        awayStats.statistics.forEach { stat ->
                            val current = statsMap[stat.type]
                            statsMap[stat.type] = Pair(current?.first ?: "0", stat.value?.toString() ?: "0")
                        }

                        val interestingStats = listOf(
                            "Ball Possession", "Total Shots", "Shots on Goal", 
                            "Fouls", "Corner Kicks", "Offsides", "Yellow Cards", "Red Cards"
                        )

                        items(interestingStats) { statType ->
                            statsMap[statType]?.let { (homeVal, awayVal) ->
                                StatBarRow(
                                    title = translateStat(statType),
                                    homeValue = homeVal,
                                    awayValue = awayVal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamsHeader(homeTeam: String, awayTeam: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(homeTeam, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
        Text("VS", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
        Text(awayTeam, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
fun StatBarRow(title: String, homeValue: String, awayValue: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(homeValue, color = Color.White, fontWeight = FontWeight.Bold)
            Text(title, color = Color.LightGray, fontSize = 14.sp)
            Text(awayValue, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            horizontalArrangement = Arrangement.Center
        ) {
            val hVal = homeValue.replace("%", "").toFloatOrNull() ?: 0f
            val aVal = awayValue.replace("%", "").toFloatOrNull() ?: 0f
            val total = if (hVal + aVal == 0f) 1f else (hVal + aVal)
            val hRatio = hVal / total
            val aRatio = aVal / total

            Box(modifier = Modifier.weight(if (hRatio > 0) hRatio else 0.01f).fillMaxHeight().background(Color(0xFF3498DB)))
            Box(modifier = Modifier.weight(if (aRatio > 0) aRatio else 0.01f).fillMaxHeight().background(Color(0xFFE74C3C)))
        }
    }
}

fun translateStat(type: String): String {
    return when(type) {
        "Ball Possession" -> "Posesión"
        "Total Shots" -> "Tiros Totales"
        "Shots on Goal" -> "Tiros al Arco"
        "Fouls" -> "Faltas"
        "Corner Kicks" -> "Tiros de Esquina"
        "Offsides" -> "Fueras de Juego"
        "Yellow Cards" -> "Tarjetas Amarillas"
        "Red Cards" -> "Tarjetas Rojas"
        else -> type
    }
}
