package com.example.worldcup2026.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.worldcup2026.data.api.NetworkModule
import com.example.worldcup2026.data.api.AnnualStandingDto
import com.example.worldcup2026.data.api.DescensoStandingDto
import com.example.worldcup2026.data.api.GoleadorDto
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import com.example.worldcup2026.data.util.StandingsCalculator
import com.example.worldcup2026.data.util.TeamStats
import kotlinx.coroutines.launch

// Detección del tipo de torneo según rangos de IDs de partidos
enum class TournamentKind { WORLD_CUP, LIBERTADORES, LIGA }

@Composable
fun StandingsScreen(matches: List<Match>) {
    // IDs 1-300 = Mundial | IDs 301-499 = Libertadores | IDs 500+ = Liga
    val tournamentKind = remember(matches) {
        val firstId = matches.minOfOrNull { it.id } ?: 0
        when {
            firstId <= 300 -> TournamentKind.WORLD_CUP
            firstId <= 499 -> TournamentKind.LIBERTADORES
            else -> TournamentKind.LIGA
        }
    }

    val tournamentId = remember(tournamentKind) {
        when (tournamentKind) {
            TournamentKind.WORLD_CUP   -> 1
            TournamentKind.LIBERTADORES -> 3
            TournamentKind.LIGA        -> 5
        }
    }

    val tabs = when (tournamentKind) {
        TournamentKind.WORLD_CUP    -> listOf("GRUPOS", "MEJORES TERCEROS")
        TournamentKind.LIBERTADORES -> listOf("GRUPOS", "GOLEADORES")
        TournamentKind.LIGA         -> listOf("ZONAS", "TABLA ANUAL", "PROMEDIOS", "GOLEADORES")
    }

    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Estados para datos dinámicos del backend
    var annualStandings by remember { mutableStateOf<List<AnnualStandingDto>?>(null) }
    var descensoStandings by remember { mutableStateOf<List<DescensoStandingDto>?>(null) }
    var goleadores by remember { mutableStateOf<List<GoleadorDto>?>(null) }

    var isLoadingAnnual by remember { mutableStateOf(false) }
    var isLoadingDescenso by remember { mutableStateOf(false) }
    var isLoadingGoleadores by remember { mutableStateOf(false) }

    val teamsByGroup = remember(matches) {
        matches.flatMap { listOf(it.homeTeam, it.awayTeam) }
            .distinctBy { it.id }
            .filter { it.id > 0 && it.group.isNotEmpty() && it.group != "Eliminación" && it.group != "TBD" }
            .groupBy { it.group }
            .toSortedMap()
    }

    // Efecto para cargar datos según la pestaña seleccionada
    LaunchedEffect(selectedTab, tournamentKind) {
        when (tournamentKind) {
            TournamentKind.LIGA -> when (selectedTab) {
                1 -> {
                    if (annualStandings == null) {
                        isLoadingAnnual = true
                        try { annualStandings = NetworkModule.apiService.getAnnualStandings(tournamentId) }
                        catch (e: Exception) { e.printStackTrace() }
                        finally { isLoadingAnnual = false }
                    }
                }
                2 -> {
                    if (descensoStandings == null) {
                        isLoadingDescenso = true
                        try { descensoStandings = NetworkModule.apiService.getDescensoStandings(tournamentId) }
                        catch (e: Exception) { e.printStackTrace() }
                        finally { isLoadingDescenso = false }
                    }
                }
                3 -> {
                    if (goleadores == null) {
                        isLoadingGoleadores = true
                        try { goleadores = NetworkModule.apiService.getGoleadores(tournamentId) }
                        catch (e: Exception) { e.printStackTrace() }
                        finally { isLoadingGoleadores = false }
                    }
                }
            }
            TournamentKind.LIBERTADORES -> {
                // pestaña 1 = Goleadores
                if (selectedTab == 1 && goleadores == null) {
                    isLoadingGoleadores = true
                    try { goleadores = NetworkModule.apiService.getGoleadores(tournamentId) }
                    catch (e: Exception) { e.printStackTrace() }
                    finally { isLoadingGoleadores = false }
                }
            }
            else -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            edgePadding = 8.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.6f)
                        ) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (tournamentKind) {
                TournamentKind.WORLD_CUP -> {
                    if (selectedTab == 0) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            teamsByGroup.forEach { (groupName, teams) ->
                                item {
                                    GroupStandingsTable(groupName, teams, matches, isWorldCup = true)
                                }
                            }
                        }
                    } else {
                        val thirdPlaceTeams = remember(teamsByGroup, matches) {
                            teamsByGroup.mapNotNull { (_, teams) ->
                                val standings = StandingsCalculator.calculateStandings(teams, matches, isWorldCup = true)
                                standings.getOrNull(2)
                            }.sortedWith(
                                compareByDescending<TeamStats> { it.pts }
                                    .thenByDescending { it.gd }
                                    .thenByDescending { it.gf }
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            item { BestThirdsTable(thirdPlaceTeams) }
                        }
                    }
                }

                TournamentKind.LIBERTADORES -> {
                    when (selectedTab) {
                        0 -> { // Grupos A-H
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                teamsByGroup.forEach { (groupName, teams) ->
                                    item {
                                        GroupStandingsTable(groupName, teams, matches, isWorldCup = true)
                                    }
                                }
                            }
                        }
                        1 -> { // Goleadores
                            if (isLoadingGoleadores) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(8.dp)
                                ) {
                                    item { GoleadoresTable(goleadores ?: emptyList()) }
                                }
                            }
                        }
                    }
                }

                TournamentKind.LIGA -> {
                    // RENDER LIGA PROFESIONAL
                    when (selectedTab) {
                        0 -> { // Zonas A y B
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            teamsByGroup.forEach { (groupName, teams) ->
                                item {
                                    GroupStandingsTable(groupName, teams, matches, isWorldCup = false)
                                }
                            }
                        }
                    }
                    1 -> { // Tabla Anual
                        if (isLoadingAnnual) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                item {
                                    AnnualStandingsTable(annualStandings ?: emptyList())
                                }
                            }
                        }
                    }
                    2 -> { // Promedios / Descenso
                        if (isLoadingDescenso) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                item {
                                    DescensoStandingsTable(descensoStandings ?: emptyList())
                                }
                            }
                        }
                    }
                    3 -> { // Goleadores
                        if (isLoadingGoleadores) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                item {
                                    GoleadoresTable(goleadores ?: emptyList())
                                }
                            }
                        }
                    }
                } // cierre when(selectedTab)
            } // cierre TournamentKind.LIGA
        } // cierre when(tournamentKind)
    } // cierre Box peso
  } // cierre Column
} // cierre StandingsScreen


@Composable
fun GroupStandingsTable(groupName: String, teams: List<Team>, matches: List<Match>, isWorldCup: Boolean, qualifiedCount: Int = if (isWorldCup) 2 else 4) {
    val standings = StandingsCalculator.calculateStandings(teams, matches, isWorldCup)
    val title = if (isWorldCup) "Grupo $groupName" else "Zona $groupName"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pos", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Equipo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("PJ", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("G", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("E", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("P", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("GF", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("GC", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("DG", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("Pts", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        standings.forEachIndexed { index, stats ->
            val isQualified = index < qualifiedCount
            val rowBgColor = if (isQualified) Color(0xFF4CAF50).copy(alpha = 0.05f) else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBgColor, RoundedCornerShape(4.dp))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}", 
                    modifier = Modifier.width(30.dp), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold,
                    color = if (isQualified) Color(0xFF81C784) else Color.White
                )
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(stats.team.flagUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Text(
                    text = stats.team.name,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isQualified) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text("${stats.pj}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.g}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.e}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.p}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.gf}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.ga}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.gd}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = if (stats.gd > 0) Color(0xFF81C784) else if (stats.gd < 0) Color(0xFFE57373) else Color.White)
                Text("${stats.pts}", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black)
            }
            if (index < standings.size - 1) {
                HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun AnnualStandingsTable(standings: List<AnnualStandingDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Tabla Anual 2026",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )
        Text(
            text = "Clasificación a Copas Libertadores y Sudamericana",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pos", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Equipo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("PJ", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("G", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("E", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("P", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("GF", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("GC", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("DG", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("Pts", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        standings.forEachIndexed { index, stats ->
            // Libertadores (pasan los primeros 3)
            val isLibertadores = index < 3
            // Sudamericana (pasan del 4 al 9)
            val isSudamericana = index in 3..8
            
            val rowBgColor = when {
                isLibertadores -> Color(0xFF2196F3).copy(alpha = 0.06f)
                isSudamericana -> Color(0xFFFF9800).copy(alpha = 0.05f)
                else -> Color.Transparent
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBgColor, RoundedCornerShape(4.dp))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stats.pos}", 
                    modifier = Modifier.width(30.dp), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isLibertadores -> Color(0xFF90CAF9)
                        isSudamericana -> Color(0xFFFFCC80)
                        else -> Color.White
                    }
                )
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(stats.logo_url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Text(
                    text = stats.team_name,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isLibertadores || isSudamericana) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text("${stats.pj}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.g}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.e}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.p}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.gf}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.gc}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.dg}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = if (stats.dg > 0) Color(0xFF81C784) else if (stats.dg < 0) Color(0xFFE57373) else Color.White)
                Text(
                    text = "${stats.pts}", 
                    modifier = Modifier.width(30.dp), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Black,
                    color = when {
                        isLibertadores -> Color(0xFF90CAF9)
                        isSudamericana -> Color(0xFFFFCC80)
                        else -> Color.White
                    }
                )
            }
            if (index < standings.size - 1) {
                HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun DescensoStandingsTable(standings: List<DescensoStandingDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Tabla de Promedios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )
        Text(
            text = "El último puesto desciende directamente a la Primera Nacional",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pos", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Equipo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Hist PTS/PJ", modifier = Modifier.width(70.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Act PTS/PJ", modifier = Modifier.width(70.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Tot PTS/PJ", modifier = Modifier.width(70.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Prom", modifier = Modifier.width(55.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        standings.forEachIndexed { index, stats ->
            // Zona de descenso (el último desciende)
            val isRelegationZone = index == standings.size - 1
            val rowBgColor = if (isRelegationZone) Color(0xFFF44336).copy(alpha = 0.08f) else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBgColor, RoundedCornerShape(4.dp))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stats.pos}", 
                    modifier = Modifier.width(30.dp), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold,
                    color = if (isRelegationZone) Color(0xFFEF9A9A) else Color.White
                )
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(stats.logo_url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Text(
                    text = stats.team_name,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isRelegationZone) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text("${stats.historical_pts}/${stats.historical_pj}", modifier = Modifier.width(70.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                Text("${stats.current_pts}/${stats.current_pj}", modifier = Modifier.width(70.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                Text("${stats.total_pts}/${stats.total_pj}", modifier = Modifier.width(70.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                
                val formattedPromedio = String.format("%.4f", stats.promedio)
                Text(
                    text = formattedPromedio, 
                    modifier = Modifier.width(55.dp), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Black,
                    color = if (isRelegationZone) Color(0xFFEF9A9A) else Color.White
                )
            }
            if (index < standings.size - 1) {
                HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun GoleadoresTable(goleadoresList: List<GoleadorDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Goleadores Primera División",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pos", modifier = Modifier.width(35.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Jugador", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Equipo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Goles", modifier = Modifier.width(50.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        if (goleadoresList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay goles registrados en este torneo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            goleadoresList.forEachIndexed { index, goleador ->
                val isTopThree = index < 3
                val rowBgColor = if (isTopThree) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else Color.Transparent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBgColor, RoundedCornerShape(4.dp))
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${goleador.pos}", 
                        modifier = Modifier.width(35.dp), 
                        textAlign = TextAlign.Center, 
                        style = MaterialTheme.typography.bodySmall, 
                        fontWeight = FontWeight.Bold,
                        color = if (isTopThree) MaterialTheme.colorScheme.primary else Color.White
                    )
                    
                    Text(
                        text = goleador.player_name,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isTopThree) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (goleador.logo_url.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(goleador.logo_url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = goleador.team_name,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Text(
                        text = "${goleador.goals}", 
                        modifier = Modifier.width(50.dp), 
                        textAlign = TextAlign.Center, 
                        style = MaterialTheme.typography.bodyMedium, 
                        fontWeight = FontWeight.Black,
                        color = if (isTopThree) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
                if (index < goleadoresList.size - 1) {
                    HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun BestThirdsTable(thirds: List<TeamStats>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Ranking de Mejores Terceros",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )
        Text(
            text = "Clasifican a 16avos los 8 mejores de los 12 grupos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pos", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Equipo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Grupo", modifier = Modifier.width(45.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("PJ", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("GF", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("GC", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("DG", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("Pts", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        thirds.forEachIndexed { index, stats ->
            val isQualified = index < 8
            val rowBgColor = if (isQualified) Color(0xFF4CAF50).copy(alpha = 0.08f) else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBgColor, RoundedCornerShape(4.dp))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}", 
                    modifier = Modifier.width(30.dp), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold,
                    color = if (isQualified) Color(0xFF81C784) else Color.White
                )
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(stats.team.flagUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Text(
                    text = stats.team.name,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isQualified) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = stats.team.group,
                    modifier = Modifier.width(45.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text("${stats.pj}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.gf}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.ga}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.gd}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${stats.pts}", 
                    modifier = Modifier.width(30.dp), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Black,
                    color = if (isQualified) Color(0xFF81C784) else Color.White
                )
            }
            if (index < thirds.size - 1) {
                HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}
