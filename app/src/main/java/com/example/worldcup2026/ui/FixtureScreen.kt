package com.example.worldcup2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@Composable
fun FixtureScreen(
    matches: List<Match>,
    onScoreChange: (Int, Int?, Int?) -> Unit,
    onPenaltiesChange: (Int, Int?, Int?) -> Unit = { _, _, _ -> },
    onStatusChange: (Int, String) -> Unit = { _, _ -> },
    onShowVipStats: () -> Unit = {},
    onPredictionChange: (Int, String?, Int?, Int?) -> Unit = { _, _, _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("POR DÍA", "POR GRUPO", "ELIMINACIÓN")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> DayFilteredFixture(matches, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange)
                1 -> GroupFilteredFixture(matches, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange)
                2 -> KnockoutBracket(matches, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange)
            }
        }
    }
}

@Composable
fun KnockoutBracket(
    matches: List<Match>,
    onScoreChange: (Int, Int?, Int?) -> Unit,
    onPenaltiesChange: (Int, Int?, Int?) -> Unit,
    onStatusChange: (Int, String) -> Unit,
    onShowVipStats: () -> Unit,
    onPredictionChange: (Int, String?, Int?, Int?) -> Unit
) {
    val rounds = listOf("DIECISEISAVOS", "OCTAVOS", "CUARTOS", "SEMIFINAL", "FINAL")
    var selectedRound by remember { mutableStateOf(rounds[0]) }
    
    val filteredMatches = matches.filter { match ->
        when (selectedRound) {
            "DIECISEISAVOS" -> match.id in 101..116
            "OCTAVOS" -> match.id in 117..124
            "CUARTOS" -> match.id in 125..128
            "SEMIFINAL" -> match.id in 129..130
            "FINAL" -> match.id in 131..132
            else -> false
        }
    }

    Column {
        ScrollableTabRow(
            selectedTabIndex = rounds.indexOf(selectedRound),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            divider = {},
            indicator = {}
        ) {
            rounds.forEach { round ->
                Tab(
                    selected = selectedRound == round,
                    onClick = { selectedRound = round },
                    text = {
                        Text(text = round, style = MaterialTheme.typography.labelSmall)
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(filteredMatches) { index, match ->
                MatchCard(match, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange)
                if ((index + 1) % 5 == 0) {
                    SponsorCard(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun DayFilteredFixture(
    matches: List<Match>, 
    onScoreChange: (Int, Int?, Int?) -> Unit, 
    onPenaltiesChange: (Int, Int?, Int?) -> Unit,
    onStatusChange: (Int, String) -> Unit,
    onShowVipStats: () -> Unit,
    onPredictionChange: (Int, String?, Int?, Int?) -> Unit
) {
    // Extraemos la fecha completa (Día DD/MM) para mostrarla y ordenarla
    val dates = matches.filter { it.id <= 100 }
        .map { 
            val parts = it.date.split(" ")
            if (parts.size >= 2) "${parts[0]} ${parts[1]}" else it.date
        }
        .distinct()
        .sortedBy { dateStr ->
            // Ordenamos por el componente DD/MM (ej: "11/06")
            val datePart = dateStr.split(" ").lastOrNull() ?: ""
            val parts = datePart.split("/")
            if (parts.size == 2) {
                // Formato MMdd para ordenación simple (0611, 0612...)
                parts[1] + parts[0]
            } else {
                dateStr
            }
        }
    
    var selectedDate by remember { mutableStateOf(if (dates.isNotEmpty()) dates.first() else "") }
    val filteredMatches = matches.filter { it.date.startsWith(selectedDate) && it.id <= 100 }

    Column {
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dates) { date ->
                FilterChip(selected = selectedDate == date, onClick = { selectedDate = date }, label = { Text(date) })
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(filteredMatches) { index, match ->
                MatchCard(match, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange)
                // En la vista por día, como hay pocos partidos, mostramos el sponsor cada 3 partidos
                if ((index + 1) % 3 == 0) {
                    SponsorCard(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun GroupFilteredFixture(
    matches: List<Match>, 
    onScoreChange: (Int, Int?, Int?) -> Unit, 
    onPenaltiesChange: (Int, Int?, Int?) -> Unit,
    onStatusChange: (Int, String) -> Unit,
    onShowVipStats: () -> Unit,
    onPredictionChange: (Int, String?, Int?, Int?) -> Unit
) {
    val groups = matches.filter { it.id <= 100 }.map { it.homeTeam.group }.distinct().sorted()
    var selectedGroup by remember { mutableStateOf(if (groups.isNotEmpty()) groups.first() else "") }
    val filteredMatches = matches.filter { it.homeTeam.group == selectedGroup && it.id <= 100 }

    Column {
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups) { group ->
                FilterChip(selected = selectedGroup == group, onClick = { selectedGroup = group }, label = { Text("GRUPO $group") })
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(filteredMatches) { index, match ->
                MatchCard(match, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange)
                if ((index + 1) % 5 == 0) {
                    SponsorCard(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    match: Match, 
    onScoreChange: (Int, Int?, Int?) -> Unit, 
    onPenaltiesChange: (Int, Int?, Int?) -> Unit,
    onStatusChange: (Int, String) -> Unit,
    onShowVipStats: () -> Unit,
    onPredictionChange: (Int, String?, Int?, Int?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (match.id > 100) "ELIMINACIÓN" else "GRUPO ${match.homeTeam.group}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (match.stadium.isNotEmpty()) {
                        Text(
                            text = "${match.stadium}, ${match.city}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 10.sp
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = match.date.split(" ").last(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    if (match.status == "Finished") {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { 
                                onScoreChange(match.id, null, null)
                                onPredictionChange(match.id, null, null, null)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = "Limpiar", 
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "LIMPIAR", 
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    } else if (match.status == "Scheduled") {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { onStatusChange(match.id, "Finished") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Default.Check, 
                                contentDescription = "Finalizar", 
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "FINALIZAR", 
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TeamMatchInfo(
                    team = match.homeTeam,
                    score = match.homeScore,
                    onScoreChange = { if (match.status != "Finished") onScoreChange(match.id, it, match.awayScore) },
                    enabled = match.status != "Finished"
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    if (match.status == "Finished") {
                        Text(
                            text = "FINALIZADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        )
                    }
                }
                
                TeamMatchInfo(
                    team = match.awayTeam,
                    score = match.awayScore,
                    onScoreChange = { if (match.status != "Finished") onScoreChange(match.id, match.homeScore, it) },
                    enabled = match.status != "Finished"
                )
            }

            if (match.status == "Finished") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onShowVipStats() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VER ESTADÍSTICAS VIP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // SECCIÓN PRODE (Predicción)
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MI PRONÓSTICO (PRODE)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (match.status == "Scheduled") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botones L E V (Solo marcan ganador, no ponen goles)
                        PredictionChip(label = "L", selected = match.predictedWinner == "L") {
                            onPredictionChange(match.id, "L", match.predictedHomeScore, match.predictedAwayScore)
                        }
                        PredictionChip(label = "E", selected = match.predictedWinner == "E") {
                            onPredictionChange(match.id, "E", match.predictedHomeScore, match.predictedAwayScore)
                        }
                        PredictionChip(label = "V", selected = match.predictedWinner == "V") {
                            onPredictionChange(match.id, "V", match.predictedHomeScore, match.predictedAwayScore)
                        }
                        
                        VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 8.dp))
                        
                        PredictionInput(value = match.predictedHomeScore, onValueChange = { onPredictionChange(match.id, match.predictedWinner, it, match.predictedAwayScore) })
                        Text(" - ", fontWeight = FontWeight.Bold)
                        PredictionInput(value = match.predictedAwayScore, onValueChange = { onPredictionChange(match.id, match.predictedWinner, match.predictedHomeScore, it) })
                    }
                } else {
                    // Si ya terminó, mostramos el resultado de la predicción
                    val realWinner = when {
                        match.homeScore!! > match.awayScore!! -> "L"
                        match.homeScore!! < match.awayScore!! -> "V"
                        else -> "E"
                    }
                    
                    val winnerPoints = if (match.predictedWinner == realWinner) 1 else 0
                    val scorePoints = if (match.homeScore == match.predictedHomeScore && match.awayScore == match.predictedAwayScore) 2 else 0
                    val totalPoints = winnerPoints + scorePoints
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Ganador: ${match.predictedWinner ?: "-"} | Marcador: ${match.predictedHomeScore ?: 0}-${match.predictedAwayScore ?: 0}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (totalPoints > 0) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (totalPoints > 0) "+$totalPoints PUNTOS" else "0 PUNTOS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPoints > 0) Color(0xFF2E7D32) else Color.Red
                            )
                        }
                    }
                }
            }
            if (match.id > 100 && match.homeScore != null && match.homeScore == match.awayScore) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("TANDA DE PENALES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PenaltyCounter(match.homePenalties ?: 0) { onPenaltiesChange(match.id, it, match.awayPenalties) }
                    Text("-", fontWeight = FontWeight.Bold)
                    PenaltyCounter(match.awayPenalties ?: 0) { onPenaltiesChange(match.id, match.homePenalties, it) }
                }
            }
        }
    }
}

@Composable
fun PenaltyCounter(score: Int, onScoreChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { if (score > 0) onScoreChange(score - 1) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        Text(text = score.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = { onScoreChange(score + 1) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun TeamMatchInfo(team: Team, score: Int?, onScoreChange: (Int?) -> Unit, enabled: Boolean = true) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(team.flagUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(54.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(team.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if ((score ?: 0) > 0) onScoreChange((score ?: 0) - 1) }, 
                modifier = Modifier.size(24.dp),
                enabled = enabled
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(text = score?.toString() ?: "0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(
                onClick = { onScoreChange((score ?: 0) + 1) }, 
                modifier = Modifier.size(24.dp),
                enabled = enabled
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun PredictionInput(value: Int?, onValueChange: (Int?) -> Unit) {
    Surface(
        modifier = Modifier.size(width = 40.dp, height = 40.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            val textValue = value?.toString() ?: ""
            androidx.compose.foundation.text.BasicTextField(
                value = textValue,
                onValueChange = {
                    if (it.isEmpty()) onValueChange(null)
                    else it.toIntOrNull()?.let { num -> if (num in 0..20) onValueChange(num) }
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PredictionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(32.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
