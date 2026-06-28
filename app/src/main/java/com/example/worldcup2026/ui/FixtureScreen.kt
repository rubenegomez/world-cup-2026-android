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
    onPredictionChange: (Int, String?, Int?, Int?) -> Unit = { _, _, _, _ -> },
    showAds: Boolean = true
) {
    val tabs = listOf("POR DÍA", "POR GRUPO", "ELIMINACIÓN")
    
    // Determinar la pestaña inicial según la fecha del sistema
    val initialPage = remember(matches) {
        try {
            val today = java.time.LocalDate.now()
            val startOfKnockouts = java.time.LocalDate.of(2026, 6, 27) // Primer partido de eliminación directa
            if (today.isAfter(startOfKnockouts) || today.isEqual(startOfKnockouts)) {
                2 // Pestaña ELIMINACIÓN
            } else {
                0 // Pestaña POR DÍA
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
                2 -> KnockoutBracket(matches, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange, showAds)
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
    onPredictionChange: (Int, String?, Int?, Int?) -> Unit,
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
    onPredictionChange: (Int, String?, Int?, Int?) -> Unit,
    showAds: Boolean
) {
    val dates = matches.filter { it.id <= 100 }
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
        .filter { (it.date ?: "").startsWith(selectedDate) && it.id <= 100 }
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
    onPredictionChange: (Int, String?, Int?, Int?) -> Unit,
    showAds: Boolean
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
    onPredictionChange: (Int, String?, Int?, Int?) -> Unit
) {
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
                Column {
                    Text(
                        text = when {
                            match.id == 131 -> "GRAN FINAL"
                            match.id == 132 -> "TERCER PUESTO"
                            match.id > 100 -> "ELIMINACIÓN"
                            else -> "GRUPO ${match.homeTeam.group}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.status.uppercase() == "FINISHED") {
                        IconButton(
                            onClick = { 
                                onScoreChange(match.id, null, null)
                                onPredictionChange(match.id, null, null, null)
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
                    onScoreChange = { if (match.status.uppercase() != "FINISHED") onScoreChange(match.id, it, match.awayScore) },
                    enabled = match.status.uppercase() != "FINISHED"
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.2f)
                    )
                    val statusUpper = match.status.uppercase()
                    when (statusUpper) {
                        "FINISHED" -> {
                            Text(
                                text = "FINALIZADO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        }
                        "LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE" -> {
                            val clockLower = match.clock?.lowercase() ?: ""
                            val isHalftime = statusUpper == "HALFTIME" || statusUpper == "ENTREETIEMPO" ||
                                    clockLower.contains("entretiempo") || clockLower.contains("halftime") || clockLower.contains("medio tiempo")
                            val isWaterBreak = statusUpper == "PAUSA" || statusUpper == "PAUSE" ||
                                    clockLower.contains("hidratacion") || clockLower.contains("pausa") || clockLower.contains("water break")
                            
                            val labelText = when {
                                isHalftime -> "ENTREETIEMPO"
                                isWaterBreak -> "PAUSA HIDRATACIÓN"
                                else -> "EN VIVO"
                            }
                            val labelColor = when {
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
                        "SCHEDULED" -> {
                            val timeStr = (match.date ?: "").split(" ").lastOrNull() ?: ""
                            if (timeStr.isNotEmpty()) {
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
                
                TeamMatchInfo(
                    team = match.awayTeam,
                    score = match.awayScore,
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
                            onPredictionChange(match.id, nextWinner, match.predictedHomeScore, match.predictedAwayScore)
                        }
                        PredictionChip(label = "E", selected = match.predictedWinner == "E") {
                            val nextWinner = if (match.predictedWinner == "E") null else "E"
                            onPredictionChange(match.id, nextWinner, match.predictedHomeScore, match.predictedAwayScore)
                        }
                        PredictionChip(label = "V", selected = match.predictedWinner == "V") {
                            val nextWinner = if (match.predictedWinner == "V") null else "V"
                            onPredictionChange(match.id, nextWinner, match.predictedHomeScore, match.predictedAwayScore)
                        }
                        
                        VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 8.dp), color = Color.White.copy(alpha = 0.1f))
                        
                        PredictionInput(
                            value = match.predictedHomeScore, 
                            onValueChange = { h -> 
                                val a = if (h != null && match.predictedAwayScore == null) 0 else match.predictedAwayScore
                                onPredictionChange(match.id, match.predictedWinner, h, a) 
                            }
                        )
                        Text(" - ", fontWeight = FontWeight.Bold, color = Color.White)
                        PredictionInput(
                            value = match.predictedAwayScore, 
                            onValueChange = { a -> 
                                val h = if (a != null && match.predictedHomeScore == null) 0 else match.predictedHomeScore
                                onPredictionChange(match.id, match.predictedWinner, h, a) 
                            }
                        )
                    }
                } else {
                    val pointsData = remember(match.homeScore, match.awayScore, match.predictedWinner, match.predictedHomeScore, match.predictedAwayScore) {
                        if (match.predictedWinner == null && match.predictedHomeScore == null && match.predictedAwayScore == null) {
                            0
                        } else {
                            val h = match.homeScore ?: 0
                            val a = match.awayScore ?: 0
                            
                            val realWinner = when {
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

            if (match.id > 100 && match.homeScore != null && match.homeScore == match.awayScore) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("TANDA DE PENALES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PenaltyCounter(match.homePenalties ?: 0) { onPenaltiesChange(match.id, it, match.awayPenalties) }
                    Text("-", fontWeight = FontWeight.Bold, color = Color.White)
                    PenaltyCounter(match.awayPenalties ?: 0) { onPenaltiesChange(match.id, match.homePenalties, it) }
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
            Text(text = score?.toString() ?: "0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp), color = Color.White)
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
