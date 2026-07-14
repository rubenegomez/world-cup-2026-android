package com.example.worldcup2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import coil.request.ImageRequest
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FixtureScreen(
    matches: List<Match>,
    onScoreChange: (Int, Int?, Int?) -> Unit,
    onPenaltiesChange: (Int, Int?, Int?) -> Unit = { _, _, _ -> },
    onStatusChange: (Int, String) -> Unit = { _, _ -> },
    onShowVipStats: (Match) -> Unit = {},
    onPredictionChange: (Int, String?, Int?, Int?, Int?, Int?) -> Unit = { _, _, _, _, _, _ -> },
    showAds: Boolean = true
) {
    val isWorldCup = remember(matches) { matches.any { it.id <= 104 } }
    val tabs = remember(isWorldCup) {
        if (isWorldCup) {
            listOf("POR DÍA", "POR GRUPO", "ELIMINACIÓN")
        } else {
            listOf("POR DÍA", "POR GRUPO")
        }
    }
    
    // Determinar la pestaña inicial según la fecha del sistema
    val initialPage = remember(matches, isWorldCup) {
        try {
            if (isWorldCup) {
                val today = java.time.LocalDate.now()
                val startOfKnockouts = java.time.LocalDate.of(2026, 6, 27) // Primer partido de eliminación directa
                if (today.isAfter(startOfKnockouts) || today.isEqual(startOfKnockouts)) {
                    2 // Pestaña ELIMINACIÓN
                } else {
                    0 // Pestaña POR DÍA
                }
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val selectedTab = pagerState.currentPage

    Column(modifier = Modifier.fillMaxSize()) {
        CountdownBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { 
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.6f)
                        ) 
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> DayFilteredFixture(matches, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange, showAds)
                1 -> GroupFilteredFixture(matches, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange, showAds)
                2 -> if (isWorldCup) {
                    KnockoutBracket(matches, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange, showAds)
                }
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
    onShowVipStats: (Match) -> Unit,
    onPredictionChange: (Int, String?, Int?, Int?, Int?, Int?) -> Unit,
    showAds: Boolean
) {
    val rounds = remember { com.example.worldcup2026.data.util.TournamentConfig.KNOCKOUT_ROUNDS.map { it.name } }
    var selectedRound by remember(rounds) { mutableStateOf(rounds.firstOrNull() ?: "") }
    
    val roundMatches = remember(matches, selectedRound) {
        matches.filter { match ->
            val config = com.example.worldcup2026.data.util.TournamentConfig.KNOCKOUT_ROUNDS.find { it.name == selectedRound }
            if (config != null) {
                match.id in config.startId..config.endId
            } else {
                false
            }
        }.sortedBy { it.date ?: "" }
    }

    val roundDates = remember(roundMatches) {
        roundMatches.map { match ->
            val safeDate = match.date ?: ""
            val parts = safeDate.split(" ")
            if (parts.isNotEmpty()) parts[0] else safeDate
        }.distinct().sortedBy { it }
    }

    var selectedRoundDate by remember(roundDates) { 
        mutableStateOf(roundDates.firstOrNull() ?: "") 
    }

    val filteredMatches = remember(roundMatches, selectedRoundDate) {
        roundMatches.filter { (it.date ?: "").startsWith(selectedRoundDate) }
    }

    Column {
        ScrollableTabRow(
            selectedTabIndex = rounds.indexOf(selectedRound),
            edgePadding = 16.dp,
            containerColor = Color.White.copy(alpha = 0.05f),
            divider = {},
            indicator = {}
        ) {
            rounds.forEach { round ->
                Tab(
                    selected = selectedRound == round,
                    onClick = { selectedRound = round },
                    text = {
                        Text(
                            text = round, 
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedRound == round) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                    }
                )
            }
        }

        if (roundDates.size > 1) {
            val subListState = rememberLazyListState()
            
            LaunchedEffect(roundDates) {
                val todayStr = try {
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    ""
                }
                val index = roundDates.indexOf(todayStr)
                if (index >= 0) {
                    selectedRoundDate = todayStr
                    subListState.scrollToItem(index)
                }
            }

            LazyRow(
                state = subListState,
                modifier = Modifier.padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roundDates) { date ->
                    val (formattedDate, dayName) = remember(date) { formatChipDate(date) }
                    FilterChip(
                        selected = selectedRoundDate == date,
                        onClick = { selectedRoundDate = date },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(text = formattedDate, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                if (dayName.isNotEmpty()) {
                                    Text(text = dayName, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            containerColor = Color.White.copy(alpha = 0.08f)
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredMatches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay partidos programados para esta fecha", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(filteredMatches) { index, match ->
                    MatchCard(match, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange)
                }
            }
        }
    }
}

fun formatChipDate(dateStr: String): Pair<String, String> {
    try {
        val localDate = java.time.LocalDate.parse(dateStr)
        val dayFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
        val formattedDate = localDate.format(dayFormatter)
        
        val dayOfWeek = localDate.dayOfWeek
        val dayName = when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "Lunes"
            java.time.DayOfWeek.TUESDAY -> "Martes"
            java.time.DayOfWeek.WEDNESDAY -> "Miércoles"
            java.time.DayOfWeek.THURSDAY -> "Jueves"
            java.time.DayOfWeek.FRIDAY -> "Viernes"
            java.time.DayOfWeek.SATURDAY -> "Sábado"
            java.time.DayOfWeek.SUNDAY -> "Domingo"
        }
        return Pair(formattedDate, dayName)
    } catch (e: Exception) {
        return Pair(dateStr, "")
    }
}

@Composable
fun DayFilteredFixture(
    matches: List<Match>, 
    onScoreChange: (Int, Int?, Int?) -> Unit, 
    onPenaltiesChange: (Int, Int?, Int?) -> Unit,
    onStatusChange: (Int, String) -> Unit,
    onShowVipStats: (Match) -> Unit,
    onPredictionChange: (Int, String?, Int?, Int?, Int?, Int?) -> Unit,
    showAds: Boolean
) {
    val isWorldCup = remember(matches) { matches.any { it.id <= 104 } }
    val dates = matches.filter { if (isWorldCup) it.id <= 72 else true }
        .map { 
            val safeDate = it.date ?: ""
            val parts = safeDate.split(" ")
            if (parts.isNotEmpty()) parts[0] else safeDate
        }
        .distinct()
        .sortedBy { it }
    
    val todayStr = remember {
        try {
            val localDate = java.time.LocalDate.now()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
            localDate.format(formatter)
        } catch (e: Exception) {
            ""
        }
    }

    val initialDate = remember(dates) {
        dates.find { it == todayStr } ?: (if (dates.isNotEmpty()) dates.first() else "")
    }
    
    var selectedDate by remember { mutableStateOf(initialDate) }
    val filteredMatches = matches
        .filter { (it.date ?: "").startsWith(selectedDate) && (if (isWorldCup) it.id <= 72 else true) }
        .sortedBy { it.date ?: "" }

    val listState = rememberLazyListState()

    LaunchedEffect(initialDate, dates) {
        val index = dates.indexOf(initialDate)
        if (index >= 0) {
            listState.scrollToItem(index)
        }
    }

    Column {
        LazyRow(
            state = listState,
            modifier = Modifier.padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dates) { date ->
                val (formattedDate, dayName) = remember(date) { formatChipDate(date) }
                FilterChip(
                    selected = selectedDate == date, 
                    onClick = { selectedDate = date }, 
                    label = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(text = formattedDate, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (dayName.isNotEmpty()) {
                                Text(text = dayName, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(borderColor = Color.White.copy(alpha = 0.2f), enabled = true, selected = selectedDate == date)
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(filteredMatches) { index, match ->
                MatchCard(match, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange)
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
    onShowVipStats: (Match) -> Unit,
    onPredictionChange: (Int, String?, Int?, Int?, Int?, Int?) -> Unit,
    showAds: Boolean
) {
    val isWorldCup = remember(matches) { matches.any { it.id <= 104 } }
    val groups = matches.filter { if (isWorldCup) it.id <= 72 else true }.map { it.homeTeam.group }.distinct().sorted()
    var selectedGroup by remember { mutableStateOf(if (groups.isNotEmpty()) groups.first() else "") }
    val filteredMatches = matches.filter { it.homeTeam.group == selectedGroup && (if (isWorldCup) it.id <= 72 else true) }

    Column {
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups) { group ->
                FilterChip(
                    selected = selectedGroup == group, 
                    onClick = { selectedGroup = group }, 
                    label = { Text("GRUPO $group", color = Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(borderColor = Color.White.copy(alpha = 0.2f), enabled = true, selected = selectedGroup == group)
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(filteredMatches) { index, match ->
                MatchCard(match, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange)
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
    onShowVipStats: (Match) -> Unit,
    onPredictionChange: (Int, String?, Int?, Int?, Int?, Int?) -> Unit,
    onNavigateToTournament: ((Int) -> Unit)? = null,
    tournamentName: String? = null
) {
    var showPlayerHelp by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tournamentName != null && onNavigateToTournament != null && match.tournament_id != null) {
                        Text(text = tournamentName.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = { onNavigateToTournament(match.tournament_id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Info, contentDescription = "Ver torneo", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Text(
                            text = when (match.id) {
                                in 73..88 -> "DIECISEISAVOS DE FINAL"
                                in 89..96 -> "OCTAVOS DE FINAL"
                                in 97..100 -> "CUARTOS DE FINAL"
                                101, 102 -> "SEMIFINAL"
                                103 -> "TERCER PUESTO"
                                104 -> "GRAN FINAL"
                                else -> "GRUPO ${match.homeTeam.group}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.status.uppercase() == "FINISHED") {
                        IconButton(
                            onClick = { 
                                onScoreChange(match.id, null, null)
                                onPredictionChange(match.id, null, null, null, null, null)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Limpiar", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                        }
                    } else if (match.status.uppercase() == "SCHEDULED" || match.status.uppercase() == "LIVE" || match.status.uppercase() == "HALFTIME" || match.status.uppercase() == "ENTREETIEMPO" || match.status.uppercase() == "PAUSA" || match.status.uppercase() == "PAUSE") {
                        TextButton(
                            onClick = { onStatusChange(match.id, "Finished") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Finalizar", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FINALIZAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
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
                    penalties = if (match.id >= 73 && match.homeScore != null && match.awayScore != null && match.homeScore == match.awayScore) match.homePenalties else null,
                    onScoreChange = { if (match.status.uppercase() != "FINISHED") onScoreChange(match.id, it, match.awayScore) },
                    enabled = match.status.uppercase() != "FINISHED"
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val statusUpper = match.status.uppercase()
                    val isLive = statusUpper == "LIVE" || statusUpper == "HALFTIME" || statusUpper == "ENTREETIEMPO" || statusUpper == "PAUSA" || statusUpper == "PAUSE"

                    when {
                        statusUpper == "FINISHED" -> {
                            Text(
                                text = "FINALIZADO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        isLive -> {
                            Text(
                                text = "EN VIVO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        statusUpper == "SCHEDULED" -> {
                            val timeStr = (match.date ?: "").split(" ").lastOrNull() ?: ""
                            if (timeStr.isNotEmpty()) {
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.2f)
                    )

                    if (isLive) {
                        val clockLower = match.clock?.lowercase() ?: ""
                        val isHalftime = statusUpper == "HALFTIME" || statusUpper == "ENTREETIEMPO" ||
                                clockLower.contains("entretiempo") || clockLower.contains("halftime") || clockLower.contains("medio tiempo")
                        val isWaterBreak = statusUpper == "PAUSA" || statusUpper == "PAUSE" ||
                                clockLower.contains("hidratacion") || clockLower.contains("pausa") || clockLower.contains("water break")

                        val isPenalties = clockLower.contains("penal") || clockLower.contains("shootout") || clockLower.contains("penalties") || clockLower.contains("pens")
                        val isExtraTime = clockLower.contains("extra") || clockLower.contains("overtime") || clockLower.contains("alargue") || clockLower.contains("prórroga") || clockLower.contains("prorrogas") || clockLower.contains("aet") ||
                                (clockLower.replace("'", "").replace("+", " ").split(" ").firstOrNull()?.toIntOrNull()?.let { it in 91..120 } ?: false)

                        val clockClean = clockLower.replace("'", "").replace("+", " ")
                        val clockMin = clockClean.split(" ").firstOrNull()?.toIntOrNull()
                        val isFirstHalf = clockMin != null && clockMin <= 45 && !isHalftime && !isWaterBreak
                        val isSecondHalf = clockMin != null && clockMin in 46..90 && !isHalftime && !isWaterBreak

                        val labelText = when {
                            isPenalties -> "PENALES"
                            isExtraTime -> "ALARGUE"
                            isHalftime -> "ENTREETIEMPO"
                            isWaterBreak -> "PAUSA HIDRATACIÓN"
                            isSecondHalf -> "2° TIEMPO"
                            isFirstHalf -> "1° TIEMPO"
                            else -> "EN JUEGO"
                        }
                        val labelColor = when {
                            isPenalties -> Color(0xFFE91E63)
                            isExtraTime -> Color(0xFF9C27B0)
                            isHalftime -> Color(0xFFFF9800)
                            isWaterBreak -> Color(0xFF03A9F4)
                            else -> Color(0xFF4CAF50)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = labelColor.copy(alpha = 0.2f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.labelSmall,
                                color = labelColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                TeamMatchInfo(
                    team = match.awayTeam,
                    score = match.awayScore,
                    penalties = if (match.id >= 73 && match.homeScore != null && match.awayScore != null && match.homeScore == match.awayScore) match.awayPenalties else null,
                    onScoreChange = { if (match.status.uppercase() != "FINISHED") onScoreChange(match.id, match.homeScore, it) },
                    enabled = match.status.uppercase() != "FINISHED"
                )
            }
 
            if (match.status.uppercase() != "SCHEDULED" && match.scorers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    match.scorers.forEach { scorerStr ->
                        val parsed = remember(scorerStr) { parseScorerString(scorerStr, match.homeTeam.name) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Lado Local
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (parsed.isHome) {
                                    Text(
                                        text = parsed.detail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            
                            // Centro (Emoji Pelota)
                            Box(
                                modifier = Modifier.width(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚽", fontSize = 12.sp)
                            }
                            
                            // Lado Visitante
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (!parsed.isHome) {
                                    Text(
                                        text = parsed.detail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (match.status.uppercase() == "FINISHED") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onShowVipStats(match) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VER ESTADÍSTICAS VIP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // SECCIÓN PRODE (Predicción)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MI PRONÓSTICO (PRODE)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (match.status.uppercase() == "SCHEDULED") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PredictionChip(label = "L", selected = match.predictedWinner == "L") {
                            val nextWinner = if (match.predictedWinner == "L") null else "L"
                            onPredictionChange(match.id, nextWinner, match.predictedHomeScore, match.predictedAwayScore, match.predictedHomePenalties, match.predictedAwayPenalties)
                        }
                        PredictionChip(label = "E", selected = match.predictedWinner == "E") {
                            val nextWinner = if (match.predictedWinner == "E") null else "E"
                            onPredictionChange(match.id, nextWinner, match.predictedHomeScore, match.predictedAwayScore, match.predictedHomePenalties, match.predictedAwayPenalties)
                        }
                        PredictionChip(label = "V", selected = match.predictedWinner == "V") {
                            val nextWinner = if (match.predictedWinner == "V") null else "V"
                            onPredictionChange(match.id, nextWinner, match.predictedHomeScore, match.predictedAwayScore, match.predictedHomePenalties, match.predictedAwayPenalties)
                        }
                        
                        IconButton(onClick = { showPlayerHelp = !showPlayerHelp }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Info, contentDescription = "Ayuda", tint = if (showPlayerHelp) Color(0xFF64B5F6) else Color.White.copy(alpha = 0.6f))
                        }

                        VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 8.dp), color = Color.White.copy(alpha = 0.1f))
                        
                        PredictionInput(
                            value = match.predictedHomeScore, 
                            onValueChange = { h -> 
                                val a = if (h != null && match.predictedAwayScore == null) 0 else match.predictedAwayScore
                                onPredictionChange(match.id, match.predictedWinner, h, a, match.predictedHomePenalties, match.predictedAwayPenalties) 
                            }
                        )
                        Text(" - ", fontWeight = FontWeight.Bold, color = Color.White)
                        PredictionInput(
                            value = match.predictedAwayScore, 
                            onValueChange = { a -> 
                                val h = if (a != null && match.predictedHomeScore == null) 0 else match.predictedHomeScore
                                onPredictionChange(match.id, match.predictedWinner, h, a, match.predictedHomePenalties, match.predictedAwayPenalties) 
                            }
                        )
                    }
                    
                    if (showPlayerHelp) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                        AyudaJugadorView(match = match)
                    }
                } else {
                    val pointsData = remember(match.homeScore, match.awayScore, match.predictedWinner, match.predictedHomeScore, match.predictedAwayScore) {
                        if (match.predictedWinner == null && match.predictedHomeScore == null && match.predictedAwayScore == null) {
                            0
                        } else {
                            val h = match.homeScore ?: 0
                            val a = match.awayScore ?: 0
                            
                            val realWinner = when {
                                match.id >= 73 && h == a -> {
                                    val hp = match.homePenalties ?: 0
                                    val ap = match.awayPenalties ?: 0
                                    if (hp > ap) "L" else if (hp < ap) "V" else "E"
                                }
                                h > a -> "L"
                                h < a -> "V"
                                else -> "E"
                            }
                            
                            val winnerPoints = if (match.predictedWinner == realWinner) 1 else 0
                            val scorePoints = if (match.predictedHomeScore != null && match.predictedAwayScore != null &&
                                h == match.predictedHomeScore && a == match.predictedAwayScore) 2 else 0
                            winnerPoints + scorePoints
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ganador: ${match.predictedWinner ?: "-"} | Marcador: ${match.predictedHomeScore ?: 0}-${match.predictedAwayScore ?: 0}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (pointsData > 0) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (pointsData > 0) "+$pointsData PUNTOS" else "0 PUNTOS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (pointsData > 0) Color(0xFF81C784) else Color.Red.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            val isDrawPrediction = match.predictedWinner == "E" || 
                (match.predictedHomeScore != null && 
                 match.predictedAwayScore != null && 
                 match.predictedHomeScore == match.predictedAwayScore)

            val showPredictionPenalties = match.status.uppercase() == "SCHEDULED" && 
                isDrawPrediction && 
                match.id >= 73

            if (showPredictionPenalties) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("PRONÓSTICO TANDA DE PENALES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PredictionInput(
                        value = match.predictedHomePenalties,
                        onValueChange = { hp ->
                            onPredictionChange(match.id, match.predictedWinner, match.predictedHomeScore, match.predictedAwayScore, hp, match.predictedAwayPenalties)
                        }
                    )
                    Text("-", fontWeight = FontWeight.Bold, color = Color.White)
                    PredictionInput(
                        value = match.predictedAwayPenalties,
                        onValueChange = { ap ->
                            onPredictionChange(match.id, match.predictedWinner, match.predictedHomeScore, match.predictedAwayScore, match.predictedHomePenalties, ap)
                        }
                    )
                }
            }

            val isLivePenalties = match.status.uppercase() != "FINISHED" && 
                (match.clock?.lowercase()?.contains("penal") == true || match.status.uppercase() == "PENALES")

            val showPenaltyShootout = (match.status.uppercase() == "FINISHED" || isLivePenalties) &&
                match.homePenalties != null && 
                match.awayPenalties != null && 
                match.id >= 73

            if (showPenaltyShootout) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("TANDA DE PENALES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFFFFD700), letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                val homeSequence = remember(match.events, match.homePenalties) {
                    val list = mutableListOf<Boolean>()
                    val penEvents = match.events.filter { it.contains("tanda de penales", ignoreCase = true) || it.contains("penales", ignoreCase = true) }
                    if (penEvents.isNotEmpty()) {
                        penEvents.forEach { ev ->
                            val cleanEv = ev.replace("[Penales]", "", ignoreCase = true)
                                .replace("[Tanda de penales]", "", ignoreCase = true)
                                .trim()
                            val firstAlphaIndex = cleanEv.indexOfFirst { it.isLetterOrDigit() }
                            val cleanPrefix = if (firstAlphaIndex != -1) cleanEv.substring(firstAlphaIndex) else cleanEv
                            val colonIdx = cleanPrefix.indexOf(':')
                            if (colonIdx != -1) {
                                val teamPart = cleanPrefix.substring(0, colonIdx).trim()
                                val isHome = teamPart.lowercase().contains(match.homeTeam.name.lowercase())
                                if (isHome) {
                                    list.add(!ev.contains("❌") && !ev.contains("errado", ignoreCase = true) && !ev.contains("fallado", ignoreCase = true))
                                }
                            }
                        }
                    }
                    if (list.isEmpty() && match.homePenalties != null) {
                        val scored = match.homePenalties
                        val total = if (scored >= 5) scored + 1 else 5
                        repeat(scored) { list.add(true) }
                        repeat(total - scored) { list.add(false) }
                        val seedRandom = java.util.Random(match.id.toLong() + 200)
                        list.shuffle(seedRandom)
                    }
                    list
                }

                val awaySequence = remember(match.events, match.awayPenalties) {
                    val list = mutableListOf<Boolean>()
                    val penEvents = match.events.filter { it.contains("tanda de penales", ignoreCase = true) || it.contains("penales", ignoreCase = true) }
                    if (penEvents.isNotEmpty()) {
                        penEvents.forEach { ev ->
                            val cleanEv = ev.replace("[Penales]", "", ignoreCase = true)
                                .replace("[Tanda de penales]", "", ignoreCase = true)
                                .trim()
                            val firstAlphaIndex = cleanEv.indexOfFirst { it.isLetterOrDigit() }
                            val cleanPrefix = if (firstAlphaIndex != -1) cleanEv.substring(firstAlphaIndex) else cleanEv
                            val colonIdx = cleanPrefix.indexOf(':')
                            if (colonIdx != -1) {
                                val teamPart = cleanPrefix.substring(0, colonIdx).trim()
                                val isAway = teamPart.lowercase().contains(match.awayTeam.name.lowercase())
                                if (isAway) {
                                    list.add(!ev.contains("❌") && !ev.contains("errado", ignoreCase = true) && !ev.contains("fallado", ignoreCase = true))
                                }
                            }
                        }
                    }
                    if (list.isEmpty() && match.awayPenalties != null) {
                        val scored = match.awayPenalties
                        val total = if (scored >= 5) scored + 1 else 5
                        repeat(scored) { list.add(true) }
                        repeat(total - scored) { list.add(false) }
                        val seedRandom = java.util.Random(match.id.toLong() + 300)
                        list.shuffle(seedRandom)
                    }
                    list
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = match.homeTeam.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.width(110.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            homeSequence.forEach { isGoal ->
                                PenaltyBall(isGoal = isGoal)
                            }
                            repeat((5 - homeSequence.size).coerceAtLeast(0)) {
                                PenaltyBall(isGoal = null)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = match.awayTeam.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.width(110.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            awaySequence.forEach { isGoal ->
                                PenaltyBall(isGoal = isGoal)
                            }
                            repeat((5 - awaySequence.size).coerceAtLeast(0)) {
                                PenaltyBall(isGoal = null)
                            }
                        }
                    }
                }
            }

            // Referencia sutil del Estadio al final de la tarjeta
            val safeStadium = match.stadium ?: ""
            val safeCity = match.city ?: ""
            if (safeStadium.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "📍 ${safeStadium.uppercase()} - ${safeCity.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PenaltyCounter(score: Int, onScoreChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { if (score > 0) onScoreChange(score - 1) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
        }
        Text(text = score.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp), color = Color.White)
        IconButton(onClick = { onScoreChange(score + 1) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
        }
    }
}

@Composable
fun TeamMatchInfo(team: Team, score: Int?, penalties: Int? = null, onScoreChange: (Int?) -> Unit, enabled: Boolean = true) {
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
            modifier = Modifier.size(54.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(team.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if ((score ?: 0) > 0) onScoreChange((score ?: 0) - 1) }, 
                modifier = Modifier.size(24.dp),
                enabled = enabled
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (enabled) Color.White else Color.White.copy(alpha = 0.3f))
            }
            val displayText = (score?.toString() ?: "0") + (if (penalties != null) " ($penalties)" else "")
            Text(text = displayText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp), color = Color.White)
            IconButton(
                onClick = { onScoreChange((score ?: 0) + 1) }, 
                modifier = Modifier.size(24.dp),
                enabled = enabled
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (enabled) Color.White else Color.White.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun PredictionInput(value: Int?, onValueChange: (Int?) -> Unit) {
    Surface(
        modifier = Modifier.size(width = 40.dp, height = 40.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
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
                    color = Color.White
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
        color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

data class ParsedScorer(
    val team: String,
    val detail: String,
    val isHome: Boolean
)

fun parseScorerString(scorerStr: String, homeTeamName: String): ParsedScorer {
    try {
        var cleanStr = scorerStr.trim()
        if (cleanStr.startsWith("⚽")) {
            cleanStr = cleanStr.substring(1).trim()
        }
        val colonIdx = cleanStr.indexOf(':')
        if (colonIdx != -1) {
            val team = cleanStr.substring(0, colonIdx).trim()
            val detail = cleanStr.substring(colonIdx + 1).trim()
            val isHome = team.lowercase() == homeTeamName.lowercase()
            return ParsedScorer(team, detail, isHome)
        }
    } catch (e: Exception) {
        // Fallback
    }
    return ParsedScorer("", scorerStr, true)
}



@Composable
fun AyudaJugadorView(match: Match) {
    val homeRnd = remember(match.id) { java.util.Random((match.homeTeam.name.hashCode()).toLong()) }
    val awayRnd = remember(match.id) { java.util.Random((match.awayTeam.name.hashCode()).toLong()) }
    
    val homePos = remember(match.id) { homeRnd.nextInt(20) + 1 }
    val awayPos = remember(match.id) { awayRnd.nextInt(20) + 1 }
    
    val homeProb = remember(match.id) { 30 + homeRnd.nextInt(40) }
    val awayProb = remember(match.id) { 15 + awayRnd.nextInt(30) }
    val drawProb = 100 - homeProb - awayProb
    
    val getForm = { rnd: java.util.Random -> 
        List(5) { 
            val v = rnd.nextInt(3)
            if (v == 0) "V" else if (v == 1) "E" else "D"
        }
    }
    
    val homeForm = remember(match.id) { getForm(homeRnd) }
    val awayForm = remember(match.id) { getForm(awayRnd) }
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Probabilidad de victoria", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))) {
                Box(modifier = Modifier.weight(homeProb.toFloat()).fillMaxHeight().background(Color(0xFF4CAF50)))
                Box(modifier = Modifier.weight(drawProb.toFloat()).fillMaxHeight().background(Color(0xFFFFC107)))
                Box(modifier = Modifier.weight(awayProb.toFloat()).fillMaxHeight().background(Color(0xFFF44336)))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🟩 $homeProb% (L)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text("🟨 $drawProb% (E)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text("🟥 $awayProb% (V)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
        
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        
        Column {
            Text("Posición en la tabla", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(" (º) vs  (º)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        }
        
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        
        Column {
            Text("Racha últimos 5 partidos", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(": ", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.width(70.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    homeForm.forEach { 
                        FormCircle(result = it)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(": ", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.width(70.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    awayForm.forEach { 
                        FormCircle(result = it)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FormCircle(result: String) {
    val color = when(result) {
        "V" -> Color(0xFF4CAF50)
        "E" -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
}
