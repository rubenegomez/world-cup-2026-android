package com.example.worldcup2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@Composable
fun FixtureScreen(
    matches: List<Match>,
    onScoreChange: (Int, Int?, Int?) -> Unit,
    onPenaltiesChange: (Int, Int?, Int?) -> Unit = { _, _, _ -> }
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
                0 -> DayFilteredFixture(matches, onScoreChange, onPenaltiesChange)
                1 -> GroupFilteredFixture(matches, onScoreChange, onPenaltiesChange)
                2 -> KnockoutBracket(matches, onScoreChange, onPenaltiesChange)
            }
        }
    }
}

@Composable
fun KnockoutBracket(
    matches: List<Match>,
    onScoreChange: (Int, Int?, Int?) -> Unit,
    onPenaltiesChange: (Int, Int?, Int?) -> Unit
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
            items(filteredMatches) { match ->
                MatchCard(match, onScoreChange, onPenaltiesChange)
            }
        }
    }
}

@Composable
fun DayFilteredFixture(matches: List<Match>, onScoreChange: (Int, Int?, Int?) -> Unit, onPenaltiesChange: (Int, Int?, Int?) -> Unit) {
    val dates = matches.filter { it.id <= 100 }.map { it.date.split(" ")[0] }.distinct().sorted()
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
            items(filteredMatches) { match ->
                MatchCard(match, onScoreChange, onPenaltiesChange)
            }
        }
    }
}

@Composable
fun GroupFilteredFixture(matches: List<Match>, onScoreChange: (Int, Int?, Int?) -> Unit, onPenaltiesChange: (Int, Int?, Int?) -> Unit) {
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
            items(filteredMatches) { match ->
                MatchCard(match, onScoreChange, onPenaltiesChange)
            }
        }
    }
}

@Composable
fun MatchCard(match: Match, onScoreChange: (Int, Int?, Int?) -> Unit, onPenaltiesChange: (Int, Int?, Int?) -> Unit) {
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
                Text(
                    text = if (match.id > 100) "ELIMINACIÓN" else "GRUPO ${match.homeTeam.group}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = match.date.split(" ").last(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    if (match.homeScore != null || match.awayScore != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { onScoreChange(match.id, null, null) },
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
                    onScoreChange = { onScoreChange(match.id, it, match.awayScore) }
                )
                
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                
                TeamMatchInfo(
                    team = match.awayTeam,
                    score = match.awayScore,
                    onScoreChange = { onScoreChange(match.id, match.homeScore, it) }
                )
            }

            // Sección de Penales (Solo si es eliminación y hay empate)
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
fun TeamMatchInfo(team: Team, score: Int?, onScoreChange: (Int?) -> Unit) {
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
            IconButton(onClick = { if ((score ?: 0) > 0) onScoreChange((score ?: 0) - 1) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(text = score?.toString() ?: "0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { onScoreChange((score ?: 0) + 1) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}
