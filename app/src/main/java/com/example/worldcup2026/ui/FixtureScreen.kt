package com.example.worldcup2026.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

fun isKnockoutMatch(match: Match): Boolean {
    val tId = match.tournament_id ?: 1
    val groupName = match.homeTeam.group
    return (tId == 1 && match.id in 73..104) || (tId == 3 && groupName.equals("Eliminación", ignoreCase = true))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FixtureScreen(
    matches: List<Match>,
    onScoreChange: (Int, Int?, Int?) -> Unit,
    onPenaltiesChange: (Int, Int?, Int?) -> Unit = { _, _, _ -> },
    onStatusChange: (Int, String) -> Unit = { _, _ -> },
    onShowVipStats: (Match) -> Unit = {},
    onPredictionChange: (Int, String?, Int?, Int?, Int?, Int?) -> Unit = { _, _, _, _, _, _ -> },
    showAds: Boolean = true,
    onToggleComodin: ((Int) -> Unit)? = null
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
                        com.example.worldcup2026.data.util.SoundManager.playTic()
                        coroutineScope.launch { pagerState.animateScrollToPage(index) } 
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
                0 -> DayFilteredFixture(matches, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange, showAds, onToggleComodin)
                1 -> GroupFilteredFixture(matches, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange, showAds, onToggleComodin)
                2 -> if (isWorldCup) {
                    KnockoutBracket(matches, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange, showAds, onToggleComodin)
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
    showAds: Boolean,
    onToggleComodin: ((Int) -> Unit)? = null
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
                    MatchCard(match, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange, allMatches = matches, onToggleComodin = onToggleComodin)
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
    showAds: Boolean,
    onToggleComodin: ((Int) -> Unit)? = null
) {
    val isWorldCup = remember(matches) { matches.any { it.id <= 104 } }
    val matchdays = remember(matches) {
        matches.mapNotNull { it.matchday }.filter { it > 0 }.distinct().sorted()
    }
    
    val defaultMatchday = remember(matchdays, matches) {
        val active = matches.firstOrNull { 
            val st = it.status.uppercase()
            (st == "LIVE" || st == "SCHEDULED" || st == "HALFTIME") && it.matchday != null && it.matchday > 0 
        }?.matchday
        active ?: matchdays.firstOrNull()
    }

    var selectedMatchday by remember(matchdays) { mutableStateOf<Int?>(if (matchdays.size > 1) defaultMatchday else null) }

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
    var searchQuery by remember { mutableStateOf("") }
    var filterLiveOnly by remember { mutableStateOf(false) }

    val filteredMatches = matches
        .filter { match ->
            if (selectedMatchday != null) {
                match.matchday == selectedMatchday
            } else {
                (match.date ?: "").startsWith(selectedDate) && (if (isWorldCup) match.id <= 72 else true)
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
        .sortedWith(
            compareByDescending<Match> { it.status != "Scheduled" }
                .thenByDescending { (it.homeScore ?: -1) + (it.awayScore ?: -1) }
        )
        .distinctBy { "${it.homeTeam.name.lowercase().trim()}_vs_${it.awayTeam.name.lowercase().trim()}" }
        .sortedBy { it.date ?: "" }

    val listState = rememberLazyListState()

    LaunchedEffect(initialDate, dates) {
        val index = dates.indexOf(initialDate)
        if (index >= 0) {
            listState.scrollToItem(index)
        }
    }

    Column {
        // Barra de Búsqueda y Filtro En Vivo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
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

        // Carrusel de Fechas / Jornadas si el torneo tiene fechas numeradas
        if (matchdays.size > 1) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = selectedMatchday == null,
                        onClick = { 
                            com.example.worldcup2026.data.util.SoundManager.playTic()
                            selectedMatchday = null 
                        },
                        label = { Text("Por Día", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedMatchday == null) Color.White else Color.White.copy(alpha = 0.7f)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            containerColor = Color(0xFF1E2536)
                        ),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                items(matchdays) { mDay ->
                    val isSelected = selectedMatchday == mDay
                    val hasLive = matches.any { it.matchday == mDay && it.status.uppercase() in listOf("LIVE", "HALFTIME", "PAUSA") }
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            com.example.worldcup2026.data.util.SoundManager.playTic()
                            selectedMatchday = mDay 
                        },
                        label = { 
                            Text(
                                text = "Fecha $mDay" + (if (hasLive) " 🔴" else ""), 
                                fontSize = 11.sp, 
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color(0xFF10141E) else Color.White
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD700),
                            containerColor = Color(0xFF1E2536)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp, 
                            if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Si se eligió la vista Por Día (o si no hay matchdays)
        if (selectedMatchday == null) {
            LazyRow(
                state = listState,
                modifier = Modifier.padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dates) { date ->
                    val (formattedDate, dayName) = remember(date) { formatChipDate(date) }
                    FilterChip(
                        selected = selectedDate == date, 
                        onClick = { 
                            com.example.worldcup2026.data.util.SoundManager.playTic()
                            selectedDate = date 
                        }, 
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
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(filteredMatches) { index, match ->
                MatchCard(match, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange, allMatches = matches, onToggleComodin = onToggleComodin)
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
    showAds: Boolean,
    onToggleComodin: ((Int) -> Unit)? = null
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
            items(filteredMatches) { match ->
                MatchCard(match, onScoreChange, onPenaltiesChange, onStatusChange, onShowVipStats, onPredictionChange, allMatches = matches, onToggleComodin = onToggleComodin)
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
    tournamentName: String? = null,
    allMatches: List<Match> = emptyList(),
    onToggleComodin: ((Int) -> Unit)? = null
) {
    var showPlayerHelp by remember { mutableStateOf(false) }
    var isEditingProde by remember { mutableStateOf(false) }

    val statusUpper = match.status.uppercase()
    val isLive = statusUpper in listOf("LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE")
    
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141924).copy(alpha = 0.95f)),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isLive) 1.5.dp else 1.dp,
            color = if (isLive) Color.Red.copy(alpha = pulseAlpha) else Color.White.copy(alpha = 0.12f)
        )
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
                    val matchdayLabel = if (match.matchday != null && match.matchday > 0) " · FECHA ${match.matchday}" else ""
                    if (tournamentName != null && onNavigateToTournament != null && match.tournament_id != null) {
                        Text(text = "${tournamentName.uppercase()}$matchdayLabel", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = { onNavigateToTournament(match.tournament_id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Info, contentDescription = "Ver torneo", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        val roundOrGroup = when (match.id) {
                            in 73..88 -> "DIECISEISAVOS DE FINAL"
                            in 89..96 -> "OCTAVOS DE FINAL"
                            in 97..100 -> "CUARTOS DE FINAL"
                            101, 102 -> "SEMIFINAL"
                            103 -> "TERCER PUESTO"
                            104 -> "GRAN FINAL"
                            else -> if (match.homeTeam.group.isNotBlank()) "GRUPO ${match.homeTeam.group}" else "PARTIDO"
                        }
                        Text(
                            text = "$roundOrGroup$matchdayLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isComodin = match.is_featured
                    val canToggleComodin = statusUpper == "SCHEDULED" && onToggleComodin != null

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isComodin) Color(0xFFFFD700) else Color.White.copy(alpha = 0.06f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            if (isComodin) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.clickable(enabled = canToggleComodin) {
                            com.example.worldcup2026.data.util.SoundManager.playTic()
                            onToggleComodin?.invoke(match.id)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star, 
                                contentDescription = "Comodín", 
                                tint = if (isComodin) Color.Black else Color(0xFFFFD700),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isComodin) "COMODÍN x2" else "x2", 
                                color = if (isComodin) Color.Black else Color(0xFFFFD700), 
                                fontWeight = FontWeight.Black, 
                                fontSize = 10.sp
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
                    penalties = if (match.id >= 73 && match.homeScore != null && match.awayScore != null && match.homeScore == match.awayScore) match.homePenalties else null
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val isLiveLocal = statusUpper == "LIVE" || statusUpper == "HALFTIME" || statusUpper == "ENTREETIEMPO" || statusUpper == "PAUSA" || statusUpper == "PAUSE"

                    when {
                        statusUpper.contains("POSTP") || statusUpper.contains("SUSPEND") || statusUpper.contains("CANCEL") -> {
                            Text(
                                text = "POSTERGADO / SUSPENDIDO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
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
                        isLiveLocal -> {
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
                            val rawDate = match.date ?: ""
                            val timePart = when {
                                rawDate.contains("T") -> rawDate.substringAfter("T").take(5)
                                rawDate.contains(" ") -> rawDate.substringAfter(" ").take(5)
                                else -> ""
                            }
                            val formattedTime = if (timePart.contains(":") && timePart.length == 5) "$timePart hs" else timePart
                            if (formattedTime.isNotEmpty()) {
                                Text(
                                    text = formattedTime,
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

                    if (isLiveLocal) {
                        val clockLower = match.clock?.lowercase() ?: ""
                        val isHalftime = statusUpper == "HALFTIME" || statusUpper == "ENTREETIEMPO" ||
                                clockLower.contains("entretiempo") || clockLower.contains("halftime") || clockLower.contains("medio tiempo")
                        val isWaterBreak = statusUpper == "PAUSA" || statusUpper == "PAUSE" ||
                                clockLower.contains("hidratacion") || clockLower.contains("pausa") || clockLower.contains("water break")

                        val isPenalties = clockLower.contains("penal") || clockLower.contains("shootout") || clockLower.contains("penalties") || clockLower.contains("pens")
                        val isExtraTime = clockLower.contains("extra") || clockLower.contains("overtime") || clockLower.contains("alargue") || clockLower.contains("prórroga") || clockLower.contains("prorrogas") || clockLower.contains("aet") ||
                                (clockLower.replace("'", "").replace("+", " ").split(" ").firstOrNull()?.toIntOrNull()?.let { it in 91..120 } ?: false)

                        val clockClean = clockLower.replace("'", "").replace("+", " ").replace(":", " ")
                        val clockMin = clockClean.split(" ").firstOrNull()?.toIntOrNull()
                        val isFirstHalf = (clockMin != null && clockMin <= 45 && !isHalftime && !isWaterBreak) || clockLower.contains("1°") || clockLower.contains("1er") || clockLower.contains("primer") || clockLower.contains("1T")
                        val isSecondHalf = (clockMin != null && clockMin in 46..90 && !isHalftime && !isWaterBreak) || clockLower.contains("2°") || clockLower.contains("2do") || clockLower.contains("segundo") || clockLower.contains("2T")

                        val labelText = when {
                            isPenalties -> "PENALES"
                            isExtraTime -> "ALARGUE"
                            isHalftime -> "ENTREETIEMPO"
                            isWaterBreak -> "PAUSA HIDRATACIÓN"
                            isSecondHalf -> "2º TIEMPO"
                            isFirstHalf -> "1º TIEMPO"
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
                    penalties = if (isKnockoutMatch(match) && match.homeScore != null && match.awayScore != null && match.homeScore == match.awayScore) match.awayPenalties else null
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

            // SECCIÓN PRODE (Predicción)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D121B))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                val matchHasStarted = match.status.uppercase() != "SCHEDULED"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MI PRONÓSTICO (PRODE)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
                    }

                    if (!matchHasStarted) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isDoubleBet(match.predictedWinner, match.predictedHomeScore, match.predictedAwayScore)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        "APUESTA DOBLE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD700),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!isEditingProde) {
                                Button(
                                    onClick = { 
                                        com.example.worldcup2026.data.util.SoundManager.playTic()
                                        isEditingProde = true 
                                    },
                                    modifier = Modifier.height(28.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                                ) {
                                    Text("EDITAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { 
                                        com.example.worldcup2026.data.util.SoundManager.playTic()
                                        isEditingProde = false
                                        onPredictionChange(
                                            match.id,
                                            match.predictedWinner,
                                            match.predictedHomeScore,
                                            match.predictedAwayScore,
                                            match.predictedHomePenalties,
                                            match.predictedAwayPenalties
                                        )
                                    },
                                    modifier = Modifier.height(28.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("GUARDAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (!matchHasStarted) {
                    val currentPred = match.predictedWinner ?: ""
                    val isLSelected = currentPred.split(",").contains("L")
                    val isESelected = currentPred.split(",").contains("E")
                    val isVSelected = currentPred.split(",").contains("V")

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Fila 1: Pronóstico 1X2 y Ayuda
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PredictionChip(label = "L", selected = isLSelected, enabled = isEditingProde) {
                                    val nextWinner = toggleWinnerChip(match.predictedWinner, "L")
                                    onPredictionChange(match.id, nextWinner, match.predictedHomeScore, match.predictedAwayScore, match.predictedHomePenalties, match.predictedAwayPenalties)
                                }
                                PredictionChip(label = "E", selected = isESelected, enabled = isEditingProde) {
                                    val nextWinner = toggleWinnerChip(match.predictedWinner, "E")
                                    onPredictionChange(match.id, nextWinner, match.predictedHomeScore, match.predictedAwayScore, match.predictedHomePenalties, match.predictedAwayPenalties)
                                }
                                PredictionChip(label = "V", selected = isVSelected, enabled = isEditingProde) {
                                    val nextWinner = toggleWinnerChip(match.predictedWinner, "V")
                                    onPredictionChange(match.id, nextWinner, match.predictedHomeScore, match.predictedAwayScore, match.predictedHomePenalties, match.predictedAwayPenalties)
                                }
                            }
                            
                            IconButton(onClick = { showPlayerHelp = !showPlayerHelp }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Info, contentDescription = "Ayuda", tint = if (showPlayerHelp) Color(0xFF64B5F6) else Color.White.copy(alpha = 0.6f))
                            }
                        }

                        // Fila 2: Selector de Goles (+ / -)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Goles:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            PredictionInput(
                                value = match.predictedHomeScore, 
                                enabled = isEditingProde,
                                onValueChange = { h -> 
                                    val a = if (h != null && match.predictedAwayScore == null) 0 else match.predictedAwayScore
                                    onPredictionChange(match.id, match.predictedWinner, h, a, match.predictedHomePenalties, match.predictedAwayPenalties) 
                                }
                            )
                            Text("   -   ", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                            PredictionInput(
                                value = match.predictedAwayScore, 
                                enabled = isEditingProde,
                                onValueChange = { a -> 
                                    val h = if (a != null && match.predictedHomeScore == null) 0 else match.predictedHomeScore
                                    onPredictionChange(match.id, match.predictedWinner, h, a, match.predictedHomePenalties, match.predictedAwayPenalties) 
                                }
                            )
                        }
                    }
                    
                    if (showPlayerHelp) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                        AyudaJugadorView(match = match, allMatches = allMatches)
                    }
                } else {
                    val pointsData = remember(match.homeScore, match.awayScore, match.predictedWinner, match.predictedHomeScore, match.predictedAwayScore, match.homePenalties, match.awayPenalties) {
                        if (match.predictedWinner == null && match.predictedHomeScore == null && match.predictedAwayScore == null) {
                            0
                        } else {
                            val h = match.homeScore ?: 0
                            val a = match.awayScore ?: 0
                            
                            val realWinner = when {
                                isKnockoutMatch(match) && h == a -> {
                                    val hp = match.homePenalties ?: 0
                                    val ap = match.awayPenalties ?: 0
                                    if (hp > ap) "L" else if (hp < ap) "V" else "E"
                                }
                                h > a -> "L"
                                h < a -> "V"
                                else -> "E"
                            }
                            
                            val isDouble = (match.predictedWinner ?: "").split(",").filter { it.isNotBlank() }.size >= 2
                            val predictedSigns = (match.predictedWinner ?: "").split(",").map { s ->
                                when (s.trim()) {
                                    "home" -> "L"
                                    "away" -> "V"
                                    "draw" -> "E"
                                    else -> s.trim()
                                }
                            }.filter { it.isNotBlank() }.toSet()

                            val isWinnerHit = predictedSigns.contains(realWinner)
                            val signPoints = if (isWinnerHit) {
                                if (isDouble) 1 else 2
                            } else 0

                            val scorePoints = if (match.predictedHomeScore != null && match.predictedAwayScore != null &&
                                h == match.predictedHomeScore && a == match.predictedAwayScore) 3 else 0

                            val diffPoints = if (scorePoints == 0 && isWinnerHit && match.predictedHomeScore != null && match.predictedAwayScore != null) {
                                val diffReal = h - a
                                val diffPred = match.predictedHomeScore!! - match.predictedAwayScore!!
                                if (diffReal == diffPred) 2 else 0
                            } else 0

                            val rawTotal = signPoints + scorePoints + diffPoints
                            if (match.is_featured) rawTotal * 2 else rawTotal
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (pointsData > 0) Color(0xFF4CAF50).copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displayWinner = when (match.predictedWinner) {
                                "home" -> "L"
                                "away" -> "V"
                                "draw" -> "E"
                                else -> match.predictedWinner ?: "-"
                            }
                            Text(
                                text = "Tu Pronóstico: $displayWinner (${match.predictedHomeScore ?: 0}-${match.predictedAwayScore ?: 0})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (pointsData > 0) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = if (isLive) "⚡ EN VIVO: +$pointsData PTS" else if (pointsData > 0) "🏆 ¡+$pointsData PTS SUMADOS!" else "❌ 0 PTS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
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
                        enabled = isEditingProde,
                        onValueChange = { hp ->
                            onPredictionChange(match.id, match.predictedWinner, match.predictedHomeScore, match.predictedAwayScore, hp, match.predictedAwayPenalties)
                        }
                    )
                    Text("-", fontWeight = FontWeight.Bold, color = Color.White)
                    PredictionInput(
                        value = match.predictedAwayPenalties,
                        enabled = isEditingProde,
                        onValueChange = { ap ->
                            onPredictionChange(match.id, match.predictedWinner, match.predictedHomeScore, match.predictedAwayScore, match.predictedHomePenalties, ap)
                        }
                    )
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
fun TeamMatchInfo(team: Team, score: Int?, penalties: Int? = null) {
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
        val displayText = (score?.toString() ?: "0") + (if (penalties != null) " ($penalties)" else "")
        Text(text = displayText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp), color = Color.White)
    }
}

@Composable
fun PredictionInput(value: Int?, enabled: Boolean = true, onValueChange: (Int?) -> Unit) {
    val current = value ?: 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(
                color = Color(0xFF1E2536),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (value != null) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (enabled && value != null) 0.08f else 0.02f))
                .clickable(enabled = enabled && value != null) {
                    com.example.worldcup2026.data.util.SoundManager.playTic()
                    if (current > 0) {
                        onValueChange(current - 1)
                    } else {
                        onValueChange(null)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Menos",
                tint = if (enabled && value != null) Color.White else Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(min = 28.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value?.toString() ?: "-",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = if (value != null) Color(0xFFFFD700) else Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (enabled) 0.08f else 0.02f))
                .clickable(enabled = enabled && (value == null || current < 20)) {
                    com.example.worldcup2026.data.util.SoundManager.playTic()
                    if (value == null) {
                        onValueChange(1)
                    } else if (current < 20) {
                        onValueChange(current + 1)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Más",
                tint = if (enabled && (value == null || current < 20)) Color(0xFFFFD700) else Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun PredictionChip(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .clickable(enabled = enabled) { 
                com.example.worldcup2026.data.util.SoundManager.playTic()
                onClick() 
            },
        shape = CircleShape,
        color = if (selected) Color(0xFFFFD700) else Color(0xFF222938),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (selected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f)
        ),
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = if (selected) Color(0xFF10141E) else (if (enabled) Color.White else Color.White.copy(alpha = 0.4f))
            )
        }
    }
}

fun isDoubleBet(predictedWinner: String?, homeScore: Int?, awayScore: Int?): Boolean {
    if (predictedWinner.isNullOrBlank()) {
        return false
    }
    val signs = predictedWinner.split(",").map { s ->
        when (s.trim()) {
            "home" -> "L"
            "away" -> "V"
            "draw" -> "E"
            else -> s.trim()
        }
    }.filter { it.isNotBlank() }
    
    // Apuesta Doble: Exclusivamente cuando se seleccionan 2 fichas (ej: L+V, L+E, E+V)
    return signs.size >= 2
}

fun toggleWinnerChip(currentWinner: String?, clickedOption: String): String? {
    if (currentWinner == null || currentWinner.isBlank()) return clickedOption
    val currentList = currentWinner.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
    if (currentList.contains(clickedOption)) {
        currentList.remove(clickedOption)
    } else {
        if (currentList.size < 2) {
            currentList.add(clickedOption)
        } else {
            currentList.removeAt(0)
            currentList.add(clickedOption)
        }
    }
    return if (currentList.isEmpty()) null else currentList.sorted().joinToString(",")
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
fun AyudaJugadorView(match: Match, allMatches: List<Match> = emptyList()) {
    val homeForm = remember(match.id, allMatches.size) { 
        computeRealTeamForm(match.homeTeam.name, allMatches)
    }
    val awayForm = remember(match.id, allMatches.size) { 
        computeRealTeamForm(match.awayTeam.name, allMatches)
    }

    val homeFormPts = homeForm.fold(0) { acc, res -> acc + (if (res == "V") 3 else if (res == "E") 1 else 0) }
    val awayFormPts = awayForm.fold(0) { acc, res -> acc + (if (res == "V") 3 else if (res == "E") 1 else 0) }
    val drawWeight = 4

    val totalPts = (homeFormPts + awayFormPts + drawWeight).coerceAtLeast(1).toFloat()
    val homeProb = (((homeFormPts + 2) / totalPts) * 100).toInt().coerceIn(10, 80)
    val awayProb = ((awayFormPts / totalPts) * 100).toInt().coerceIn(10, 80)
    val drawProb = (100 - homeProb - awayProb).coerceAtLeast(10)
    
    val h2hSummary = remember(match.id, allMatches.size) {
        computeRealH2H(match.homeTeam.name, match.awayTeam.name, allMatches)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text("Reglas del Prode y Puntos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            
            Text("• Apuesta de Signo (L, E, V):", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
            Text("   - Apuesta Simple: +2 PUNTOS al acertar (ej: L, E o V)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
            Text("   - Apuesta Doble: +1 PUNTO al acertar (ej: L+E, E+V, L+V)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
            
            Spacer(modifier = Modifier.height(4.dp))
            Text("• Goles y Diferencia de Gol:", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
            Text("   - Marcador Exacto: +3 PUNTOS al acertar los goles exactos (ej: 0-2)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
            Text("   - Misma Diferencia de Gol: +2 PUNTOS al acertar ganador/empate si coincide la diferencia de goles pero no el marcador exacto (ej: pusiste 0-2 y terminó 1-3)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
            
            Spacer(modifier = Modifier.height(4.dp))
            Text("• Comodín de la Fecha (⭐ x2):", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            Text("   - ¡DUPLICA todo el puntaje obtenido en ese partido (hasta 10 pts)!", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
            Text("   - Se activa tocando la estrella ⭐ en la esquina superior del partido.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))

            Spacer(modifier = Modifier.height(4.dp))
            Text("• Totales Máximos Posibles por Partido:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            Text("   - Simple (+2) + Misma Diferencia (+2): 4 PUNTOS (x2 Comodín = 8 Pts)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
            Text("   - Simple (+2) + Marcador Exacto (+3): 5 PUNTOS (x2 Comodín = 10 Pts)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
        }
        
        Column {
            Text("Probabilidad estimada", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))) {
                Box(modifier = Modifier.weight(homeProb.toFloat()).fillMaxHeight().background(Color(0xFF4CAF50)))
                Box(modifier = Modifier.weight(drawProb.toFloat()).fillMaxHeight().background(Color(0xFFFFC107)))
                Box(modifier = Modifier.weight(awayProb.toFloat()).fillMaxHeight().background(Color(0xFFF44336)))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🟩 $homeProb% (${match.homeTeam.name})", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text("🟨 $drawProb% (Empate)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text("🟥 $awayProb% (${match.awayTeam.name})", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
        
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        
        Column {
            Text("Racha últimos partidos (Reales)", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏠 ${match.homeTeam.name}: ", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.width(110.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (homeForm.isEmpty()) {
                        Text("Sin datos previos", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        homeForm.forEach { 
                            FormCircle(result = it)
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✈️ ${match.awayTeam.name}: ", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.width(110.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (awayForm.isEmpty()) {
                        Text("Sin datos previos", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        awayForm.forEach { 
                            FormCircle(result = it)
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        Column {
            Text("Historial Directo (H2H entre sí)", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(h2hSummary, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

fun computeRealTeamForm(teamName: String, allMatches: List<Match>): List<String> {
    if (teamName.isBlank() || allMatches.isEmpty()) return emptyList()
    val finishedMatches = allMatches.filter { 
        it.status == "Finished" && 
        (it.homeTeam.name.equals(teamName, ignoreCase = true) || it.awayTeam.name.equals(teamName, ignoreCase = true))
    }.sortedByDescending { it.date ?: "" }.take(5)

    return finishedMatches.map { match ->
        val isHome = match.homeTeam.name.equals(teamName, ignoreCase = true)
        val hScore = match.homeScore ?: 0
        val aScore = match.awayScore ?: 0
        when {
            hScore == aScore -> "E"
            (isHome && hScore > aScore) || (!isHome && aScore > hScore) -> "V"
            else -> "D"
        }
    }
}

fun computeRealH2H(homeName: String, awayName: String, allMatches: List<Match>): String {
    if (homeName.isBlank() || awayName.isBlank() || allMatches.isEmpty()) return "Sin enfrentamientos previos registrados."
    val h2h = allMatches.filter { 
        it.status == "Finished" && 
        ((it.homeTeam.name.equals(homeName, ignoreCase = true) && it.awayTeam.name.equals(awayName, ignoreCase = true)) ||
         (it.homeTeam.name.equals(awayName, ignoreCase = true) && it.awayTeam.name.equals(homeName, ignoreCase = true)))
    }
    if (h2h.isEmpty()) return "Sin enfrentamientos previos registrados."

    var homeWins = 0
    var draws = 0
    var awayWins = 0

    h2h.forEach { m ->
        val h = m.homeScore ?: 0
        val a = m.awayScore ?: 0
        val homeIsTarget = m.homeTeam.name.equals(homeName, ignoreCase = true)
        when {
            h == a -> draws++
            (homeIsTarget && h > a) || (!homeIsTarget && a > h) -> homeWins++
            else -> awayWins++
        }
    }

    return "Últimos ${h2h.size} cruces: $homeWins victorias $homeName, $draws empates, $awayWins victorias $awayName"
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
