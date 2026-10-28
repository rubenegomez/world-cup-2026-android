package com.example.worldcup2026.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate

import coil.compose.AsyncImage
import com.example.worldcup2026.ui.allTournaments
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.MasterTeamCatalog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DailyMatchesScreen(
    date: LocalDate,
    matches: List<Match>,
    viewModel: WorldCupViewModel,
    onNavigateToTournament: (Int) -> Unit,
    onShowVipStats: (Match) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var filterLiveOnly by remember { mutableStateOf(false) }
    val favTournaments by viewModel.favoriteTournamentIds
    val favTeams by viewModel.favoriteTeamNames
    val apiTournamentsList by viewModel.apiTournaments
    val liveTournamentsMap by viewModel.liveTournaments

    var selectedTournamentIds by remember(favTournaments) { 
        mutableStateOf<Set<Int>>(
            if (favTournaments.isNotEmpty()) setOf(favTournaments.first()) else setOf(0)
        ) 
    }

    var showTournamentSelectorDialog by remember { mutableStateOf(false) }
    var editingTournamentId by remember { mutableStateOf<Int?>(null) }
    var customVersionTrigger by remember { mutableStateOf(0) }

    // Modo de visualización: 0 = Calendario Diario, 1 = Fecha Completa del Torneo
    var viewMode by remember { mutableStateOf(0) }
    var selectedMatchday by remember { mutableStateOf<Int?>(null) }

    val singleSelectedTourneyId = if (!filterLiveOnly && selectedTournamentIds.size == 1 && !selectedTournamentIds.contains(0)) {
        selectedTournamentIds.first()
    } else null

    val currentTourneyName = remember(selectedTournamentIds, filterLiveOnly, customVersionTrigger) {
        when {
            filterLiveOnly -> "🔴 Partidos En Vivo"
            singleSelectedTourneyId != null -> MasterTeamCatalog.getTournamentDisplayName(context, singleSelectedTourneyId)
            else -> "🌐 Todos los Torneos"
        }
    }

    val currentTourneyLogo = remember(singleSelectedTourneyId, customVersionTrigger) {
        if (singleSelectedTourneyId != null) {
            MasterTeamCatalog.getTournamentLogo(context, singleSelectedTourneyId)
        } else null
    }

    // Partidos del torneo individual para matchdays
    val tournamentMatches = remember(matches, singleSelectedTourneyId) {
        if (singleSelectedTourneyId != null) {
            val list = matches.filter { (it.tournament_id ?: 1) == singleSelectedTourneyId }
            assignInferredMatchdays(list)
        } else emptyList()
    }

    val availableMatchdays = remember(tournamentMatches) {
        tournamentMatches.mapNotNull { it.matchday ?: if (it.tournament_id == 1 && it.id in 1..72) ((it.id - 1) / 16 + 1) else null }.filter { it > 0 }.distinct().sorted()
    }

    LaunchedEffect(singleSelectedTourneyId, availableMatchdays, date) {
        if (availableMatchdays.isNotEmpty()) {
            val todayStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val matchOnDate = tournamentMatches.firstOrNull { it.date?.startsWith(todayStr) == true && (it.matchday ?: 0) > 0 }
            val defaultMd = matchOnDate?.matchday
                ?: tournamentMatches.filter {
                    val d = it.date ?: ""
                    d.length >= 10 && d.substring(0, 10) >= todayStr
                }.minByOrNull { it.date ?: "9999" }?.matchday
                ?: availableMatchdays.lastOrNull()
                ?: 1
            selectedMatchday = defaultMd
        } else {
            selectedMatchday = null
            viewMode = 0
        }
    }

    // Partidos para la fecha seleccionada en el calendario
    val matchesForSelectedDate = remember(matches, date, filterLiveOnly, selectedTournamentIds, favTournaments, favTeams) {
        val dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        matches
            .filter { match ->
                match.date?.startsWith(dateStr) == true
            }
            .filter { match ->
                val mId = match.tournament_id ?: 1
                if (selectedTournamentIds.contains(0) || selectedTournamentIds.isEmpty()) {
                    true
                } else {
                    selectedTournamentIds.contains(mId)
                }
            }
            .filter { match ->
                val rawStatus = match.status.uppercase()
                val isCancelledOrPostponed = rawStatus.contains("POSTP") || rawStatus.contains("SUSPEND") || rawStatus.contains("CANC")
                val isLiveRaw = rawStatus in listOf("LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE")
                val isTimePassed = try {
                    val rawDate = match.date ?: ""
                    if (rawDate.length >= 16) {
                        val dt = if (rawDate.contains("T")) java.time.LocalDateTime.parse(rawDate.take(19))
                                 else java.time.LocalDateTime.parse(rawDate.take(16), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        val now = java.time.LocalDateTime.now()
                        dt.isBefore(now) && dt.plusMinutes(135).isAfter(now) && rawStatus != "FINISHED" && !isCancelledOrPostponed
                    } else false
                } catch (e: Exception) { false }
                
                val isLive = !isCancelledOrPostponed && (isLiveRaw || isTimePassed || (match.homeScore != null && match.awayScore != null && rawStatus != "FINISHED"))
                if (filterLiveOnly) isLive else true
            }
            .sortedWith(
                compareByDescending<Match> { it.status != "Scheduled" || (it.homeScore != null && it.awayScore != null) }
                    .thenByDescending { (it.homeScore ?: -1) + (it.awayScore ?: -1) }
            )
            .distinctBy { "${normalizeTeamName(it.homeTeam.name).lowercase()}_vs_${normalizeTeamName(it.awayTeam.name).lowercase()}" }
            .sortedWith(
                compareByDescending<Match> { it.homeTeam.name in favTeams || it.awayTeam.name in favTeams }
                    .thenByDescending { it.status.uppercase() in listOf("LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE") || (it.homeScore != null && it.awayScore != null && it.status.uppercase() != "FINISHED") }
                    .thenBy { it.status.uppercase() == "FINISHED" }
                    .thenBy { it.date?.substringAfter(" ", "00:00") ?: "00:00" }
            )
    }

    // Partidos que se van a mostrar en pantalla (Diario vs Fecha Completa)
    val displayedMatches = remember(matchesForSelectedDate, viewMode, singleSelectedTourneyId, selectedMatchday, tournamentMatches, filterLiveOnly, favTeams) {
        if (viewMode == 1 && singleSelectedTourneyId != null && selectedMatchday != null) {
            tournamentMatches
                .filter { it.matchday == selectedMatchday }
                .filter { match ->
                    if (filterLiveOnly) {
                        val rawStatus = match.status.uppercase()
                        val isCancelledOrPostponed = rawStatus.contains("POSTP") || rawStatus.contains("SUSPEND") || rawStatus.contains("CANC")
                        val isLiveRaw = rawStatus in listOf("LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE")
                        !isCancelledOrPostponed && (isLiveRaw || (match.homeScore != null && match.awayScore != null && rawStatus != "FINISHED"))
                    } else true
                }
                .sortedWith(compareBy<Match> { it.date ?: "9999" }.thenBy { it.id })
        } else {
            matchesForSelectedDate
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Selector Superior Unificado de Torneos (En reemplazo de los filtros rápidos anteriores)
        Card(
            onClick = {
                com.example.worldcup2026.data.util.SoundManager.playTic()
                showTournamentSelectorDialog = true
            },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (filterLiveOnly) {
                        Text("🔴", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                    } else if (!currentTourneyLogo.isNullOrBlank()) {
                        AsyncImage(
                            model = currentTourneyLogo,
                            contentDescription = "Símbolo",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    } else if (singleSelectedTourneyId != null) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    } else {
                        Text("🌐", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = currentTourneyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (singleSelectedTourneyId != null) {
                        IconButton(
                            onClick = { editingTournamentId = singleSelectedTourneyId },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar Símbolo",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Cambiar Torneo",
                        tint = Color(0xFFFFD700)
                    )
                }
            }
        }

        // 2. Opciones de Modo: Calendario Diario vs Fecha Completa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = viewMode == 0,
                onClick = {
                    com.example.worldcup2026.data.util.SoundManager.playTic()
                    viewMode = 0
                },
                label = {
                    Text(
                        "📅 Por Día (${date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))})",
                        fontSize = 11.sp,
                        fontWeight = if (viewMode == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (viewMode == 0) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFD700),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(30.dp)
            )

            FilterChip(
                selected = viewMode == 1,
                onClick = {
                    com.example.worldcup2026.data.util.SoundManager.playTic()
                    if (singleSelectedTourneyId == null) {
                        val dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val autoTourney = matches.firstOrNull { it.date?.startsWith(dateStr) == true }?.tournament_id
                            ?: 5
                        selectedTournamentIds = setOf(autoTourney)
                        filterLiveOnly = false
                    }
                    viewMode = 1
                },
                label = {
                    Text(
                        "📋 Fecha Completa",
                        fontSize = 11.sp,
                        fontWeight = if (viewMode == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (viewMode == 1) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFD700),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(30.dp)
            )
        }

        // Barra selectora de Fechas: [<] FECHA X [>]
        if (viewMode == 1 && selectedMatchday != null && availableMatchdays.isNotEmpty()) {
                val curIndex = availableMatchdays.indexOf(selectedMatchday)
                val hasPrev = curIndex > 0
                val hasNext = curIndex >= 0 && curIndex < availableMatchdays.size - 1

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (hasPrev) {
                                com.example.worldcup2026.data.util.SoundManager.playTic()
                                selectedMatchday = availableMatchdays[curIndex - 1]
                            }
                        },
                        enabled = hasPrev,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Fecha Anterior",
                            tint = if (hasPrev) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                    }

                    Text(
                        text = "⚽ FECHA $selectedMatchday DE ${availableMatchdays.maxOrNull() ?: 0} (${displayedMatches.size} partidos)",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color(0xFFFFD700)
                    )

                    IconButton(
                        onClick = {
                            if (hasNext) {
                                com.example.worldcup2026.data.util.SoundManager.playTic()
                                selectedMatchday = availableMatchdays[curIndex + 1]
                            }
                        },
                        enabled = hasNext,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Fecha Siguiente",
                            tint = if (hasNext) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                    }
                }
            }

        // 3. Lista de Partidos (Diaria o Fecha Completa)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (displayedMatches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filterLiveOnly) "No hay partidos en vivo en esta selección." 
                                   else if (viewMode == 1) "No hay partidos programados para la Fecha $selectedMatchday."
                                   else "No hay partidos programados para la fecha seleccionada.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(displayedMatches) { match ->
                    val tName = resolveTournamentName(match, context)

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
                        },
                        favoriteTeamNames = favTeams,
                        onTeamUpdated = {
                            viewModel.syncLiveResults()
                        },
                        onDeleteMatch = {
                            viewModel.syncLiveResults()
                        }
                    )
                }
            }
        }
    }

    // Modal Selector de Torneo (Estilo ProdeMaster)
    if (showTournamentSelectorDialog) {
        Dialog(onDismissRequest = { showTournamentSelectorDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆 Seleccionar Torneo",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showTournamentSelectorDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val gridItems = remember(apiTournamentsList, liveTournamentsMap, customVersionTrigger) {
                        val result = mutableListOf<SelectorTournamentItem>()
                        // 1. Todos
                        result.add(SelectorTournamentItem(id = 0, name = "Todos", isTodos = true))
                        // 2. En Vivo
                        val anyLive = liveTournamentsMap.values.any { it }
                        result.add(SelectorTournamentItem(id = -1, name = "En Vivo", isLive = true, hasLive = anyLive))

                        val registeredIds = mutableSetOf<Int>()
                        for (apiT in apiTournamentsList) {
                            registeredIds.add(apiT.id)
                            val customName = MasterTeamCatalog.getTournamentDisplayName(context, apiT.id)
                            val customLogo = MasterTeamCatalog.getTournamentLogo(context, apiT.id) ?: apiT.logo_url
                            val isActive = MasterTeamCatalog.ACTIVE_TOURNAMENT_IDS.contains(apiT.id)
                            val hasLive = liveTournamentsMap[apiT.id] == true
                            result.add(
                                SelectorTournamentItem(
                                    id = apiT.id,
                                    name = if (customName.isNotBlank()) customName else apiT.name,
                                    logoUrl = customLogo,
                                    hasLive = hasLive,
                                    isActive = isActive
                                )
                            )
                        }

                        for (masterT in MasterTeamCatalog.MASTER_TOURNAMENTS) {
                            if (!registeredIds.contains(masterT.id)) {
                                val customName = MasterTeamCatalog.getTournamentDisplayName(context, masterT.id)
                                val customLogo = MasterTeamCatalog.getTournamentLogo(context, masterT.id) ?: masterT.logoUrl
                                val hasLive = liveTournamentsMap[masterT.id] == true
                                result.add(
                                    SelectorTournamentItem(
                                        id = masterT.id,
                                        name = if (customName.isNotBlank()) customName else masterT.displayName,
                                        logoUrl = customLogo,
                                        hasLive = hasLive,
                                        isActive = masterT.isActive
                                    )
                                )
                            }
                        }

                        val special = result.filter { it.isTodos || it.isLive }
                        val active = result.filter { !it.isTodos && !it.isLive && it.isActive }
                        val inactive = result.filter { !it.isTodos && !it.isLive && !it.isActive }
                        special + active + inactive
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(gridItems, key = { if (it.isTodos) "todos" else if (it.isLive) "live" else "t_${it.id}" }) { item ->
                            val isSelected = when {
                                item.isTodos -> !filterLiveOnly && (selectedTournamentIds.contains(0) || selectedTournamentIds.isEmpty())
                                item.isLive -> filterLiveOnly
                                else -> !filterLiveOnly && selectedTournamentIds.contains(item.id)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) Color(0xFFFFD700).copy(alpha = 0.22f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .combinedClickable(
                                        onClick = {
                                            com.example.worldcup2026.data.util.SoundManager.playTic()
                                            when {
                                                item.isTodos -> {
                                                    filterLiveOnly = false
                                                    selectedTournamentIds = setOf(0)
                                                }
                                                item.isLive -> {
                                                    filterLiveOnly = true
                                                    selectedTournamentIds = setOf(0)
                                                }
                                                else -> {
                                                    filterLiveOnly = false
                                                    selectedTournamentIds = setOf(item.id)
                                                }
                                            }
                                            showTournamentSelectorDialog = false
                                        },
                                        onLongClick = {
                                            if (!item.isTodos && !item.isLive) {
                                                com.example.worldcup2026.data.util.SoundManager.playTic()
                                                showTournamentSelectorDialog = false
                                                editingTournamentId = item.id
                                            }
                                        }
                                    )
                                    .padding(6.dp)
                            ) {
                                if (item.hasLive) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFE53935),
                                        modifier = Modifier
                                            .size(8.dp)
                                            .align(Alignment.TopEnd)
                                    ) {}
                                }

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    when {
                                        item.isTodos -> Text("🌐", fontSize = 28.sp)
                                        item.isLive -> Text("🔴", fontSize = 28.sp)
                                        !item.logoUrl.isNullOrBlank() -> {
                                            AsyncImage(
                                                model = item.logoUrl,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                        }
                                        else -> Text("🏆", fontSize = 28.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val cleanName = item.name.replace(Regex("^[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}]+\\s*"), "").trim()
                                    Text(
                                        text = cleanName,
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "💡 Tip: Mantené presionado un torneo para editar su nombre o símbolo",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }

    // Diálogo de edición de símbolo y nombre del torneo
    if (editingTournamentId != null) {
        val tId = editingTournamentId!!
        AdminEditTournamentDialog(
            tournamentId = tId,
            initialName = MasterTeamCatalog.getTournamentDisplayName(context, tId),
            initialLogoUrl = MasterTeamCatalog.getTournamentLogo(context, tId),
            onDismiss = { editingTournamentId = null },
            onSuccess = { _, _ ->
                editingTournamentId = null
                customVersionTrigger++
                viewModel.loadTournaments()
            }
        )
    }
}

private fun resolveTournamentName(match: Match, context: android.content.Context? = null): String {
    val tId = match.tournament_id
    if (tId != null) {
        val custom = MasterTeamCatalog.getTournamentDisplayName(context, tId)
        if (custom.isNotBlank()) return custom
        val found = allTournaments.find { it.id == tId }?.name
        if (found != null) return found
    }
    val contextText = "${match.homeTeam.group} ${match.stadium} ${match.city}".lowercase()
    return when {
        contextText.contains("primera c") || contextText.contains("metro c") -> "Primera C Metropolitana"
        contextText.contains("promocional") || contextText.contains("amateur") -> "Torneo Promocional Amateur"
        contextText.contains("primera b") -> "Primera B Metropolitana"
        contextText.contains("primera nacional") || contextText.contains("nacional b") -> "Primera Nacional"
        contextText.contains("federal a") -> "Torneo Federal A"
        contextText.contains("regional") || contextText.contains("trfa") -> "Torneo Regional Federal Amateur"
        contextText.contains("libertadores") -> "Copa CONMEBOL Libertadores"
        contextText.contains("sudamericana") -> "Copa CONMEBOL Sudamericana"
        contextText.contains("intercontinental") -> "Copa Intercontinental"
        contextText.contains("mundial de clubes") -> "Mundial de Clubes"
        contextText.contains("argentina") -> "Copa Argentina"
        else -> "Competición Oficial"
    }
}



private data class SelectorTournamentItem(
    val id: Int,
    val name: String,
    val logoUrl: String? = null,
    val isTodos: Boolean = false,
    val isLive: Boolean = false,
    val hasLive: Boolean = false,
    val isActive: Boolean = false
)
