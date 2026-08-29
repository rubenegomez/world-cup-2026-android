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
enum class TournamentFormat {
    WORLD_CUP,
    ELIMINATORIAS_CONMEBOL,
    LIBERTADORES,
    SUDAMERICANA,
    LIGA_PROFESIONAL,
    PRIMERA_NACIONAL,
    PRIMERA_B,
    PRIMERA_C,
    COPA_DIRECTA,
    COPA_INTERCONTINENTAL
}

// Compatibilidad con código previo
typealias TournamentKind = TournamentFormat

val LIGA_ZONAS_MAP = mapOf(
    // ZONA A (15 equipos)
    "Gimnasia y Esgrima (Mendoza)" to "Zona A",
    "Gimnasia (Mendoza)" to "Zona A",
    "Newell's Old Boys" to "Zona A",
    "Vélez Sarsfield" to "Zona A",
    "Lanús" to "Zona A",
    "Unión (Santa Fe)" to "Zona A",
    "Platense" to "Zona A",
    "Defensa y Justicia" to "Zona A",
    "Boca Juniors" to "Zona A",
    "Deportivo Riestra" to "Zona A",
    "Estudiantes de La Plata" to "Zona A",
    "Independiente" to "Zona A",
    "Central Córdoba (Santiago del Estero)" to "Zona A",
    "Instituto (Córdoba)" to "Zona A",
    "Talleres (Córdoba)" to "Zona A",
    "San Lorenzo" to "Zona A",

    // ZONA B (15 equipos)
    "Argentinos Juniors" to "Zona B",
    "Belgrano (Córdoba)" to "Zona B",
    "Racing Club" to "Zona B",
    "Huracán" to "Zona B",
    "Godoy Cruz Antonio Tomba" to "Zona B",
    "Godoy Cruz" to "Zona B",
    "Barracas Central" to "Zona B",
    "Aldosivi" to "Zona B",
    "Atlético Tucumán" to "Zona B",
    "Independiente Rivadavia" to "Zona B",
    "Sarmiento (Junín)" to "Zona B",
    "Rosario Central" to "Zona B",
    "Gimnasia La Plata" to "Zona B",
    "River Plate" to "Zona B",
    "Tigre" to "Zona B",
    "Banfield" to "Zona B"
)

fun getLigaZona(name: String): String? {
    val norm = normalizeTeamName(name).lowercase()
    for ((key, zone) in LIGA_ZONAS_MAP) {
        val normKey = normalizeTeamName(key).lowercase()
        if (norm == normKey || norm.contains(normKey) || normKey.contains(norm)) {
            return zone
        }
    }
    return LIGA_ZONAS_MAP[name]
}

@Composable
fun StandingsScreen(matches: List<Match>) {
    val tournamentId = remember(matches) {
        matches.firstOrNull { it.tournament_id != null }?.tournament_id ?: run {
            val firstId = matches.minOfOrNull { it.id } ?: 0
            when {
                firstId <= 300 -> 1
                firstId <= 499 -> 3
                else -> 5
            }
        }
    }

    val tournamentFormat = remember(tournamentId) {
        when (tournamentId) {
            1 -> TournamentFormat.WORLD_CUP
            2 -> TournamentFormat.ELIMINATORIAS_CONMEBOL
            3 -> TournamentFormat.LIBERTADORES
            4 -> TournamentFormat.SUDAMERICANA
            5 -> TournamentFormat.LIGA_PROFESIONAL
            6, 7 -> TournamentFormat.COPA_DIRECTA
            8 -> TournamentFormat.PRIMERA_NACIONAL
            9 -> TournamentFormat.PRIMERA_B
            10, 11 -> TournamentFormat.PRIMERA_C
            12 -> TournamentFormat.WORLD_CUP
            13 -> TournamentFormat.COPA_INTERCONTINENTAL
            else -> TournamentFormat.LIGA_PROFESIONAL
        }
    }

    val tabs = when (tournamentFormat) {
        TournamentFormat.WORLD_CUP              -> listOf("GRUPOS", "MEJORES TERCEROS", "GOLEADORES")
        TournamentFormat.ELIMINATORIAS_CONMEBOL -> listOf("TABLA ÚNICA", "GOLEADORES")
        TournamentFormat.LIBERTADORES           -> listOf("GRUPOS", "GOLEADORES")
        TournamentFormat.SUDAMERICANA           -> listOf("GRUPOS", "GOLEADORES")
        TournamentFormat.LIGA_PROFESIONAL       -> listOf("ZONAS", "TABLA ANUAL", "PROMEDIOS", "GOLEADORES")
        TournamentFormat.PRIMERA_NACIONAL       -> listOf("ZONA A", "ZONA B", "REDUCIDO", "GOLEADORES")
        TournamentFormat.PRIMERA_B              -> listOf("TABLA GENERAL", "REDUCIDO", "COPA ARGENTINA", "GOLEADORES")
        TournamentFormat.PRIMERA_C              -> listOf("ZONA A", "ZONA B", "FINAL Y REDUCIDO", "GOLEADORES")
        TournamentFormat.COPA_DIRECTA,
        TournamentFormat.COPA_INTERCONTINENTAL  -> listOf("LLAVE ELIMINATORIA", "GOLEADORES")
    }

    var selectedTab by remember { mutableStateOf(0) }

    // Estados para datos dinámicos del backend
    var annualStandings by remember { mutableStateOf<List<AnnualStandingDto>?>(null) }
    var descensoStandings by remember { mutableStateOf<List<DescensoStandingDto>?>(null) }
    var goleadores by remember { mutableStateOf<List<GoleadorDto>?>(null) }

    var isLoadingAnnual by remember { mutableStateOf(false) }
    var isLoadingDescenso by remember { mutableStateOf(false) }
    var isLoadingGoleadores by remember { mutableStateOf(false) }

    val teamsByGroup = remember(matches, tournamentFormat) {
        val rawTeams = matches.flatMap { listOfNotNull(it.homeTeam, it.awayTeam) }
            .distinctBy { it.name.trim().lowercase() }
        
        if (tournamentFormat == TournamentFormat.LIGA_PROFESIONAL) {
            val nameAliasMap = mapOf(
                "Gimnasia (Mendoza)" to "Gimnasia y Esgrima (Mendoza)",
                "Estudiantes de Río Cuarto" to "Godoy Cruz Antonio Tomba",
                "Godoy Cruz" to "Godoy Cruz Antonio Tomba"
            )

            val mappedTeams = rawTeams.mapNotNull { team ->
                val cleanName = team.name.trim()
                val canonicalName = nameAliasMap[cleanName] ?: cleanName
                val group = getLigaZona(canonicalName) ?: getLigaZona(cleanName)
                if (group != null) {
                    team.copy(name = canonicalName, group = group, players = team.players ?: emptyList())
                } else {
                    null
                }
            }.distinctBy { normalizeTeamName(it.name).lowercase() }

            mappedTeams
                .groupBy { it.group }
                .toSortedMap()
        } else {
            rawTeams
                .filter { it.id > 0 && it.group.isNotEmpty() && it.group != "Eliminación" && it.group != "TBD" }
                .groupBy { it.group }
                .toSortedMap()
        }
    }

    val currentTabTitle = tabs.getOrNull(selectedTab) ?: ""

    // Efecto para cargar datos según la pestaña seleccionada
    LaunchedEffect(currentTabTitle, tournamentId) {
        when (currentTabTitle) {
            "TABLA ANUAL" -> {
                isLoadingAnnual = true
                try { annualStandings = NetworkModule.apiService.getAnnualStandings(tournamentId) }
                catch (e: Exception) { annualStandings = emptyList(); e.printStackTrace() }
                finally { isLoadingAnnual = false }
            }
            "PROMEDIOS" -> {
                if (descensoStandings == null) {
                    isLoadingDescenso = true
                    try { descensoStandings = NetworkModule.apiService.getDescensoStandings(tournamentId) }
                    catch (e: Exception) { descensoStandings = emptyList(); e.printStackTrace() }
                    finally { isLoadingDescenso = false }
                }
            }
            "GOLEADORES" -> {
                if (goleadores == null) {
                    isLoadingGoleadores = true
                    try { goleadores = NetworkModule.apiService.getGoleadores(tournamentId) }
                    catch (e: Exception) { goleadores = emptyList(); e.printStackTrace() }
                    finally { isLoadingGoleadores = false }
                }
            }
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
            when (currentTabTitle) {
                "GRUPOS", "ZONAS", "ZONA A", "ZONA B", "TABLA GENERAL", "TABLA ÚNICA" -> {
                    val filterGroup = when (currentTabTitle) {
                        "ZONA A" -> "Zona A"
                        "ZONA B" -> "Zona B"
                        else -> null
                    }
                    val displayGroups = if (filterGroup != null) {
                        teamsByGroup.filterKeys { it.equals(filterGroup, ignoreCase = true) }
                    } else {
                        teamsByGroup
                    }

                    val filteredMatches = remember(matches, tournamentFormat, tournamentId) {
                        if (tournamentFormat == TournamentFormat.LIGA_PROFESIONAL) {
                            matches.filter { match ->
                                (match.tournament_id == null || match.tournament_id == 5) &&
                                isClausuraMatch(match.date)
                            }
                        } else {
                            matches.filter { match -> match.tournament_id == null || match.tournament_id == tournamentId }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        displayGroups.forEach { (groupName, teams) ->
                            item {
                                GroupStandingsTable(groupName, teams, filteredMatches, tournamentFormat = tournamentFormat)
                            }
                        }
                    }
                }
                "MEJORES TERCEROS" -> {
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
                "TABLA ANUAL" -> {
                    if (isLoadingAnnual) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            item { AnnualStandingsTable(annualStandings ?: emptyList(), descensoStandings ?: emptyList()) }
                        }
                    }
                }
                "PROMEDIOS" -> {
                    if (isLoadingDescenso) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            item { DescensoStandingsTable(descensoStandings ?: emptyList()) }
                        }
                    }
                }
                "GOLEADORES" -> {
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
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Etapa de $currentTabTitle",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Se habilitará automáticamente al finalizar la fase regular.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getQualificationColor(format: TournamentFormat, position: Int, totalTeams: Int): Color {
    return when (format) {
        TournamentFormat.LIBERTADORES -> when (position) {
            1, 2 -> Color(0xFF4CAF50) // Octavos Libertadores
            3    -> Color(0xFFFFB300) // 16vos Sudamericana
            else -> Color.Transparent
        }
        TournamentFormat.SUDAMERICANA -> when (position) {
            1    -> Color(0xFF4CAF50) // Octavos Sudamericana
            2    -> Color(0xFFFFB300) // Playoff Sudamericana
            else -> Color.Transparent
        }
        TournamentFormat.ELIMINATORIAS_CONMEBOL -> when (position) {
            in 1..6 -> Color(0xFF4CAF50) // Clasificación Directa
            7       -> Color(0xFFFFB300) // Repechaje
            else    -> Color.Transparent
        }
        TournamentFormat.PRIMERA_NACIONAL -> when (position) {
            1       -> Color(0xFF4CAF50) // Final 1º Ascenso
            in 2..8 -> Color(0xFF2196F3) // Reducido 2º Ascenso
            else    -> Color.Transparent
        }
        TournamentFormat.PRIMERA_B -> when (position) {
            1       -> Color(0xFF4CAF50) // Campeón Ascenso Directo
            in 2..9 -> Color(0xFF2196F3) // Reducido
            totalTeams - 1, totalTeams -> Color(0xFFF44336) // Descenso
            else    -> Color.Transparent
        }
        TournamentFormat.PRIMERA_C -> when (position) {
            1       -> Color(0xFF4CAF50) // Final 1º Ascenso
            in 2..7 -> Color(0xFF2196F3) // Reducido 2º Ascenso
            totalTeams -> Color(0xFFF44336) // Desafiliación / Descenso
            else    -> Color.Transparent
        }
        TournamentFormat.WORLD_CUP -> when (position) {
            1, 2 -> Color(0xFF4CAF50)
            3    -> Color(0xFFFFB300)
            else -> Color.Transparent
        }
        TournamentFormat.LIGA_PROFESIONAL -> when (position) {
            in 1..4 -> Color(0xFF4CAF50)
            else    -> Color.Transparent
        }
        else -> Color.Transparent
    }
}

@Composable
fun GroupStandingsTable(
    groupName: String, 
    teams: List<Team>, 
    matches: List<Match>, 
    tournamentFormat: TournamentFormat = TournamentFormat.WORLD_CUP
) {
    val isWorldCup = tournamentFormat == TournamentFormat.WORLD_CUP
    val standings = StandingsCalculator.calculateStandings(teams, matches, isWorldCup)
    val title = if (groupName.startsWith("Zona") || groupName.startsWith("Tabla")) groupName else "Grupo $groupName"

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
            val pos = index + 1
            val totalTeams = standings.size
            val rowBgColor = getQualificationColor(tournamentFormat, pos, totalTeams)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (rowBgColor != Color.Transparent) rowBgColor.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(4.dp))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$pos", 
                    modifier = Modifier.width(30.dp), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold,
                    color = if (rowBgColor != Color.Transparent) rowBgColor else Color.White
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
                    fontWeight = if (rowBgColor != Color.Transparent) FontWeight.Bold else FontWeight.Normal,
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
fun AnnualStandingsTable(standings: List<AnnualStandingDto>, descensoStandings: List<DescensoStandingDto> = emptyList()) {
    if (standings.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No hay datos de la Tabla Anual disponibles aún.", color = Color.Gray, fontSize = 13.sp)
        }
        return
    }
    val worstDescensoTeamId = descensoStandings.minByOrNull { it.promedio }?.team_id
    val worstAnnualTeamId = standings.lastOrNull()?.team_id
    
    // Si el 30º de la Anual es también el peor en Promedios, el descenso por Tabla Anual pasa al 29º
    val descensoIndex = if (worstAnnualTeamId != null && worstAnnualTeamId == worstDescensoTeamId && standings.size >= 2) {
        standings.size - 2
    } else {
        standings.size - 1
    }

    // Corrimiento automático de cupos por Campeón (ej: Belgrano Campeón Apertura)
    val teamQualifications = remember(standings) {
        val map = mutableMapOf<Int, String>()
        var libGruposAssigned = 0
        var libPreviaAssigned = 0
        var sudamAssigned = 0

        standings.forEach { dto ->
            val isAperturaChampion = dto.team_name.contains("Belgrano", ignoreCase = true)
            if (isAperturaChampion) {
                map[dto.team_id] = "CAMPEON_LIBERTADORES"
            } else if (libGruposAssigned < 2) {
                map[dto.team_id] = "LIBERTADORES_GRUPOS"
                libGruposAssigned++
            } else if (libPreviaAssigned < 1) {
                map[dto.team_id] = "LIBERTADORES_PREVIA"
                libPreviaAssigned++
            } else if (sudamAssigned < 6) {
                map[dto.team_id] = "SUDAMERICANA"
                sudamAssigned++
            } else {
                map[dto.team_id] = "NINGUNO"
            }
        }
        map
    }

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
            text = "Acumulado Apertura + Clausura (Clasificación a Copas y Descenso)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        // Leyenda explicativa al inicio para máxima visibilidad
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFD54F), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("🏆 Campeón (Libertadores Directo - Libera Cupo)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF81C784), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copa Libertadores (Fase de Grupos)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF64B5F6), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copa Libertadores (Fase Previa)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFB74D), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copa Sudamericana (Grupos - Incluye Corrimiento)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFE57373), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Descenso (30º puesto / Corrimiento)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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

        standings.forEachIndexed { index, dto ->
            val pos = index + 1
            val qual = teamQualifications[dto.team_id] ?: "NINGUNO"
            val isDescenso = index == descensoIndex

            val isCampeon = qual == "CAMPEON_LIBERTADORES"
            val isLibertadoresGrupos = qual == "LIBERTADORES_GRUPOS"
            val isLibertadoresPrevia = qual == "LIBERTADORES_PREVIA"
            val isSudamericana = qual == "SUDAMERICANA"
            
            val rowBgColor = when {
                isCampeon -> Color(0xFFFFD54F).copy(alpha = 0.12f)
                isLibertadoresGrupos -> Color(0xFF4CAF50).copy(alpha = 0.08f)
                isLibertadoresPrevia -> Color(0xFF2196F3).copy(alpha = 0.08f)
                isSudamericana -> Color(0xFFFF9800).copy(alpha = 0.08f)
                isDescenso -> Color(0xFFF44336).copy(alpha = 0.08f)
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
                    text = "$pos", 
                    modifier = Modifier.width(30.dp), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCampeon -> Color(0xFFFFD54F)
                        isLibertadoresGrupos -> Color(0xFF81C784)
                        isLibertadoresPrevia -> Color(0xFF64B5F6)
                        isSudamericana -> Color(0xFFFFB74D)
                        isDescenso -> Color(0xFFE57373)
                        else -> Color.White
                    }
                )
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(dto.logo_url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Text(
                    text = dto.team_name + if (isCampeon) " 🏆" else "",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isCampeon || isLibertadoresGrupos || isLibertadoresPrevia || isSudamericana || isDescenso) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text("${dto.pj}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${dto.g}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${dto.e}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${dto.p}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${dto.gf}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${dto.gc}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${dto.dg}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = if (dto.dg > 0) Color(0xFF81C784) else if (dto.dg < 0) Color(0xFFE57373) else Color.White)
                Text("${dto.pts}", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black)
            }
            if (index < standings.size - 1) {
                HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun DescensoStandingsTable(standings: List<DescensoStandingDto>) {
    if (standings.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No hay datos de Promedios disponibles aún.", color = Color.Gray, fontSize = 13.sp)
        }
        return
    }
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
    if (goleadoresList.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No hay registros de goleadores aún.", color = Color.Gray, fontSize = 13.sp)
        }
        return
    }
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

private fun isClausuraMatch(dateStr: String?): Boolean {
    if (dateStr.isNullOrBlank()) return true
    val isApertura = dateStr.contains("-01-") || dateStr.contains("-02-") || dateStr.contains("-03-") ||
                     dateStr.contains("-04-") || dateStr.contains("-05-") || dateStr.contains("-06-") ||
                     dateStr.contains("/01/") || dateStr.contains("/02/") || dateStr.contains("/03/") ||
                     dateStr.contains("/04/") || dateStr.contains("/05/") || dateStr.contains("/06/") ||
                     dateStr.startsWith("2026-01") || dateStr.startsWith("2026-02") || dateStr.startsWith("2026-03") ||
                     dateStr.startsWith("2026-04") || dateStr.startsWith("2026-05") || dateStr.startsWith("2026-06")
    return !isApertura
}
