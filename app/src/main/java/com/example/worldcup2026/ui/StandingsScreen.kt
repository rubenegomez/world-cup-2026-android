package com.example.worldcup2026.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
    TORNEO_FEDERAL_A,
    COPA_DIRECTA,
    COPA_INTERCONTINENTAL
}

// Compatibilidad con código previo
typealias TournamentKind = TournamentFormat

val PN_ZONA_A_TEAMS = listOf(
    "Acassuso",
    "All Boys",
    "Almirante Brown",
    "Central Norte (Salta)",
    "Chaco For Ever",
    "Ciudad de Bolívar",
    "Colón (Santa Fe)",
    "Defensores de Belgrano",
    "Deportivo Madryn",
    "Deportivo Morón",
    "Estudiantes (Buenos Aires)",
    "Ferro Carril Oeste",
    "Godoy Cruz Antonio Tomba",
    "Los Andes",
    "Mitre (Santiago del Estero)",
    "Racing (Córdoba)",
    "San Miguel",
    "San Telmo"
)

val PN_ZONA_B_TEAMS = listOf(
    "Agropecuario",
    "Almagro",
    "Atlanta",
    "Atlético de Rafaela",
    "Chacarita Juniors",
    "Colegiales",
    "Deportivo Maipú",
    "Gimnasia y Esgrima (Jujuy)",
    "Gimnasia y Tiro (Salta)",
    "Güemes (Santiago del Estero)",
    "Midland",
    "Nueva Chicago",
    "Patronato",
    "Quilmes",
    "San Martín (San Juan)",
    "San Martín (Tucumán)",
    "Temperley",
    "Tristán Suárez"
)

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
    "Estudiantes de Río Cuarto" to "Zona B",
    "Estudiantes (Río Cuarto)" to "Zona B",
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

fun getCanonicalLigaTeam(name: String): Pair<String, String>? {
    val norm = normalizeTeamName(name).lowercase().trim()
    return when {
        // ZONA B
        norm.contains("independiente rivadavia") || (norm.contains("independiente") && norm.contains("rivadavia")) || norm.contains("rivadavia") ->
            "Independiente Rivadavia" to "Zona B"
        norm.contains("argentinos") ->
            "Argentinos Juniors" to "Zona B"
        norm.contains("belgrano") ->
            "Belgrano (Córdoba)" to "Zona B"
        norm.contains("racing") && (norm.contains("club") || !norm.contains("cordoba")) ->
            "Racing Club" to "Zona B"
        norm.contains("huracan") ->
            "Huracán" to "Zona B"
        norm.contains("rio cuarto") || norm.contains("río cuarto") || norm.contains("cuarto") ->
            "Estudiantes de Río Cuarto" to "Zona B"
        norm.contains("barracas") ->
            "Barracas Central" to "Zona B"
        norm.contains("aldosivi") ->
            "Aldosivi" to "Zona B"
        norm.contains("atletico tucuman") || (norm.contains("tucuman") && norm.contains("atletico")) ->
            "Atlético Tucumán" to "Zona B"
        norm.contains("sarmiento") ->
            "Sarmiento (Junín)" to "Zona B"
        norm.contains("rosario central") || norm == "rosario" ->
            "Rosario Central" to "Zona B"
        norm.contains("gimnasia") && (norm.contains("plata") || norm.contains("lp") || norm.contains("gelp") || !norm.contains("mendoza")) ->
            "Gimnasia La Plata" to "Zona B"
        norm.contains("river") ->
            "River Plate" to "Zona B"
        norm.contains("tigre") ->
            "Tigre" to "Zona B"
        norm.contains("banfield") ->
            "Banfield" to "Zona B"

        // ZONA A
        norm.contains("mendoza") && (norm.contains("gimnasia") || norm.contains("esgrima")) ->
            "Gimnasia y Esgrima (Mendoza)" to "Zona A"
        norm.contains("newell") ->
            "Newell's Old Boys" to "Zona A"
        norm.contains("velez") ->
            "Vélez Sarsfield" to "Zona A"
        norm.contains("lanus") ->
            "Lanús" to "Zona A"
        (norm.contains("union") && norm.contains("santa fe")) || norm == "union" ->
            "Unión (Santa Fe)" to "Zona A"
        norm.contains("platense") ->
            "Platense" to "Zona A"
        norm.contains("defensa") ->
            "Defensa y Justicia" to "Zona A"
        norm.contains("boca") ->
            "Boca Juniors" to "Zona A"
        norm.contains("riestra") ->
            "Deportivo Riestra" to "Zona A"
        norm.contains("estudiantes") && !norm.contains("cuarto") && !norm.contains("caseros") && !norm.contains("buenos aires") ->
            "Estudiantes de La Plata" to "Zona A"
        norm.contains("independiente") ->
            "Independiente" to "Zona A"
        norm.contains("central cordoba") || norm.contains("santiago del estero") ->
            "Central Córdoba (Santiago del Estero)" to "Zona A"
        norm.contains("instituto") ->
            "Instituto (Córdoba)" to "Zona A"
        norm.contains("talleres") ->
            "Talleres (Córdoba)" to "Zona A"
        norm.contains("san lorenzo") ->
            "San Lorenzo" to "Zona A"

        else -> null
    }
}

fun getLigaZona(name: String): String? = getCanonicalLigaTeam(name)?.second ?: LIGA_ZONAS_MAP[name]

@Composable
fun StandingsScreen(
    matches: List<Match>,
    initialTournamentId: Int? = null,
    onNavigateToMatch: (Match) -> Unit = {}
) {
    var tournamentId by remember(initialTournamentId) {
        mutableStateOf(initialTournamentId ?: 5)
    }

    val tournamentFormat = remember(tournamentId) {
        when (tournamentId) {
            1 -> TournamentFormat.WORLD_CUP
            2 -> TournamentFormat.ELIMINATORIAS_CONMEBOL
            3 -> TournamentFormat.LIBERTADORES
            4 -> TournamentFormat.SUDAMERICANA
            5 -> TournamentFormat.LIGA_PROFESIONAL
            6 -> TournamentFormat.COPA_DIRECTA
            7 -> TournamentFormat.PRIMERA_NACIONAL
            8 -> TournamentFormat.PRIMERA_B
            9, 10 -> TournamentFormat.PRIMERA_C
            15 -> TournamentFormat.TORNEO_FEDERAL_A
            12 -> TournamentFormat.WORLD_CUP
            13, 18 -> TournamentFormat.COPA_INTERCONTINENTAL
            else -> TournamentFormat.LIGA_PROFESIONAL
        }
    }

    val tabs = when (tournamentFormat) {
        TournamentFormat.WORLD_CUP              -> listOf("GRUPOS", "MEJORES TERCEROS", "GOLEADORES", "FIXTURE")
        TournamentFormat.ELIMINATORIAS_CONMEBOL -> listOf("TABLA ÚNICA", "GOLEADORES", "FIXTURE")
        TournamentFormat.LIBERTADORES           -> listOf("GRUPOS", "GOLEADORES", "FIXTURE")
        TournamentFormat.SUDAMERICANA           -> listOf("GRUPOS", "GOLEADORES", "FIXTURE")
        TournamentFormat.LIGA_PROFESIONAL       -> listOf("ZONAS", "TABLA ANUAL", "PROMEDIOS", "GOLEADORES", "FIXTURE")
        TournamentFormat.PRIMERA_NACIONAL       -> listOf("ZONA A", "ZONA B", "TABLA GENERAL", "GOLEADORES", "FIXTURE")
        TournamentFormat.TORNEO_FEDERAL_A       -> listOf("TABLA GENERAL", "GOLEADORES", "FIXTURE")
        TournamentFormat.PRIMERA_B              -> listOf("TABLA GENERAL", "REDUCIDO", "GOLEADORES", "FIXTURE")
        TournamentFormat.PRIMERA_C              -> listOf("ZONA A", "ZONA B", "GOLEADORES", "FIXTURE")
        TournamentFormat.COPA_DIRECTA,
        TournamentFormat.COPA_INTERCONTINENTAL  -> listOf("LLAVE ELIMINATORIA", "GOLEADORES", "FIXTURE")
    }

    var selectedTab by remember { mutableStateOf(0) }

    // Estados para datos dinámicos del backend
    var annualStandings by remember { mutableStateOf<List<AnnualStandingDto>?>(null) }
    var descensoStandings by remember { mutableStateOf<List<DescensoStandingDto>?>(null) }
    var goleadores by remember { mutableStateOf<List<GoleadorDto>?>(null) }

    var isLoadingAnnual by remember { mutableStateOf(false) }
    var isLoadingDescenso by remember { mutableStateOf(false) }
    var isLoadingGoleadores by remember { mutableStateOf(false) }

val LIGA_ZONA_A_TEAMS = listOf(
    "Boca Juniors",
    "Central Córdoba (Santiago del Estero)",
    "Defensa y Justicia",
    "Deportivo Riestra",
    "Estudiantes de La Plata",
    "Gimnasia y Esgrima (Mendoza)",
    "Independiente",
    "Instituto (Córdoba)",
    "Lanús",
    "Newell's Old Boys",
    "Platense",
    "San Lorenzo",
    "Talleres (Córdoba)",
    "Unión (Santa Fe)",
    "Vélez Sarsfield"
)

val LIGA_ZONA_B_TEAMS = listOf(
    "Aldosivi",
    "Argentinos Juniors",
    "Atlético Tucumán",
    "Banfield",
    "Barracas Central",
    "Belgrano (Córdoba)",
    "Estudiantes de Río Cuarto",
    "Gimnasia La Plata",
    "Huracán",
    "Independiente Rivadavia",
    "Racing Club",
    "River Plate",
    "Rosario Central",
    "Sarmiento (Junín)",
    "Tigre"
)

    val currentTournamentMatches = remember(matches, tournamentId) {
        matches.filter { it.tournament_id == tournamentId }
    }

    val teamsByGroup = remember(currentTournamentMatches, tournamentFormat) {
        val rawTeams = currentTournamentMatches.flatMap { listOfNotNull(it.homeTeam, it.awayTeam) }
            .distinctBy { it.name.trim().lowercase() }
        
        when (tournamentFormat) {
            TournamentFormat.LIGA_PROFESIONAL -> {
                val teamsMap = rawTeams.mapNotNull { team ->
                    val canonical = getCanonicalLigaTeam(team.name)
                    if (canonical != null) {
                        canonical.first to team.copy(name = canonical.first, group = canonical.second, players = team.players ?: emptyList())
                    } else null
                }.toMap()

                val zonaATeams = LIGA_ZONA_A_TEAMS.map { name ->
                    teamsMap[name] ?: Team(id = 8300 + Math.abs(name.hashCode() % 1000), name = name, flagUrl = "", group = "Zona A", players = emptyList())
                }
                val zonaBTeams = LIGA_ZONA_B_TEAMS.map { name ->
                    teamsMap[name] ?: Team(id = 8300 + Math.abs(name.hashCode() % 1000), name = name, flagUrl = "", group = "Zona B", players = emptyList())
                }

                mapOf(
                    "Zona A" to zonaATeams,
                    "Zona B" to zonaBTeams
                ).toSortedMap()
            }
            TournamentFormat.PRIMERA_NACIONAL -> {
                val teamsMap = rawTeams.associateBy { it.name.trim().lowercase() }
                val zonaATeams = PN_ZONA_A_TEAMS.map { name ->
                    teamsMap[name.lowercase()] ?: rawTeams.find { it.name.contains(name, ignoreCase = true) } ?: Team(id = 8500 + Math.abs(name.hashCode() % 1000), name = name, flagUrl = "", group = "Zona A", players = emptyList())
                }
                val zonaBTeams = PN_ZONA_B_TEAMS.map { name ->
                    teamsMap[name.lowercase()] ?: rawTeams.find { it.name.contains(name, ignoreCase = true) } ?: Team(id = 8500 + Math.abs(name.hashCode() % 1000), name = name, flagUrl = "", group = "Zona B", players = emptyList())
                }
                mapOf(
                    "Zona A" to zonaATeams,
                    "Zona B" to zonaBTeams,
                    "Tabla General" to (zonaATeams + zonaBTeams)
                )
            }
            TournamentFormat.TORNEO_FEDERAL_A -> {
                mapOf(
                    "Tabla General" to rawTeams
                )
            }
            else -> {
                val grouped = rawTeams
                    .filter { it.id > 0 && it.group.isNotEmpty() && it.group != "Eliminación" && it.group != "TBD" }
                    .groupBy { it.group }
                    .toSortedMap()
                
                if (grouped.isEmpty() && rawTeams.isNotEmpty()) {
                    mapOf("Tabla General" to rawTeams)
                } else {
                    grouped
                }
            }
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

    val activeTournaments = remember {
        com.example.worldcup2026.data.model.MasterTeamCatalog.ACTIVE_TOURNAMENTS
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Selector Rápido de Torneos en Juego
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(activeTournaments) { t ->
                val isSelected = t.id == tournamentId
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        com.example.worldcup2026.data.util.SoundManager.playTic()
                        tournamentId = t.id
                        selectedTab = 0
                    },
                    label = {
                        Text(
                            text = t.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.85f)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFC107),
                        containerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(30.dp)
                )
            }
        }

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

        Box(modifier = Modifier.weight(1f)) {
            when (currentTabTitle) {
                "FIXTURE" -> {
                    TournamentFixtureView(
                        matches = filteredMatches,
                        onMatchClick = onNavigateToMatch
                    )
                }
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

fun assignInferredMatchdays(matches: List<Match>): List<Match> {
    if (matches.isEmpty()) return matches
    
    // Si ya tienen matchdays válidos asignados (> 0) y cubren la gran mayoría, los usamos
    val countWithMatchdays = matches.count { (it.matchday ?: 0) > 0 }
    if (countWithMatchdays >= (matches.size * 0.7)) {
        return matches.map { if ((it.matchday ?: 0) <= 0) it.copy(matchday = 1) else it }
    }

    val tournamentId = matches.firstOrNull()?.tournament_id ?: 5
    val sorted = matches.sortedWith(compareBy({ it.date ?: "" }, { it.id }))

    // Cantidad exacta de partidos por fecha según estructura de cada torneo:
    val matchesPerRound = when (tournamentId) {
        5 -> 15     // Liga Profesional (30 equipos = 15 partidos por fecha)
        7 -> 19     // Primera Nacional (38 equipos = 19 partidos por fecha)
        8 -> 11     // Primera B Metropolitana (22 equipos = 11 partidos por fecha)
        9, 10 -> 12 // Primera C (25 equipos = 12 partidos por fecha)
        15 -> 18    // Torneo Federal A (~18 partidos por jornada)
        else -> null
    }

    if (matchesPerRound != null && matchesPerRound > 0) {
        return sorted.mapIndexed { index, m ->
            m.copy(matchday = (index / matchesPerRound) + 1)
        }
    }
    
    // Algoritmo adaptativo para copas o torneos sin estructura fija
    var currentMatchday = 1
    val teamsInCurrent = mutableSetOf<String>()
    var matchesInCurrent = 0
    val result = mutableListOf<Match>()
    
    for (m in sorted) {
        val home = m.homeTeam.name.trim()
        val away = m.awayTeam.name.trim()
        
        val teamCollision = (home.isNotBlank() && home in teamsInCurrent) || (away.isNotBlank() && away in teamsInCurrent)
        
        if (teamCollision && matchesInCurrent >= 4) {
            currentMatchday++
            teamsInCurrent.clear()
            matchesInCurrent = 0
        }
        
        if (home.isNotBlank()) teamsInCurrent.add(home)
        if (away.isNotBlank()) teamsInCurrent.add(away)
        matchesInCurrent++
        
        result.add(m.copy(matchday = currentMatchday))
    }
    
    return result
}

@Composable
fun TournamentFixtureView(
    matches: List<Match>,
    onMatchClick: (Match) -> Unit = {}
) {
    if (matches.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No hay partidos registrados para este torneo",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    // Procesamos e inferimos las fechas reales de los partidos
    val processedMatches = remember(matches) {
        assignInferredMatchdays(matches)
    }

    // Agrupamos por Matchday / Fecha ordenado
    val matchdaysGrouped = remember(processedMatches) {
        processedMatches.groupBy { it.matchday ?: 1 }.toSortedMap()
    }

    val matchdaysList = remember(matchdaysGrouped) { matchdaysGrouped.keys.toList() }
    
    // Auto-detectar la fecha actual o próxima más relevante según fecha real de calendario
    val defaultMatchday = remember(processedMatches, matchdaysList) {
        val todayStr = java.time.LocalDate.now().toString()
        // 1. Partido en vivo
        val liveMatch = processedMatches.firstOrNull { 
            it.status.uppercase() in listOf("LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE") 
        }
        if (liveMatch?.matchday != null && liveMatch.matchday in matchdaysList) {
            return@remember liveMatch.matchday
        }
        // 2. Buscar por fecha: encontrar el primer matchday que tenga partidos con fecha >= hoy (ej: septiembre 2026 -> Fecha 8)
        val upcomingMatch = processedMatches.filter {
            val d = it.date ?: ""
            d.length >= 10 && d.substring(0, 10) >= todayStr
        }.minByOrNull { it.date ?: "9999" }
        
        if (upcomingMatch?.matchday != null && upcomingMatch.matchday in matchdaysList) {
            return@remember upcomingMatch.matchday
        }
        
        // 3. Si no hay partidos futuros, buscar la fecha más reciente jugada
        val latestPastMatch = processedMatches.filter {
            val d = it.date ?: ""
            d.length >= 10
        }.maxByOrNull { it.date ?: "" }
        
        if (latestPastMatch?.matchday != null && latestPastMatch.matchday in matchdaysList) {
            return@remember latestPastMatch.matchday
        }

        matchdaysList.firstOrNull() ?: 1
    }

    var selectedMatchday by remember(processedMatches, defaultMatchday) {
        mutableStateOf(defaultMatchday)
    }

    LaunchedEffect(defaultMatchday) {
        selectedMatchday = defaultMatchday
    }

    val listState = rememberLazyListState()

    // Auto-scroll del selector horizontal al chip de la fecha actual
    LaunchedEffect(selectedMatchday, matchdaysList) {
        val index = matchdaysList.indexOf(selectedMatchday)
        if (index >= 0) {
            try {
                listState.animateScrollToItem((index - 1).coerceAtLeast(0))
            } catch (e: Exception) {
                // Ignore scroll exceptions
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Selector horizontal de Fechas / Jornadas
        if (matchdaysList.size > 1) {
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(matchdaysList) { md ->
                    val isSelected = md == selectedMatchday
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            com.example.worldcup2026.data.util.SoundManager.playTic()
                            selectedMatchday = md
                        },
                        label = {
                            Text(
                                text = "Fecha $md",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFC107),
                            containerColor = Color.White.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }

        // Barra de navegación de Fecha y estado
        val currentIndex = matchdaysList.indexOf(selectedMatchday)
        val matchesForSelectedMatchday = remember(processedMatches, selectedMatchday, matchdaysGrouped) {
            (matchdaysGrouped[selectedMatchday] ?: processedMatches).sortedBy { it.date ?: "" }
        }

        val hasLiveInMatchday = matchesForSelectedMatchday.any { 
            it.status.uppercase() in listOf("LIVE", "HALFTIME", "PAUSA", "PAUSE") 
        }
        val allFinished = matchesForSelectedMatchday.all { 
            it.status.uppercase() in listOf("FINISHED", "FT", "FINALIZADO") 
        }

        Surface(
            color = Color.White.copy(alpha = 0.04f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentIndex > 0) {
                            com.example.worldcup2026.data.util.SoundManager.playTic()
                            selectedMatchday = matchdaysList[currentIndex - 1]
                        }
                    },
                    enabled = currentIndex > 0,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Fecha anterior",
                        tint = if (currentIndex > 0) Color.White else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = when {
                        hasLiveInMatchday -> "🔴 FECHA $selectedMatchday · EN JUEGO"
                        allFinished -> "🏁 FECHA $selectedMatchday · FINALIZADA"
                        selectedMatchday == defaultMatchday -> "⭐ FECHA $selectedMatchday · ACTUAL"
                        else -> "FECHA $selectedMatchday"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = when {
                        hasLiveInMatchday -> Color(0xFFE53935)
                        selectedMatchday == defaultMatchday -> Color(0xFFFFD700)
                        else -> Color.White
                    }
                )

                IconButton(
                    onClick = {
                        if (currentIndex < matchdaysList.size - 1) {
                            com.example.worldcup2026.data.util.SoundManager.playTic()
                            selectedMatchday = matchdaysList[currentIndex + 1]
                        }
                    },
                    enabled = currentIndex < matchdaysList.size - 1,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Fecha siguiente",
                        tint = if (currentIndex < matchdaysList.size - 1) Color.White else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(matchesForSelectedMatchday) { match ->
                FixtureMatchCard(
                    match = match,
                    onClick = { onMatchClick(match) }
                )
            }
        }
    }
}

@Composable
fun FixtureMatchCard(
    match: Match,
    onClick: () -> Unit = {}
) {
    val isLive = match.status.uppercase() in listOf("LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE")
    val isFinished = match.status.uppercase() in listOf("FINISHED", "FT", "FINALIZADO")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                com.example.worldcup2026.data.util.SoundManager.playTic()
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161A28)),
        border = if (isLive) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935)) else null
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Cabecera: Fecha / Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.date ?: "",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                
                Surface(
                    color = when {
                        isLive -> Color(0xFFE53935).copy(alpha = 0.2f)
                        isFinished -> Color.White.copy(alpha = 0.08f)
                        else -> Color(0xFFFFC107).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = when {
                            isLive -> "🔴 EN VIVO"
                            isFinished -> "FINALIZADO"
                            else -> "PROGRAMADO"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isLive -> Color(0xFFE53935)
                            isFinished -> Color.White.copy(alpha = 0.7f)
                            else -> Color(0xFFFFC107)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de Partido (Local vs Visitante)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Local
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.homeTeam.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (!match.homeTeam.flagUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = match.homeTeam.flagUrl,
                            contentDescription = match.homeTeam.name,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Resultado / Marcador
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFinished || isLive) {
                        Text(
                            text = "${match.homeScore ?: 0} - ${match.awayScore ?: 0}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isLive) Color(0xFFE53935) else Color(0xFFFFC107)
                        )
                    } else {
                        Text(
                            text = "vs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                // Visitante
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    if (!match.awayTeam.flagUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = match.awayTeam.flagUrl,
                            contentDescription = match.awayTeam.name,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = match.awayTeam.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!isFinished) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Toca acá para cargar pronóstico / votar en el Prode",
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
