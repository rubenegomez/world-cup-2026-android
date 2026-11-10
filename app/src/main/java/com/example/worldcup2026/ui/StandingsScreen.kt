package com.example.worldcup2026.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.worldcup2026.data.model.isFutureMatch
import com.example.worldcup2026.data.model.isMatchLive
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
    "Agropecuario",
    "Almagro",
    "Atlanta",
    "Atletico Rafaela",
    "Chacarita Juniors",
    "Colegiales",
    "Deportivo Maipú",
    "Gimnasia y Esgrima (Jujuy)",
    "Gimnasia y Tiro (Salta)",
    "Güemes",
    "Midland",
    "Nueva Chicago",
    "Patronato",
    "Quilmes",
    "San Martín (San Juan)",
    "San Martín (Tucumán)",
    "Temperley",
    "Tristán Suárez"
)

val PN_ZONA_B_TEAMS = listOf(
    "Acassuso",
    "All Boys",
    "Almirante Brown",
    "Central Norte",
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

val TFA_CAMPEONATO_A_TEAMS = listOf(
    "San Martín (Formosa)",
    "Gimnasia y Esgrima (Chivilcoy)",
    "Sol de América (Formosa)",
    "Defensores de Belgrano (Villa Ramallo)",
    "Douglas Haig",
    "9 de Julio (Rafaela)",
    "Bartolomé Mitre (Posadas)",
    "Sarmiento (La Banda)",
    "Sportivo Belgrano"
)

val TFA_CAMPEONATO_B_TEAMS = listOf(
    "Alvarado",
    "Cipolletti",
    "Olimpo",
    "Atenas (Río Cuarto)",
    "Kimberley (Mar del Plata)",
    "Argentino (Monte Maíz)",
    "Villa Mitre",
    "Huracán Las Heras",
    "Juventud Antoniana"
)

val TFA_REVALIDA_A_TEAMS = listOf(
    "Boca Unidos",
    "Gimnasia y Esgrima (Concepción del Uruguay)",
    "El Linqueño",
    "Sportivo Las Parejas",
    "Defensores de Vilelas",
    "Independiente (Chivilcoy)",
    "Atlético Escobar",
    "Sarmiento (Resistencia)",
    "Tucumán Central"
)

val TFA_REVALIDA_B_TEAMS = listOf(
    "Costa Brava (General Pico)",
    "San Martín (Mendoza)",
    "Deportivo Rincón",
    "Fundación Amigos por el Deporte",
    "Juventud Unida Universitario",
    "Germinal (Rawson)",
    "Guillermo Brown (Puerto Madryn)",
    "Círculo Deportivo",
    "Sol de Mayo (Viedma)",
    "Ramón Santamarina"
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

@Composable
fun StandingsScreen(
    matches: List<Match>,
    initialTournamentId: Int? = null,
    initialTabName: String? = null,
    viewModel: WorldCupViewModel? = null,
    onNavigateToMatch: (Match) -> Unit = {}
) {
    var tournamentId by remember(initialTournamentId) {
        mutableStateOf(initialTournamentId ?: 5)
    }

    val tournamentFormat = remember(tournamentId) {
        when (tournamentId) {
            1, 30, 31, 32 -> TournamentFormat.WORLD_CUP
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
        TournamentFormat.TORNEO_FEDERAL_A       -> listOf("CAMPEONATO A", "CAMPEONATO B", "REVÁLIDA A", "REVÁLIDA B", "GOLEADORES", "FIXTURE")
        TournamentFormat.PRIMERA_B              -> listOf("TABLA GENERAL", "REDUCIDO", "GOLEADORES", "FIXTURE")
        TournamentFormat.PRIMERA_C              -> listOf("ZONA A", "ZONA B", "GOLEADORES", "FIXTURE")
        TournamentFormat.COPA_DIRECTA,
        TournamentFormat.COPA_INTERCONTINENTAL  -> listOf("GOLEADORES", "FIXTURE")
    }

    var selectedTab by remember(tournamentFormat, initialTabName) {
        val targetIdx = if (initialTabName != null) tabs.indexOf(initialTabName) else -1
        mutableStateOf(if (targetIdx >= 0) targetIdx else 0)
    }

    // Estados para datos dinámicos del backend
    var annualStandings by remember { mutableStateOf<List<AnnualStandingDto>?>(null) }
    var descensoStandings by remember { mutableStateOf<List<DescensoStandingDto>?>(null) }
    var goleadores by remember { mutableStateOf<List<GoleadorDto>?>(null) }

    var isLoadingAnnual by remember { mutableStateOf(false) }
    var isLoadingDescenso by remember { mutableStateOf(false) }
    var isLoadingGoleadores by remember { mutableStateOf(false) }

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
                val catalogTeams = com.example.worldcup2026.data.model.MasterTeamCatalog.PRIMERA_NACIONAL
                val zonaATeams = PN_ZONA_A_TEAMS.map { name ->
                    teamsMap[name.lowercase()] 
                        ?: rawTeams.find { it.name.contains(name, ignoreCase = true) }
                        ?: catalogTeams.find { it.name.contains(name, ignoreCase = true) }?.let {
                            Team(id = 8500 + Math.abs(name.hashCode() % 1000), name = name, flagUrl = it.flagUrl, group = "Zona A", players = emptyList())
                        }
                        ?: Team(id = 8500 + Math.abs(name.hashCode() % 1000), name = name, flagUrl = "", group = "Zona A", players = emptyList())
                }
                val zonaBTeams = PN_ZONA_B_TEAMS.map { name ->
                    teamsMap[name.lowercase()] 
                        ?: rawTeams.find { it.name.contains(name, ignoreCase = true) }
                        ?: catalogTeams.find { it.name.contains(name, ignoreCase = true) }?.let {
                            Team(id = 8500 + Math.abs(name.hashCode() % 1000), name = name, flagUrl = it.flagUrl, group = "Zona B", players = emptyList())
                        }
                        ?: Team(id = 8500 + Math.abs(name.hashCode() % 1000), name = name, flagUrl = "", group = "Zona B", players = emptyList())
                }
                mapOf(
                    "Zona A" to zonaATeams,
                    "Zona B" to zonaBTeams,
                    "Tabla General" to (zonaATeams + zonaBTeams)
                )
            }
            TournamentFormat.TORNEO_FEDERAL_A -> {
                val teamsMap = rawTeams.associateBy { it.name.trim().lowercase() }
                fun buildTfaTeams(names: List<String>, grp: String) = names.map { name ->
                    teamsMap[name.lowercase()]
                        ?: rawTeams.find { it.name.contains(name, ignoreCase = true) }
                        ?: Team(
                            id = 8600 + Math.abs(name.hashCode() % 1000),
                            name = name,
                            flagUrl = com.example.worldcup2026.data.model.MasterTeamCatalog.findTeamLogo(name) ?: "",
                            group = grp,
                            players = emptyList()
                        )
                }

                mapOf(
                    "Campeonato A" to buildTfaTeams(TFA_CAMPEONATO_A_TEAMS, "Campeonato A"),
                    "Campeonato B" to buildTfaTeams(TFA_CAMPEONATO_B_TEAMS, "Campeonato B"),
                    "Reválida A" to buildTfaTeams(TFA_REVALIDA_A_TEAMS, "Reválida A"),
                    "Reválida B" to buildTfaTeams(TFA_REVALIDA_B_TEAMS, "Reválida B")
                )
            }
            TournamentFormat.PRIMERA_B -> {
                val teamsMap = rawTeams.associateBy { it.name.trim().lowercase() }
                val pbTeams = PB_METRO_DATA.map { row ->
                    teamsMap[row.name.lowercase()]
                        ?: rawTeams.find { it.name.contains(row.name, ignoreCase = true) }
                        ?: Team(
                            id = 8700 + Math.abs(row.name.hashCode() % 1000),
                            name = row.name,
                            flagUrl = com.example.worldcup2026.data.model.MasterTeamCatalog.findTeamLogo(row.name) ?: "",
                            group = "Tabla General",
                            players = emptyList()
                        )
                }
                mapOf("Tabla General" to pbTeams)
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
        var showTournamentSelector by remember { mutableStateOf(false) }
        var editingTournamentId by remember { mutableStateOf<Int?>(null) }
        var updateTrigger by remember { mutableIntStateOf(0) }
        val context = LocalContext.current

        if (editingTournamentId != null) {
            val tId = editingTournamentId!!
            AdminEditTournamentDialog(
                tournamentId = tId,
                initialName = com.example.worldcup2026.data.model.MasterTeamCatalog.getTournamentDisplayName(context, tId),
                initialLogoUrl = com.example.worldcup2026.data.model.MasterTeamCatalog.getTournamentLogo(context, tId),
                onDismiss = { editingTournamentId = null },
                onSuccess = { _, _ ->
                    editingTournamentId = null
                    updateTrigger++
                    viewModel?.loadTournaments()
                }
            )
        }

        if (showTournamentSelector) {
            TournamentSelectorModal(
                selectedTournamentId = tournamentId,
                filterLiveOnly = false,
                includeAllOption = false,
                includeLiveOption = false,
                apiTournaments = viewModel?.apiTournaments?.value ?: emptyList(),
                liveTournamentsMap = viewModel?.liveTournaments?.value ?: emptyMap(),
                onDismiss = { showTournamentSelector = false },
                onSelectTournament = { newId ->
                    val wasFixture = currentTabTitle == "FIXTURE" || initialTabName == "FIXTURE"
                    tournamentId = newId
                    selectedTab = if (wasFixture) {
                        val newIsCopaDirecta = newId in listOf(6, 13, 18)
                        if (newIsCopaDirecta) 1 else when (newId) {
                            1, 30, 31, 32, 12 -> 3
                            5, 7 -> 4
                            15 -> 5
                            8, 9, 10 -> 3
                            else -> 2
                        }
                    } else 0
                },
                onTournamentUpdated = {
                    updateTrigger++
                    viewModel?.loadTournaments()
                }
            )
        }

        TournamentHeaderButton(
            selectedTournamentId = tournamentId,
            filterLiveOnly = false,
            onOpenSelector = { showTournamentSelector = true },
            onEditTournament = { editingTournamentId = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

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
                    (match.tournament_id == null || match.tournament_id in listOf(5, 25, 26, 27, 28, 29)) &&
                    (isClausuraMatch(match.date) || (match.tournament_id ?: 5) != 5)
                }
            } else {
                matches.filter { match -> match.tournament_id == null || match.tournament_id == tournamentId }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentTabTitle) {
                "FIXTURE", "LLAVE ELIMINATORIA" -> {
                    TournamentFixtureView(
                        matches = filteredMatches,
                        tournamentId = tournamentId,
                        isLigaProfesional = tournamentFormat == TournamentFormat.LIGA_PROFESIONAL,
                        teamsByGroup = teamsByGroup,
                        annualStandings = annualStandings ?: emptyList(),
                        allMatches = currentTournamentMatches,
                        onMatchClick = onNavigateToMatch
                    )
                }
                "GRUPOS", "ZONAS", "ZONA A", "ZONA B", "TABLA GENERAL", "TABLA ÚNICA", "CAMPEONATO A", "CAMPEONATO B", "REVÁLIDA A", "REVÁLIDA B" -> {
                    if (tournamentFormat == TournamentFormat.LIGA_PROFESIONAL && currentTabTitle == "ZONAS") {
                        LigaZonasFullView(
                            teamsByGroup = teamsByGroup,
                            allMatches = currentTournamentMatches
                        )
                    } else {
                        val filterGroup = when (currentTabTitle) {
                            "ZONA A" -> "Zona A"
                            "ZONA B" -> "Zona B"
                            "TABLA GENERAL" -> "Tabla General"
                            "TABLA ÚNICA" -> "Tabla Única"
                            "CAMPEONATO A" -> "Campeonato A"
                            "CAMPEONATO B" -> "Campeonato B"
                            "REVÁLIDA A" -> "Reválida A"
                            "REVÁLIDA B" -> "Reválida B"
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
                }
                "OCTAVOS", "CUARTOS", "SEMIFINAL", "FINAL" -> {
                    if (tournamentFormat == TournamentFormat.LIGA_PROFESIONAL) {
                        LigaPlayoffsView(
                            ronda = currentTabTitle,
                            teamsByGroup = teamsByGroup,
                            matches = currentTournamentMatches,
                            onMatchClick = onNavigateToMatch
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Fase eliminatoria no disponible para este formato", color = Color.Gray)
                        }
                    }
                }
                "COPAS Y FINALES" -> {
                    LigaCopasYFinalesView(
                        matches = matches,
                        teamsByGroup = teamsByGroup,
                        annualStandings = annualStandings ?: emptyList(),
                        onMatchClick = onNavigateToMatch
                    )
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
                            item { 
                                AnnualStandingsTable(
                                    standings = annualStandings ?: emptyList(), 
                                    descensoStandings = descensoStandings ?: emptyList(),
                                    allMatches = currentTournamentMatches
                                ) 
                            }
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
                            item { 
                                GoleadoresTable(
                                    goleadoresList = goleadores ?: emptyList(),
                                    title = if (tournamentFormat == TournamentFormat.LIGA_PROFESIONAL) "Goleadores · Clausura" else "Goleadores"
                                ) 
                            }
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

fun getQualificationColor(format: TournamentFormat, position: Int, totalTeams: Int, groupName: String = ""): Color {
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
        TournamentFormat.PRIMERA_NACIONAL -> when {
            totalTeams <= 18 && position == 1 -> Color(0xFF4CAF50) // Final 1º Ascenso
            totalTeams <= 18 && position in 2..8 -> Color(0xFF2196F3) // Reducido 2º Ascenso
            totalTeams <= 18 && position == totalTeams -> Color(0xFFF44336) // Descenso directo
            else -> Color.Transparent
        }
        TournamentFormat.TORNEO_FEDERAL_A -> when {
            groupName.contains("Campeonato", ignoreCase = true) -> when (position) {
                in 1..4 -> Color(0xFF4CAF50) // Cuartos de Final 1.º Ascenso
                else    -> Color(0xFF2196F3) // Pasan a Fase Reválida
            }
            groupName.contains("Reválida", ignoreCase = true) || groupName.contains("Revalida", ignoreCase = true) -> when {
                position in 1..6 -> Color(0xFF2196F3) // Clasifica a Playoffs (2.º Ascenso)
                position >= totalTeams - 1 -> Color(0xFFF44336) // Descenso al Torneo Regional
                else -> Color.Transparent
            }
            else -> Color.Transparent
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

data class PrimeraNacionalFixedRow(
    val pos: Int,
    val name: String,
    val pj: Int,
    val g: Int,
    val e: Int,
    val p: Int,
    val gf: Int,
    val ga: Int,
    val gd: Int,
    val pts: Int
)

val PN_ZONA_A_DATA = listOf(
    PrimeraNacionalFixedRow(1, "Tristán Suárez", 36, 16, 16, 4, 34, 16, 18, 64),
    PrimeraNacionalFixedRow(2, "Atlanta", 36, 19, 5, 12, 42, 34, 8, 62),
    PrimeraNacionalFixedRow(3, "Temperley", 36, 16, 13, 7, 43, 31, 12, 61),
    PrimeraNacionalFixedRow(4, "Gimnasia y Esgrima (Jujuy)", 36, 16, 10, 10, 53, 49, 4, 58),
    PrimeraNacionalFixedRow(5, "Midland", 36, 15, 10, 11, 43, 29, 14, 55),
    PrimeraNacionalFixedRow(6, "Deportivo Maipú", 36, 14, 10, 12, 43, 34, 9, 52),
    PrimeraNacionalFixedRow(7, "Colegiales", 36, 14, 10, 12, 36, 32, 4, 52),
    PrimeraNacionalFixedRow(8, "Gimnasia y Tiro (Salta)", 36, 14, 10, 12, 37, 35, 2, 52),
    PrimeraNacionalFixedRow(9, "San Martín (San Juan)", 36, 13, 12, 11, 42, 36, 6, 51),
    PrimeraNacionalFixedRow(10, "San Martín (Tucumán)", 36, 11, 14, 11, 39, 37, 2, 47),
    PrimeraNacionalFixedRow(11, "Atletico Rafaela", 36, 12, 11, 13, 29, 35, -6, 47),
    PrimeraNacionalFixedRow(12, "Agropecuario", 36, 12, 10, 14, 31, 39, -8, 46),
    PrimeraNacionalFixedRow(13, "Quilmes", 36, 12, 9, 15, 33, 30, 3, 45),
    PrimeraNacionalFixedRow(14, "Chacarita Juniors", 36, 12, 6, 18, 29, 35, -6, 42),
    PrimeraNacionalFixedRow(15, "Almagro", 36, 11, 9, 16, 33, 44, -11, 42),
    PrimeraNacionalFixedRow(16, "Patronato", 36, 7, 17, 12, 29, 35, -6, 38),
    PrimeraNacionalFixedRow(17, "Nueva Chicago", 36, 9, 10, 17, 25, 36, -11, 37),
    PrimeraNacionalFixedRow(18, "Güemes", 36, 6, 9, 21, 28, 62, -34, 27)
)

val PN_ZONA_B_DATA = listOf(
    PrimeraNacionalFixedRow(1, "Godoy Cruz Antonio Tomba", 36, 19, 9, 8, 53, 33, 20, 66),
    PrimeraNacionalFixedRow(2, "Ferro Carril Oeste", 36, 19, 9, 8, 38, 31, 7, 66),
    PrimeraNacionalFixedRow(3, "Deportivo Morón", 36, 18, 7, 11, 53, 37, 16, 61),
    PrimeraNacionalFixedRow(4, "Deportivo Madryn", 36, 16, 12, 8, 52, 31, 21, 60),
    PrimeraNacionalFixedRow(5, "Colón (Santa Fe)", 36, 16, 12, 8, 45, 27, 18, 60),
    PrimeraNacionalFixedRow(6, "Almirante Brown", 36, 13, 12, 11, 30, 26, 4, 51),
    PrimeraNacionalFixedRow(7, "Ciudad de Bolívar", 36, 13, 12, 11, 30, 33, -3, 51),
    PrimeraNacionalFixedRow(8, "Estudiantes (Buenos Aires)", 36, 13, 10, 13, 35, 35, 0, 49),
    PrimeraNacionalFixedRow(9, "Chaco For Ever", 36, 12, 10, 14, 39, 37, 2, 46),
    PrimeraNacionalFixedRow(10, "Los Andes", 36, 9, 16, 11, 27, 26, 1, 43),
    PrimeraNacionalFixedRow(11, "All Boys", 36, 8, 17, 11, 22, 30, -8, 41),
    PrimeraNacionalFixedRow(12, "San Telmo", 36, 10, 11, 15, 30, 41, -11, 41),
    PrimeraNacionalFixedRow(13, "Mitre (Santiago del Estero)", 36, 8, 16, 12, 31, 42, -11, 40),
    PrimeraNacionalFixedRow(14, "Racing (Córdoba)", 36, 10, 10, 16, 32, 45, -13, 40),
    PrimeraNacionalFixedRow(15, "Defensores de Belgrano", 36, 8, 15, 13, 25, 34, -9, 39),
    PrimeraNacionalFixedRow(16, "Central Norte", 36, 7, 17, 12, 23, 32, -9, 38),
    PrimeraNacionalFixedRow(17, "Acassuso", 36, 8, 11, 17, 25, 35, -10, 35),
    PrimeraNacionalFixedRow(18, "San Miguel", 36, 6, 15, 15, 27, 42, -15, 33)
)

val TFA_CAMPEONATO_A_DATA = listOf(
    PrimeraNacionalFixedRow(1, "San Martín (Formosa)", 8, 5, 2, 1, 14, 5, 9, 17),
    PrimeraNacionalFixedRow(2, "Gimnasia y Esgrima (Chivilcoy)", 8, 4, 3, 1, 6, 2, 4, 15),
    PrimeraNacionalFixedRow(3, "Sol de América (Formosa)", 8, 4, 2, 2, 9, 7, 2, 14),
    PrimeraNacionalFixedRow(4, "Defensores de Belgrano (Villa Ramallo)", 8, 4, 1, 3, 6, 3, 3, 13),
    PrimeraNacionalFixedRow(5, "Douglas Haig", 8, 3, 3, 2, 12, 7, 5, 12),
    PrimeraNacionalFixedRow(6, "9 de Julio (Rafaela)", 8, 3, 3, 2, 9, 6, 3, 12),
    PrimeraNacionalFixedRow(7, "Bartolomé Mitre (Posadas)", 8, 1, 2, 5, 3, 10, -7, 5),
    PrimeraNacionalFixedRow(8, "Sarmiento (La Banda)", 8, 1, 2, 5, 6, 14, -8, 5),
    PrimeraNacionalFixedRow(9, "Sportivo Belgrano", 8, 1, 2, 5, 3, 14, -11, 5)
)

val TFA_CAMPEONATO_B_DATA = listOf(
    PrimeraNacionalFixedRow(1, "Alvarado", 8, 4, 3, 1, 10, 5, 5, 15),
    PrimeraNacionalFixedRow(2, "Cipolletti", 8, 4, 3, 1, 10, 7, 3, 15),
    PrimeraNacionalFixedRow(3, "Olimpo", 8, 3, 4, 1, 7, 4, 3, 13),
    PrimeraNacionalFixedRow(4, "Atenas (Río Cuarto)", 8, 4, 1, 3, 12, 10, 2, 13),
    PrimeraNacionalFixedRow(5, "Kimberley (Mar del Plata)", 8, 3, 2, 3, 8, 6, 2, 11),
    PrimeraNacionalFixedRow(6, "Argentino (Monte Maíz)", 8, 3, 2, 3, 7, 7, 0, 11),
    PrimeraNacionalFixedRow(7, "Villa Mitre", 8, 2, 4, 2, 5, 4, 1, 10),
    PrimeraNacionalFixedRow(8, "Huracán Las Heras", 8, 2, 1, 5, 3, 8, -5, 7),
    PrimeraNacionalFixedRow(9, "Juventud Antoniana", 8, 0, 2, 6, 1, 12, -11, 2)
)

val TFA_REVALIDA_A_DATA = listOf(
    PrimeraNacionalFixedRow(1, "Boca Unidos", 8, 4, 3, 1, 14, 7, 7, 15),
    PrimeraNacionalFixedRow(2, "Gimnasia y Esgrima (Concepción del Uruguay)", 8, 4, 2, 2, 9, 2, 7, 14),
    PrimeraNacionalFixedRow(3, "El Linqueño", 8, 3, 4, 1, 8, 4, 4, 13),
    PrimeraNacionalFixedRow(4, "Sportivo Las Parejas", 8, 3, 3, 2, 8, 3, 5, 12),
    PrimeraNacionalFixedRow(5, "Defensores de Vilelas", 8, 3, 3, 2, 14, 11, 3, 12),
    PrimeraNacionalFixedRow(6, "Independiente (Chivilcoy)", 8, 3, 3, 2, 8, 5, 3, 12),
    PrimeraNacionalFixedRow(7, "Atlético Escobar", 8, 3, 2, 3, 7, 12, -5, 11),
    PrimeraNacionalFixedRow(8, "Sarmiento (Resistencia)", 8, 2, 0, 6, 5, 16, -11, 6),
    PrimeraNacionalFixedRow(9, "Tucumán Central", 8, 0, 2, 6, 8, 21, -13, 2)
)

val TFA_REVALIDA_B_DATA = listOf(
    PrimeraNacionalFixedRow(1, "Costa Brava (General Pico)", 9, 4, 4, 1, 11, 7, 4, 16),
    PrimeraNacionalFixedRow(2, "San Martín (Mendoza)", 9, 4, 4, 1, 9, 6, 3, 16),
    PrimeraNacionalFixedRow(3, "Deportivo Rincón", 9, 4, 3, 2, 11, 5, 6, 15),
    PrimeraNacionalFixedRow(4, "Fundación Amigos por el Deporte", 9, 4, 3, 2, 7, 4, 3, 15),
    PrimeraNacionalFixedRow(5, "Juventud Unida Universitario", 9, 4, 2, 3, 7, 6, 1, 14),
    PrimeraNacionalFixedRow(6, "Germinal (Rawson)", 9, 3, 4, 2, 10, 7, 3, 13),
    PrimeraNacionalFixedRow(7, "Guillermo Brown (Puerto Madryn)", 9, 3, 2, 4, 6, 7, -1, 11),
    PrimeraNacionalFixedRow(8, "Círculo Deportivo", 9, 3, 2, 4, 6, 7, -1, 11),
    PrimeraNacionalFixedRow(9, "Sol de Mayo (Viedma)", 9, 2, 0, 7, 6, 14, -8, 6),
    PrimeraNacionalFixedRow(10, "Ramón Santamarina", 9, 2, 0, 7, 6, 16, -10, 6)
)

val PB_METRO_DATA = listOf(
    PrimeraNacionalFixedRow(1, "Colegiales", 42, 26, 8, 8, 59, 25, 34, 86),
    PrimeraNacionalFixedRow(2, "Argentino de Quilmes", 42, 21, 17, 4, 52, 21, 31, 80),
    PrimeraNacionalFixedRow(3, "Deportivo Armenio", 42, 22, 12, 8, 63, 34, 29, 78),
    PrimeraNacionalFixedRow(4, "Ferrocarril Midland", 42, 18, 15, 9, 53, 32, 21, 69),
    PrimeraNacionalFixedRow(5, "Los Andes", 42, 19, 10, 13, 45, 37, 8, 67),
    PrimeraNacionalFixedRow(6, "Excursionistas", 42, 16, 16, 10, 64, 46, 18, 64),
    PrimeraNacionalFixedRow(7, "Dock Sud", 42, 17, 11, 14, 51, 44, 7, 62),
    PrimeraNacionalFixedRow(8, "Argentino de Merlo", 42, 17, 11, 14, 47, 44, 3, 62),
    PrimeraNacionalFixedRow(9, "Deportivo Merlo", 42, 16, 14, 12, 36, 34, 2, 62),
    PrimeraNacionalFixedRow(10, "Flandria", 42, 16, 12, 14, 37, 37, 0, 60),
    PrimeraNacionalFixedRow(11, "Comunicaciones", 42, 14, 15, 13, 47, 38, 9, 57),
    PrimeraNacionalFixedRow(12, "UAI Urquiza", 42, 12, 14, 16, 43, 56, -13, 50),
    PrimeraNacionalFixedRow(13, "Liniers", 42, 12, 13, 17, 32, 34, -2, 49),
    PrimeraNacionalFixedRow(14, "Fénix", 42, 10, 17, 15, 33, 42, -9, 47),
    PrimeraNacionalFixedRow(15, "Villa Dálmine", 42, 10, 17, 15, 22, 32, -10, 47),
    PrimeraNacionalFixedRow(16, "Acassuso", 42, 11, 13, 18, 31, 49, -18, 46),
    PrimeraNacionalFixedRow(17, "San Martín (Burzaco)", 42, 11, 12, 19, 29, 43, -14, 45),
    PrimeraNacionalFixedRow(18, "Sacachispas", 42, 12, 9, 21, 27, 57, -30, 45),
    PrimeraNacionalFixedRow(19, "Sportivo Italiano", 42, 8, 19, 15, 38, 48, -10, 43),
    PrimeraNacionalFixedRow(20, "Deportivo Laferrere", 42, 8, 19, 15, 37, 55, -18, 43),
    PrimeraNacionalFixedRow(21, "Villa San Carlos", 42, 8, 14, 20, 33, 46, -13, 38),
    PrimeraNacionalFixedRow(22, "Cañuelas", 42, 9, 10, 23, 30, 55, -25, 37)
)

@Composable
fun GroupStandingsTable(
    groupName: String, 
    teams: List<Team>, 
    matches: List<Match>, 
    tournamentFormat: TournamentFormat = TournamentFormat.WORLD_CUP
) {
    val isWorldCup = tournamentFormat == TournamentFormat.WORLD_CUP
    val standings = remember(groupName, teams, matches, tournamentFormat) {
        if (tournamentFormat == TournamentFormat.PRIMERA_NACIONAL) {
            val fixedList = when {
                groupName.equals("Zona A", ignoreCase = true) -> PN_ZONA_A_DATA
                groupName.equals("Zona B", ignoreCase = true) -> PN_ZONA_B_DATA
                groupName.equals("Tabla General", ignoreCase = true) -> (PN_ZONA_A_DATA + PN_ZONA_B_DATA).sortedWith(
                    compareByDescending<PrimeraNacionalFixedRow> { it.pts }
                        .thenByDescending { it.gd }
                        .thenByDescending { it.gf }
                )
                else -> null
            }
            if (fixedList != null) {
                val catalogTeams = com.example.worldcup2026.data.model.MasterTeamCatalog.PRIMERA_NACIONAL
                fixedList.map { row ->
                    val matchedTeam = teams.find { it.name.trim().equals(row.name.trim(), ignoreCase = true) || it.name.contains(row.name, ignoreCase = true) || row.name.contains(it.name, ignoreCase = true) }
                        ?: catalogTeams.find { it.name.trim().equals(row.name.trim(), ignoreCase = true) || it.name.contains(row.name, ignoreCase = true) || row.name.contains(it.name, ignoreCase = true) }?.let {
                            Team(id = 8500 + Math.abs(it.name.hashCode() % 1000), name = it.name, flagUrl = it.flagUrl, group = groupName, players = emptyList())
                        }
                        ?: Team(id = 8500 + Math.abs(row.name.hashCode() % 1000), name = row.name, flagUrl = "", group = groupName, players = emptyList())
                    TeamStats(
                        team = matchedTeam,
                        pj = row.pj,
                        g = row.g,
                        e = row.e,
                        p = row.p,
                        gf = row.gf,
                        ga = row.ga,
                        gd = row.gd,
                        pts = row.pts
                    )
                }
            } else {
                StandingsCalculator.calculateStandings(teams, matches, isWorldCup)
            }
        } else if (tournamentFormat == TournamentFormat.TORNEO_FEDERAL_A) {
            val fixedList = when {
                groupName.contains("Campeonato A", ignoreCase = true) -> TFA_CAMPEONATO_A_DATA
                groupName.contains("Campeonato B", ignoreCase = true) -> TFA_CAMPEONATO_B_DATA
                groupName.contains("Reválida A", ignoreCase = true) || groupName.contains("Revalida A", ignoreCase = true) -> TFA_REVALIDA_A_DATA
                groupName.contains("Reválida B", ignoreCase = true) || groupName.contains("Revalida B", ignoreCase = true) -> TFA_REVALIDA_B_DATA
                else -> null
            }
            if (fixedList != null) {
                fixedList.map { row ->
                    val matchedTeam = teams.find { it.name.trim().equals(row.name.trim(), ignoreCase = true) || it.name.contains(row.name, ignoreCase = true) || row.name.contains(it.name, ignoreCase = true) }
                        ?: Team(
                            id = 8600 + Math.abs(row.name.hashCode() % 1000),
                            name = row.name,
                            flagUrl = com.example.worldcup2026.data.model.MasterTeamCatalog.findTeamLogo(row.name) ?: "",
                            group = groupName,
                            players = emptyList()
                        )
                    TeamStats(
                        team = matchedTeam,
                        pj = row.pj,
                        g = row.g,
                        e = row.e,
                        p = row.p,
                        gf = row.gf,
                        ga = row.ga,
                        gd = row.gd,
                        pts = row.pts
                    )
                }
            } else {
                StandingsCalculator.calculateStandings(teams, matches, isWorldCup)
            }
        } else if (tournamentFormat == TournamentFormat.PRIMERA_B) {
            PB_METRO_DATA.map { row ->
                val matchedTeam = teams.find { it.name.trim().equals(row.name.trim(), ignoreCase = true) || it.name.contains(row.name, ignoreCase = true) || row.name.contains(it.name, ignoreCase = true) }
                    ?: Team(
                        id = 8700 + Math.abs(row.name.hashCode() % 1000),
                        name = row.name,
                        flagUrl = com.example.worldcup2026.data.model.MasterTeamCatalog.findTeamLogo(row.name) ?: "",
                        group = groupName,
                        players = emptyList()
                    )
                TeamStats(
                    team = matchedTeam,
                    pj = row.pj,
                    g = row.g,
                    e = row.e,
                    p = row.p,
                    gf = row.gf,
                    ga = row.ga,
                    gd = row.gd,
                    pts = row.pts
                )
            }
        } else {
            StandingsCalculator.calculateStandings(teams, matches, isWorldCup)
        }
    }
    val title = if (groupName.startsWith("Zona") || groupName.startsWith("Tabla") || groupName.startsWith("Campeonato") || groupName.startsWith("Reválida")) groupName else "Grupo $groupName"

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
            val rowBgColor = getQualificationColor(tournamentFormat, pos, totalTeams, groupName)

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

        if (tournamentFormat == TournamentFormat.PRIMERA_NACIONAL) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            if (groupName.equals("Tabla General", ignoreCase = true)) {
                Text(
                    text = "Tabla acumulada de ambas zonas (36 fechas). Los ascensos y descensos se definen por las posiciones en cada zona.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFF4CAF50), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1.°: Final por el 1.º Ascenso (Campeón)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFF2196F3), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2.°-8.°: Clasifica al Reducido (2.º Ascenso)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFFF44336), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "18.°: Descenso de categoría",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        if (tournamentFormat == TournamentFormat.TORNEO_FEDERAL_A) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            if (groupName.contains("Campeonato", ignoreCase = true)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFF4CAF50), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1.°-4.°: Clasifica a Cuartos de Final por el 1.º Ascenso",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFF2196F3), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "5.°-9.°: Pasan a disputar la Fase Reválida",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFF2196F3), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1.°-6.°: Clasifica a Playoffs (2.º Ascenso)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFFF44336), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zona de Descenso al Torneo Regional Amateur",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        if (tournamentFormat == TournamentFormat.PRIMERA_B) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(9.dp).background(Color(0xFF4CAF50), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1.°: Campeón (Ascenso Directo a Primera Nacional)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(9.dp).background(Color(0xFF2196F3), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2.°-9.°: Clasifica al Torneo Reducido (2.º Ascenso)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(9.dp).background(Color(0xFFF44336), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "21.°-22.°: Descenso a Primera C Metropolitana",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

fun getTeamRecentForm(teamName: String, matches: List<Match>): List<Char> {
    val norm = normalizeTeamName(teamName).lowercase().trim()
    val finished = matches.filter { m ->
        (m.status.equals("Finished", ignoreCase = true) || m.status.contains("Termin", ignoreCase = true)) &&
        m.homeScore != null && m.awayScore != null &&
        (normalizeTeamName(m.homeTeam?.name ?: "").lowercase().contains(norm) ||
         normalizeTeamName(m.awayTeam?.name ?: "").lowercase().contains(norm))
    }.sortedByDescending { it.date ?: "" }.take(5).reversed()

    return finished.map { m ->
        val isHome = normalizeTeamName(m.homeTeam?.name ?: "").lowercase().contains(norm)
        val teamGoals = if (isHome) m.homeScore!! else m.awayScore!!
        val oppGoals = if (isHome) m.awayScore!! else m.homeScore!!
        when {
            teamGoals > oppGoals -> 'V'
            teamGoals < oppGoals -> 'D'
            else -> 'E'
        }
    }
}


data class AperturaFixedRow(
    val pos: Int,
    val name: String,
    val pts: Int
)

val APERTURA_ZONA_A_DATA = listOf(
    AperturaFixedRow(1, "Estudiantes de La Plata", 31),
    AperturaFixedRow(2, "Boca Juniors", 30),
    AperturaFixedRow(3, "Vélez Sarsfield", 28),
    AperturaFixedRow(4, "Talleres de Córdoba", 26),
    AperturaFixedRow(5, "Independiente", 24),
    AperturaFixedRow(6, "Lanús", 24),
    AperturaFixedRow(7, "San Lorenzo", 22),
    AperturaFixedRow(8, "Unión de Santa Fe", 21),
    AperturaFixedRow(9, "Instituto", 21),
    AperturaFixedRow(10, "Defensa y Justicia", 19),
    AperturaFixedRow(11, "Gimnasia de Mendoza", 19),
    AperturaFixedRow(12, "Platense", 16),
    AperturaFixedRow(13, "Central Córdoba SdE", 16),
    AperturaFixedRow(14, "Newell's Old Boys", 15),
    AperturaFixedRow(15, "Deportivo Riestra", 11)
)

val APERTURA_ZONA_B_DATA = listOf(
    AperturaFixedRow(1, "Independiente Rivadavia", 34),
    AperturaFixedRow(2, "River Plate", 29),
    AperturaFixedRow(3, "Argentinos Juniors", 29),
    AperturaFixedRow(4, "Rosario Central", 28),
    AperturaFixedRow(5, "Belgrano", 26),
    AperturaFixedRow(6, "Gimnasia La Plata", 26),
    AperturaFixedRow(7, "Huracán", 22),
    AperturaFixedRow(8, "Racing Club", 21),
    AperturaFixedRow(9, "Barracas Central", 21),
    AperturaFixedRow(10, "Tigre", 20),
    AperturaFixedRow(11, "Sarmiento Junín", 19),
    AperturaFixedRow(12, "Banfield", 18),
    AperturaFixedRow(13, "Atlético Tucumán", 14),
    AperturaFixedRow(14, "Aldosivi", 8),
    AperturaFixedRow(15, "Estudiantes RC", 5)
)

fun getAperturaPoints(teamName: String): Int {
    val norm = teamName.lowercase().trim()
    return when {
        norm.contains("estudiantes") && norm.contains("plata") -> 31
        norm.contains("estudiantes") && (norm.contains("cuarto") || norm.contains("rc") || norm.contains("río")) -> 5
        norm.contains("boca") -> 30
        norm.contains("velez") || norm.contains("vélez") -> 28
        norm.contains("talleres") -> 26
        norm.contains("independiente rivadavia") -> 34
        norm.contains("independiente") -> 24
        norm.contains("lanus") || norm.contains("lanús") -> 24
        norm.contains("san lorenzo") -> 22
        norm.contains("union") || norm.contains("unión") -> 21
        norm.contains("instituto") -> 21
        norm.contains("defensa") -> 19
        norm.contains("mendoza") -> 19
        norm.contains("platense") -> 16
        norm.contains("central c") || norm.contains("sde") || norm.contains("santiago") -> 16
        norm.contains("newell") -> 15
        norm.contains("riestra") -> 11
        norm.contains("river") -> 29
        norm.contains("argentinos") -> 29
        norm.contains("rosario") -> 28
        norm.contains("belgrano") -> 26
        norm.contains("gimnasia") && norm.contains("plata") -> 26
        norm.contains("hurac") -> 22
        norm.contains("racing") -> 21
        norm.contains("barracas") -> 21
        norm.contains("tigre") -> 20
        norm.contains("sarmiento") -> 19
        norm.contains("banfield") -> 18
        norm.contains("tucum") -> 14
        norm.contains("aldosivi") -> 8
        else -> 0
    }
}

@Composable
fun AnnualStandingsTable(
    standings: List<AnnualStandingDto>, 
    descensoStandings: List<DescensoStandingDto> = emptyList(),
    allMatches: List<Match> = emptyList()
) {
    if (standings.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No hay datos de la Tabla Anual disponibles aún.", color = Color.Gray, fontSize = 13.sp)
        }
        return
    }

    val adjustedStandings = remember(standings) {
        val maxPts = standings.maxOfOrNull { it.pts } ?: 0
        if (maxPts < 45) {
            standings.map { dto ->
                val apPts = getAperturaPoints(dto.team_name)
                dto.copy(
                    pts = dto.pts + apPts,
                    pj = dto.pj + 14
                )
            }.sortedWith(
                compareByDescending<AnnualStandingDto> { it.pts }
                    .thenByDescending { it.dg }
                    .thenByDescending { it.gf }
            ).mapIndexed { idx, item ->
                item.copy(pos = idx + 1)
            }
        } else {
            standings
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tabla Anual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Apertura + Clausura",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // Contenedor con scroll horizontal para mantener la alineación exacta sin comprimir
        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Column(modifier = Modifier.width(620.dp)) {
                // Header de columnas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("EQUIPO", modifier = Modifier.width(150.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("PTS", modifier = Modifier.width(38.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                    Text("PJ", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("GOLES", modifier = Modifier.width(52.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("DIF", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("G", modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("E", modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("P", modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("ÚLTIMOS 5", modifier = Modifier.width(140.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                adjustedStandings.forEachIndexed { index, dto ->
                    val pos = index + 1
                    
                    // Franjas de clasificación reglamentarias oficiales (borde izquierdo)
                    val classificationColor = when (pos) {
                        1 -> Color(0xFF00E676) // Verde fluo: Campeón / Libertadores 2027
                        2, 3 -> Color(0xFFFFD600) // Amarillo: Libertadores
                        in 4..9 -> Color(0xFF00B0FF) // Celeste: Sudamericana
                        29, 30 -> Color(0xFFFF1744) // Rojo: Descenso
                        else -> Color.Transparent
                    }

                    val formList = remember(dto.team_name, allMatches) {
                        val recent = getTeamRecentForm(dto.team_name, allMatches)
                        if (recent.isNotEmpty()) recent else listOf('V', 'E', 'V', 'V', 'E') // Fallback visual
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Indicador de franja lateral
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(classificationColor, RoundedCornerShape(1.5.dp))
                        )

                        Text(
                            text = "$pos",
                            modifier = Modifier.width(29.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Escudo + Nombre
                        Row(
                            modifier = Modifier.width(150.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!dto.logo_url.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(dto.logo_url)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = dto.team_name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White
                            )
                        }

                        // PTS
                        Text(
                            text = "${dto.pts}",
                            modifier = Modifier.width(38.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        // PJ
                        Text(
                            text = "${dto.pj}",
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        // GOLES (GF:GC)
                        Text(
                            text = "${dto.gf}:${dto.gc}",
                            modifier = Modifier.width(52.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        // DIF
                        val difText = if (dto.dg > 0) "${dto.dg}" else "${dto.dg}"
                        Text(
                            text = difText,
                            modifier = Modifier.width(34.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (dto.dg > 0) Color(0xFF81C784) else if (dto.dg < 0) Color(0xFFE57373) else Color.White
                        )

                        // G
                        Text(
                            text = "${dto.g}",
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        // E
                        Text(
                            text = "${dto.e}",
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        // P
                        Text(
                            text = "${dto.p}",
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        // ÚLTIMOS 5 (badges V/E/D)
                        Row(
                            modifier = Modifier.width(140.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            formList.takeLast(5).forEach { result ->
                                val (badgeBg, badgeText) = when (result) {
                                    'V' -> Color(0xFF2E7D32) to "V"
                                    'D' -> Color(0xFFC62828) to "D"
                                    else -> Color(0xFFF9A825) to "E"
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(18.dp)
                                        .background(badgeBg, RoundedCornerShape(3.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = badgeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    if (index < standings.size - 1) {
                        HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Leyenda al pie exacta al diseño de Anulo Mufo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(9.dp).background(Color(0xFF00E676), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "1.°: Campeón de Liga. Clasificado a Copa Libertadores 2027 y Supercopa Internacional 2026",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(9.dp).background(Color(0xFFFFD600), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "2.°-3.°: CONMEBOL Libertadores",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(9.dp).background(Color(0xFF00B0FF), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "4.°-9.°: CONMEBOL Sudamericana",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(9.dp).background(Color(0xFFFF1744), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "29.°-30.°: Descenso",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Equipo", modifier = Modifier.weight(1f).padding(start = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Pts", modifier = Modifier.width(44.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("PJ", modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Prom.", modifier = Modifier.width(58.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        standings.forEachIndexed { index, stats ->
            val pos = stats.pos
            val isDescenso = pos == 30 || index == standings.size - 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Barra roja lateral en el puesto 30
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .background(if (isDescenso) Color(0xFFFF1744) else Color.Transparent, RoundedCornerShape(1.5.dp))
                )

                Text(
                    text = "$pos",
                    modifier = Modifier.width(29.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!stats.logo_url.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(stats.logo_url)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = stats.team_name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isDescenso) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                }

                Text(
                    text = "${stats.total_pts}",
                    modifier = Modifier.width(44.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )

                Text(
                    text = "${stats.total_pj}",
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                val formattedPromedio = String.format(java.util.Locale.US, "%.3f", stats.promedio)
                Text(
                    text = formattedPromedio,
                    modifier = Modifier.width(58.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            if (index < standings.size - 1) {
                HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pie de tabla con leyenda
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(9.dp).background(Color(0xFFFF1744), CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "30.°: Descenso",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun GoleadoresTable(goleadoresList: List<GoleadorDto>, title: String = "Goleadores Primera División") {
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Jugador", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Equipo", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Goles", modifier = Modifier.width(48.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        goleadoresList.forEachIndexed { index, goleador ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goleador.pos}",
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = goleador.player_name,
                    modifier = Modifier.weight(1.1f).padding(end = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.weight(1.1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!goleador.logo_url.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(goleador.logo_url)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = goleador.team_name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Text(
                    text = "${goleador.goals}",
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            if (index < goleadoresList.size - 1) {
                HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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

@Composable
fun LigaZonasFullView(
    teamsByGroup: Map<String, List<Team>>,
    allMatches: List<Match>
) {
    val matchesClausura = remember(allMatches) {
        allMatches.filter { (it.tournament_id == null || it.tournament_id == 5) && isClausuraMatch(it.date) }
    }
    val matchesApertura = remember(allMatches) {
        allMatches.filter { (it.tournament_id == null || it.tournament_id == 5) && !isClausuraMatch(it.date) }
    }

    val zonaATeams = teamsByGroup["Zona A"] ?: emptyList()
    val zonaBTeams = teamsByGroup["Zona B"] ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En la parte superior: Torneo en curso (Clausura)
        item {
            LigaZonaCard(
                title = "Clausura · Grupo A",
                isEnCurso = true,
                teams = zonaATeams,
                matches = matchesClausura
            )
        }
        item {
            LigaZonaCard(
                title = "Clausura · Grupo B",
                isEnCurso = true,
                teams = zonaBTeams,
                matches = matchesClausura
            )
        }

        // En la parte inferior: Torneo Apertura (Puntajes Oficiales)
        item {
            LigaZonaCard(
                title = "Apertura · Zona A",
                isEnCurso = false,
                teams = zonaATeams,
                fixedRows = APERTURA_ZONA_A_DATA
            )
        }
        item {
            LigaZonaCard(
                title = "Apertura · Zona B",
                isEnCurso = false,
                teams = zonaBTeams,
                fixedRows = APERTURA_ZONA_B_DATA
            )
        }
    }
}

@Composable
fun LigaZonaCard(
    title: String,
    isEnCurso: Boolean,
    teams: List<Team>,
    matches: List<Match> = emptyList(),
    fixedRows: List<AperturaFixedRow>? = null
) {
    val standings = remember(teams, matches, fixedRows) {
        if (fixedRows != null) {
            fixedRows.map { row ->
                val teamMatch = teams.find { it.name.contains(row.name, ignoreCase = true) || row.name.contains(it.name, ignoreCase = true) }
                    ?: Team(id = 9000 + row.pos, name = row.name, flagUrl = "", group = "")
                com.example.worldcup2026.data.util.TeamStats(
                    team = teamMatch,
                    pj = 14,
                    g = 0,
                    e = 0,
                    p = 0,
                    gf = 0,
                    ga = 0,
                    gd = 0,
                    pts = row.pts
                )
            }
        } else {
            StandingsCalculator.calculateStandings(teams, matches, isWorldCup = false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        // Encabezado de la zona / grupo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (isEnCurso) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF76FF03), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "EN CURSO",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Encabezado de columnas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("EQUIPO", modifier = Modifier.weight(1f).padding(start = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("PTS", modifier = Modifier.width(44.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // Filas de equipos
        standings.forEachIndexed { index, stats ->
            val pos = index + 1
            val isQualified = pos in 1..8 // Octavos de final

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Barra celeste lateral en los puestos 1 al 8
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .background(if (isQualified) Color(0xFF00B0FF) else Color.Transparent, RoundedCornerShape(1.5.dp))
                )

                Text(
                    text = "$pos",
                    modifier = Modifier.width(29.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!stats.team.flagUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(stats.team.flagUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = stats.team.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isQualified) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                }

                Text(
                    text = "${stats.pts}",
                    modifier = Modifier.width(44.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            if (index < standings.size - 1) {
                HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pie de tabla con leyenda
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(9.dp).background(Color(0xFF00B0FF), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "1.°-8.°: Octavos de Final",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp
                )
            }
            Text(
                text = "Clasificación y descenso sujetos a los desempates del torneo.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp
            )
        }
    }
}

data class PlayoffLlaveItem(
    val numero: Int,
    val titulo: String,
    val localSeedText: String,
    val visitSeedText: String,
    val localTeam: Team?,
    val visitTeam: Team?,
    val match: Match? = null,
    val venueText: String = "Cancha del mejor clasificado",
    val winnerTeam: Team? = null
)

@Composable
fun LigaPlayoffsView(
    ronda: String, // "OCTAVOS", "CUARTOS", "SEMIFINAL", "FINAL"
    teamsByGroup: Map<String, List<Team>>,
    matches: List<Match>,
    onMatchClick: (Match) -> Unit = {}
) {
    val matchesClausura = remember(matches) {
        matches.filter { (it.tournament_id == null || it.tournament_id == 5) && isClausuraMatch(it.date) }
    }
    val zonaATeams = teamsByGroup["Zona A"] ?: emptyList()
    val zonaBTeams = teamsByGroup["Zona B"] ?: emptyList()

    val standingsA = remember(zonaATeams, matchesClausura) {
        StandingsCalculator.calculateStandings(zonaATeams, matchesClausura, isWorldCup = false)
    }
    val standingsB = remember(zonaBTeams, matchesClausura) {
        StandingsCalculator.calculateStandings(zonaBTeams, matchesClausura, isWorldCup = false)
    }

    // Clasificados 1 al 8 de cada zona
    val a1 = standingsA.getOrNull(0)?.team
    val a2 = standingsA.getOrNull(1)?.team
    val a3 = standingsA.getOrNull(2)?.team
    val a4 = standingsA.getOrNull(3)?.team
    val a5 = standingsA.getOrNull(4)?.team
    val a6 = standingsA.getOrNull(5)?.team
    val a7 = standingsA.getOrNull(6)?.team
    val a8 = standingsA.getOrNull(7)?.team

    val b1 = standingsB.getOrNull(0)?.team
    val b2 = standingsB.getOrNull(1)?.team
    val b3 = standingsB.getOrNull(2)?.team
    val b4 = standingsB.getOrNull(3)?.team
    val b5 = standingsB.getOrNull(4)?.team
    val b6 = standingsB.getOrNull(5)?.team
    val b7 = standingsB.getOrNull(6)?.team
    val b8 = standingsB.getOrNull(7)?.team

    // Función auxiliar para comparar equipos sin confundir nombres similares
    fun isSamePlayoffTeam(candidate: Team, matchTeam: Team?): Boolean {
        if (matchTeam == null) return false
        if (candidate.id > 0 && matchTeam.id > 0 && candidate.id == matchTeam.id) return true
        val c = normalizeTeamName(candidate.name).lowercase().trim()
        val m = normalizeTeamName(matchTeam.name).lowercase().trim()
        if (c == m) return true

        // Desambiguar Independiente vs Independiente Rivadavia
        val cRivadavia = c.contains("rivadavia")
        val mRivadavia = m.contains("rivadavia")
        if (cRivadavia != mRivadavia) return false

        // Desambiguar Central Córdoba vs Rosario Central vs Barracas Central
        val cCordoba = c.contains("cordoba") || c.contains("córdoba")
        val mCordoba = m.contains("cordoba") || m.contains("córdoba")
        if (cCordoba != mCordoba) return false

        val cRosario = c.contains("rosario")
        val mRosario = m.contains("rosario")
        if (cRosario != mRosario) return false

        val cBarracas = c.contains("barracas")
        val mBarracas = m.contains("barracas")
        if (cBarracas != mBarracas) return false

        return c.contains(m) || m.contains(c)
    }

    // Función auxiliar para buscar partido disputado o ganador
    fun resolveMatch(team1: Team?, team2: Team?): Pair<Match?, Team?> {
        if (team1 == null || team2 == null) return null to null
        val m = matches.firstOrNull { match ->
            val isRegularSeason = (match.matchday ?: 0) in 1..16
            val isPlayoff = !isRegularSeason && (
                (match.matchday ?: 0) > 16 || match.status.contains("Playoff", ignoreCase = true)
            )
            val teamsMatch = (isSamePlayoffTeam(team1, match.homeTeam) && isSamePlayoffTeam(team2, match.awayTeam)) ||
                             (isSamePlayoffTeam(team2, match.homeTeam) && isSamePlayoffTeam(team1, match.awayTeam))
            teamsMatch && isPlayoff
        }
        var winner: Team? = null
        if (m != null && !m.isFutureMatch() && (m.status.equals("Finished", ignoreCase = true) || m.status.contains("Termin", ignoreCase = true))) {
            val hScore = m.homeScore ?: 0
            val aScore = m.awayScore ?: 0
            if (hScore > aScore) {
                winner = if (isSamePlayoffTeam(team1, m.homeTeam)) team1 else team2
            } else if (aScore > hScore) {
                winner = if (isSamePlayoffTeam(team1, m.awayTeam)) team1 else team2
            } else {
                val hPen = m.homePenalties ?: 0
                val aPen = m.awayPenalties ?: 0
                if (hPen > aPen) {
                    winner = if (isSamePlayoffTeam(team1, m.homeTeam)) team1 else team2
                } else if (aPen > hPen) {
                    winner = if (isSamePlayoffTeam(team1, m.awayTeam)) team1 else team2
                }
            }
        }
        return m to winner
    }

    // 8 Llaves Oficiales de Octavos
    val octavosLlaves = remember(a1, a2, a3, a4, a5, a6, a7, a8, b1, b2, b3, b4, b5, b6, b7, b8, matches) {
        listOf(
            // Cuadro Superior
            run {
                val (m, w) = resolveMatch(a1, b8)
                PlayoffLlaveItem(1, "LLAVE 1 · CUADRO SUPERIOR", "1.° Zona A", "8.° Zona B", a1, b8, m, a1?.let { "Estadio de ${it.name}" } ?: "Cancha 1.° Zona A", w)
            },
            run {
                val (m, w) = resolveMatch(b4, a5)
                PlayoffLlaveItem(2, "LLAVE 2 · CUADRO SUPERIOR", "4.° Zona B", "5.° Zona A", b4, a5, m, b4?.let { "Estadio de ${it.name}" } ?: "Cancha 4.° Zona B", w)
            },
            run {
                val (m, w) = resolveMatch(b2, a7)
                PlayoffLlaveItem(3, "LLAVE 3 · CUADRO SUPERIOR", "2.° Zona B", "7.° Zona A", b2, a7, m, b2?.let { "Estadio de ${it.name}" } ?: "Cancha 2.° Zona B", w)
            },
            run {
                val (m, w) = resolveMatch(a3, b6)
                PlayoffLlaveItem(4, "LLAVE 4 · CUADRO SUPERIOR", "3.° Zona A", "6.° Zona B", a3, b6, m, a3?.let { "Estadio de ${it.name}" } ?: "Cancha 3.° Zona A", w)
            },
            // Cuadro Inferior
            run {
                val (m, w) = resolveMatch(b1, a8)
                PlayoffLlaveItem(5, "LLAVE 5 · CUADRO INFERIOR", "1.° Zona B", "8.° Zona A", b1, a8, m, b1?.let { "Estadio de ${it.name}" } ?: "Cancha 1.° Zona B", w)
            },
            run {
                val (m, w) = resolveMatch(a4, b5)
                PlayoffLlaveItem(6, "LLAVE 6 · CUADRO INFERIOR", "4.° Zona A", "5.° Zona B", a4, b5, m, a4?.let { "Estadio de ${it.name}" } ?: "Cancha 4.° Zona A", w)
            },
            run {
                val (m, w) = resolveMatch(a2, b7)
                PlayoffLlaveItem(7, "LLAVE 7 · CUADRO INFERIOR", "2.° Zona A", "7.° Zona B", a2, b7, m, a2?.let { "Estadio de ${it.name}" } ?: "Cancha 2.° Zona A", w)
            },
            run {
                val (m, w) = resolveMatch(b3, a6)
                PlayoffLlaveItem(8, "LLAVE 8 · CUADRO INFERIOR", "3.° Zona B", "6.° Zona A", b3, a6, m, b3?.let { "Estadio de ${it.name}" } ?: "Cancha 3.° Zona B", w)
            }
        )
    }

    // Cuartos de Final
    val cuartosLlaves = remember(octavosLlaves, matches) {
        val l1 = octavosLlaves[0]
        val l2 = octavosLlaves[1]
        val l3 = octavosLlaves[2]
        val l4 = octavosLlaves[3]
        val l5 = octavosLlaves[4]
        val l6 = octavosLlaves[5]
        val l7 = octavosLlaves[6]
        val l8 = octavosLlaves[7]

        listOf(
            run {
                val teamLocal = l1.winnerTeam ?: l1.localTeam
                val teamVisit = l2.winnerTeam ?: l2.localTeam
                val (m, w) = resolveMatch(teamLocal, teamVisit)
                PlayoffLlaveItem(1, "CUARTO 1", "Ganador Llave 1", "Ganador Llave 2", teamLocal, teamVisit, m, "Estadio del mejor posicionado", w)
            },
            run {
                val teamLocal = l3.winnerTeam ?: l3.localTeam
                val teamVisit = l4.winnerTeam ?: l4.localTeam
                val (m, w) = resolveMatch(teamLocal, teamVisit)
                PlayoffLlaveItem(2, "CUARTO 2", "Ganador Llave 3", "Ganador Llave 4", teamLocal, teamVisit, m, "Estadio del mejor posicionado", w)
            },
            run {
                val teamLocal = l5.winnerTeam ?: l5.localTeam
                val teamVisit = l6.winnerTeam ?: l6.localTeam
                val (m, w) = resolveMatch(teamLocal, teamVisit)
                PlayoffLlaveItem(3, "CUARTO 3", "Ganador Llave 5", "Ganador Llave 6", teamLocal, teamVisit, m, "Estadio del mejor posicionado", w)
            },
            run {
                val teamLocal = l7.winnerTeam ?: l7.localTeam
                val teamVisit = l8.winnerTeam ?: l8.localTeam
                val (m, w) = resolveMatch(teamLocal, teamVisit)
                PlayoffLlaveItem(4, "CUARTO 4", "Ganador Llave 7", "Ganador Llave 8", teamLocal, teamVisit, m, "Estadio del mejor posicionado", w)
            }
        )
    }

    // Semifinales
    val semiLlaves = remember(cuartosLlaves, matches) {
        val c1 = cuartosLlaves[0]
        val c2 = cuartosLlaves[1]
        val c3 = cuartosLlaves[2]
        val c4 = cuartosLlaves[3]

        listOf(
            run {
                val t1 = c1.winnerTeam ?: c1.localTeam
                val t2 = c2.winnerTeam ?: c2.localTeam
                val (m, w) = resolveMatch(t1, t2)
                PlayoffLlaveItem(1, "SEMIFINAL 1", "Ganador Cuarto 1", "Ganador Cuarto 2", t1, t2, m, "Sede Neutral", w)
            },
            run {
                val t1 = c3.winnerTeam ?: c3.localTeam
                val t2 = c4.winnerTeam ?: c4.localTeam
                val (m, w) = resolveMatch(t1, t2)
                PlayoffLlaveItem(2, "SEMIFINAL 2", "Ganador Cuarto 3", "Ganador Cuarto 4", t1, t2, m, "Sede Neutral", w)
            }
        )
    }

    // Final
    val finalLlave = remember(semiLlaves, matches) {
        val s1 = semiLlaves[0]
        val s2 = semiLlaves[1]
        val t1 = s1.winnerTeam ?: s1.localTeam
        val t2 = s2.winnerTeam ?: s2.localTeam
        val (m, w) = resolveMatch(t1, t2)
        listOf(
            PlayoffLlaveItem(1, "GRAN FINAL", "Ganador Semifinal 1", "Ganador Semifinal 2", t1, t2, m, "Sede Neutral (Ambas Hinchadas)", w)
        )
    }

    val displayLlaves = when (ronda) {
        "OCTAVOS" -> octavosLlaves
        "CUARTOS" -> cuartosLlaves
        "SEMIFINAL" -> semiLlaves
        "FINAL" -> finalLlave
        else -> octavosLlaves
    }

    val explicacionRonda = when (ronda) {
        "OCTAVOS" -> "Partido único en la cancha del equipo mejor ubicado. Empate -> suplementario de 30 min y penales."
        "CUARTOS" -> "Partido único en la cancha del mejor clasificado de la fase regular."
        "SEMIFINAL" -> "Partido único en sede neutral. En caso de igualdad: alargue y penales."
        "FINAL" -> "🏆 El club campeón del Clausura clasifica a fase de grupos de Copa Libertadores 2027 y juega el Trofeo de Campeones frente a Belgrano."
        else -> ""
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚡ $ronda DE FINAL",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Actualizado en vivo",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = explicacionRonda,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        items(displayLlaves) { llave ->
            PlayoffCard(
                llave = llave,
                onClick = { if (llave.match != null) onMatchClick(llave.match) }
            )
        }
    }
}

@Composable
fun PlayoffCard(
    llave: PlayoffLlaveItem,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = llave.match != null, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            // Header del cruce
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = llave.titulo,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val statusText = when {
                    llave.match?.isFutureMatch() == true -> "POR DISPUTARSE"
                    llave.match?.status?.equals("Finished", ignoreCase = true) == true -> "FINALIZADO"
                    llave.match?.status?.contains("Live", ignoreCase = true) == true -> "EN VIVO"
                    else -> "POR DISPUTARSE"
                }
                val statusColor = when {
                    statusText == "EN VIVO" -> Color(0xFF00E676)
                    statusText == "FINALIZADO" -> Color(0xFF64B5F6)
                    else -> Color.White.copy(alpha = 0.5f)
                }
                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Equipo 1 (Local o mejor ubicado)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!llave.localTeam?.flagUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(llave.localTeam?.flagUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = llave.localTeam?.name ?: llave.localSeedText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (llave.winnerTeam != null && llave.winnerTeam == llave.localTeam) FontWeight.Black else FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = llave.localSeedText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                val localScore = llave.match?.homeScore?.toString() ?: "-"
                Text(
                    text = localScore,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Equipo 2 (Visitante)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!llave.visitTeam?.flagUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(llave.visitTeam?.flagUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = llave.visitTeam?.name ?: llave.visitSeedText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (llave.winnerTeam != null && llave.winnerTeam == llave.visitTeam) FontWeight.Black else FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = llave.visitSeedText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                val visitScore = llave.match?.awayScore?.toString() ?: "-"
                Text(
                    text = visitScore,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sede / Estadio
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${llave.venueText}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                if (llave.match?.homePenalties != null && llave.match.awayPenalties != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Penales: ${llave.match.homePenalties} - ${llave.match.awayPenalties}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                }
            }
        }
    }
}

@Composable
fun LigaCopasYFinalesView(
    matches: List<Match>,
    teamsByGroup: Map<String, List<Team>>,
    annualStandings: List<AnnualStandingDto>,
    onMatchClick: (Match) -> Unit = {}
) {
    val allTeams = remember(teamsByGroup) {
        (teamsByGroup["Zona A"].orEmpty() + teamsByGroup["Zona B"].orEmpty()).distinctBy { it.name.trim().lowercase() }
    }
    
    val belgranoTeam = remember(allTeams) {
        allTeams.find { it.name.contains("Belgrano", ignoreCase = true) }
            ?: Team(id = 8304, name = "Belgrano (Córdoba)", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/4.png", group = "Zona B")
    }

    val bestAnnualTeam = remember(annualStandings, allTeams) {
        val topDto = annualStandings.firstOrNull()
        if (topDto != null) {
            allTeams.find { it.name.contains(topDto.team_name, ignoreCase = true) }
                ?: Team(id = topDto.team_id, name = topDto.team_name, flagUrl = topDto.logo_url, group = "Zona A")
        } else {
            allTeams.find { it.name.contains("Boca", ignoreCase = true) }
                ?: Team(id = 8305, name = "Boca Juniors", flagUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/5.png", group = "Zona A")
        }
    }

    fun findCopaMatch(keywords: List<String>, tIds: List<Int>): Match? {
        return matches.firstOrNull { m ->
            (m.tournament_id in tIds) || keywords.any { kw ->
                (m.stadium?.contains(kw, ignoreCase = true) == true) ||
                (m.city?.contains(kw, ignoreCase = true) == true)
            }
        }
    }

    val trofeoCampeonesMatch = remember(matches) { findCopaMatch(listOf("Trofeo de Campeones", "Campeones"), listOf(25, 28)) }
    val supercopaIntMatch = remember(matches) { findCopaMatch(listOf("Supercopa Internacional"), listOf(27)) }
    val supercopaArgMatch = remember(matches) { findCopaMatch(listOf("Supercopa Argentina"), listOf(26)) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🏆 COPAS Y FINALES NACIONALES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Todos los títulos y supercopas oficiales que entrega la Liga Profesional al culminar la temporada regular.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // 1. Trofeo de Campeones
        item {
            CopaFinalCard(
                title = "TROFEO DE CAMPEONES",
                subTitle = "Campeón Apertura vs. Campeón Clausura",
                team1 = belgranoTeam,
                team1Seed = "Campeón Torneo Apertura",
                team2 = null,
                team2Seed = "Campeón Torneo Clausura (A definir)",
                venue = "Estadio Madre de Ciudades (Santiago del Estero)",
                officialBadge = "Título Oficial de Liga",
                match = trofeoCampeonesMatch,
                onClick = { if (trofeoCampeonesMatch != null) onMatchClick(trofeoCampeonesMatch) }
            )
        }

        // 2. Supercopa Internacional
        item {
            CopaFinalCard(
                title = "SUPERCOPA INTERNACIONAL",
                subTitle = "Ganador Trofeo de Campeones vs. 1.° Tabla Anual",
                team1 = bestAnnualTeam,
                team1Seed = "1.° Puesto Tabla Anual Acumulada",
                team2 = null,
                team2Seed = "Campeón Trofeo de Campeones",
                venue = "Estadio Internacional / Neutral",
                officialBadge = "Clasifica por Tabla Anual",
                match = supercopaIntMatch,
                onClick = { if (supercopaIntMatch != null) onMatchClick(supercopaIntMatch) }
            )
        }

        // 3. Supercopa Argentina
        item {
            CopaFinalCard(
                title = "SUPERCOPA ARGENTINA",
                subTitle = "Campeón de Liga vs. Campeón Copa Argentina",
                team1 = null,
                team1Seed = "Campeón de Liga / Trofeo",
                team2 = null,
                team2Seed = "Campeón Copa Argentina",
                venue = "Sede Neutral (Ambas Hinchadas)",
                officialBadge = "Copa Nacional Oficial AFA",
                match = supercopaArgMatch,
                onClick = { if (supercopaArgMatch != null) onMatchClick(supercopaArgMatch) }
            )
        }

        // 4. Recopa de Campeones / Trofeo de Liga
        item {
            CopaFinalCard(
                title = "RECOPA DE CAMPEONES / TROFEO DE LIGA",
                subTitle = "Cruces y desempates clasificatorios de temporada",
                team1 = null,
                team1Seed = "Finalista / Ganador Clasificatorio",
                team2 = null,
                team2Seed = "Finalista de Temporada",
                venue = "Sede Neutral Designada por AFA",
                officialBadge = "Desempates AFA",
                match = null,
                onClick = {}
            )
        }
    }
}

@Composable
fun CopaFinalCard(
    title: String,
    subTitle: String,
    team1: Team?,
    team1Seed: String,
    team2: Team?,
    team2Seed: String,
    venue: String,
    officialBadge: String,
    match: Match? = null,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = match != null, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF00B0FF).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = officialBadge,
                        color = Color(0xFF00B0FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = subTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
            )

            // Equipo 1
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!team1?.flagUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(team1?.flagUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = team1?.name ?: team1Seed,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = team1Seed,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                val score1 = match?.homeScore?.toString() ?: "-"
                Text(
                    text = score1,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Equipo 2
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!team2?.flagUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(team2?.flagUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = team2?.name ?: team2Seed,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = team2Seed,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                val score2 = match?.awayScore?.toString() ?: "-"
                Text(
                    text = score2,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sede / Estadio
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏟️ $venue",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.weight(1f))
                val statusText = when {
                    match?.isFutureMatch() == true -> "PROGRAMADA"
                    match?.status?.equals("Finished", ignoreCase = true) == true -> "FINALIZADO"
                    match?.status?.contains("Live", ignoreCase = true) == true -> "EN VIVO"
                    else -> "PROGRAMADA"
                }
                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (statusText == "EN VIVO") Color(0xFF00E676) else Color.White.copy(alpha = 0.6f)
                )
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

    // Si los partidos ya traen su jornada/matchday asignada desde el backend, respetarla
    if (sorted.any { it.matchday != null && it.matchday > 0 }) {
        return sorted.map { m ->
            m.copy(matchday = m.matchday ?: 1)
        }
    }

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

fun getCopaArgentinaStageName(md: Int): String = when (md) {
    1 -> "32avos de Final"
    2 -> "16avos de Final"
    3 -> "Octavos de Final"
    4 -> "Cuartos de Final"
    5 -> "Semifinal"
    6 -> "Final"
    else -> "Fase $md"
}

fun getFederalAStageName(md: Int): String = when (md) {
    in 1..9 -> "2.ª Fase - Fecha $md"
    10 -> "Cuartos (1.° Ascenso)"
    11 -> "Reválida - 2.ª Etapa"
    12 -> "Semifinales (1.° Asc.)"
    13 -> "Reválida - 3.ª Etapa"
    14 -> "Final 1.° Ascenso"
    15 -> "Reválida - 4.ª Etapa"
    16 -> "Reválida - Semis"
    17 -> "Final 2.° Ascenso"
    else -> "Fase $md"
}

@Composable
fun TournamentFixtureView(
    matches: List<Match>,
    tournamentId: Int = 5,
    isLigaProfesional: Boolean = false,
    teamsByGroup: Map<String, List<Team>> = emptyMap(),
    annualStandings: List<AnnualStandingDto> = emptyList(),
    allMatches: List<Match> = emptyList(),
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

    val isCopaArgentina = remember(tournamentId, matches) {
        tournamentId == 6 || (matches.isNotEmpty() && matches.all { (it.tournament_id ?: 6) == 6 })
    }

    val isFederalA = remember(tournamentId, matches) {
        tournamentId == 15 || (matches.isNotEmpty() && matches.all { (it.tournament_id ?: 0) == 15 })
    }

    // Procesamos e inferimos las fechas reales de los partidos
    val processedMatches = remember(matches) {
        assignInferredMatchdays(matches)
    }

    // Agrupamos por Matchday / Fecha ordenado
    val matchdaysGrouped = remember(processedMatches) {
        processedMatches.groupBy { it.matchday ?: 1 }.toSortedMap()
    }

    val matchdaysList = remember(matchdaysGrouped, isCopaArgentina) {
        if (isCopaArgentina) listOf(1, 2, 3, 4, 5, 6) else matchdaysGrouped.keys.toList()
    }
    val knockoutTabs = remember(isLigaProfesional) {
        if (isLigaProfesional) {
            listOf(
                101 to "OCTAVOS",
                102 to "CUARTOS",
                103 to "SEMIFINAL",
                104 to "FINAL",
                105 to "COPAS Y FINALES"
            )
        } else emptyList()
    }
    
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
                                text = when {
                                    isCopaArgentina -> getCopaArgentinaStageName(md)
                                    isFederalA -> getFederalAStageName(md)
                                    else -> "Fecha $md"
                                },
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

                if (knockoutTabs.isNotEmpty()) {
                    items(knockoutTabs) { (code, label) ->
                        val isSelected = selectedMatchday == code
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                com.example.worldcup2026.data.util.SoundManager.playTic()
                                selectedMatchday = code
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color(0xFF00E5FF)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF),
                                containerColor = Color(0xFF00E5FF).copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        }

        if (selectedMatchday in 101..104) {
            val rondaName = when (selectedMatchday) {
                101 -> "OCTAVOS"
                102 -> "CUARTOS"
                103 -> "SEMIFINAL"
                104 -> "FINAL"
                else -> "OCTAVOS"
            }
            LigaPlayoffsView(
                ronda = rondaName,
                teamsByGroup = teamsByGroup,
                matches = allMatches,
                onMatchClick = onMatchClick
            )
        } else if (selectedMatchday == 105) {
            LigaCopasYFinalesView(
                matches = allMatches,
                teamsByGroup = teamsByGroup,
                annualStandings = annualStandings,
                onMatchClick = onMatchClick
            )
        } else {
            // Barra de navegación de Fecha y estado
            val currentIndex = matchdaysList.indexOf(selectedMatchday)
            val matchesForSelectedMatchday = remember(processedMatches, selectedMatchday, matchdaysGrouped, isCopaArgentina) {
                val baseList = matchdaysGrouped[selectedMatchday] ?: if (isCopaArgentina) emptyList() else processedMatches
                baseList.sortedBy { it.date ?: "" }
            }

            val hasLiveInMatchday = matchesForSelectedMatchday.any { 
                it.status.uppercase() in listOf("LIVE", "HALFTIME", "PAUSA", "PAUSE") 
            }
            val allFinished = matchesForSelectedMatchday.isNotEmpty() && matchesForSelectedMatchday.all { 
                !it.isFutureMatch() && it.status.uppercase() in listOf("FINISHED", "FT", "FINALIZADO") 
            }
            val stageBaseTitle = when {
                isCopaArgentina -> getCopaArgentinaStageName(selectedMatchday).uppercase()
                isFederalA -> getFederalAStageName(selectedMatchday).uppercase()
                else -> "FECHA $selectedMatchday"
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
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
                            tint = if (currentIndex > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = when {
                            hasLiveInMatchday -> "🔴 $stageBaseTitle · EN JUEGO"
                            allFinished -> "🏁 $stageBaseTitle · FINALIZADA"
                            selectedMatchday == defaultMatchday -> "⭐ $stageBaseTitle · ACTUAL"
                            else -> stageBaseTitle
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = when {
                            hasLiveInMatchday -> Color(0xFFE53935)
                            selectedMatchday == defaultMatchday -> Color(0xFFFFD700)
                            else -> MaterialTheme.colorScheme.onSurface
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
                            tint = if (currentIndex < matchdaysList.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if ((matchesForSelectedMatchday.isEmpty() || selectedMatchday == 6) && isCopaArgentina) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val finalMatch = matchesForSelectedMatchday.firstOrNull()
                    CopaFinalCard(
                        title = "GRAN FINAL · COPA ARGENTINA 2026",
                        subTitle = "Definición a partido único en sede neutral",
                        team1 = matches.flatMap { listOf(it.homeTeam, it.awayTeam) }.find { it.name.contains("Platense", ignoreCase = true) },
                        team1Seed = "Ganador Semifinal 1 (Platense)",
                        team2 = null,
                        team2Seed = "Ganador Semifinal 2 (Banfield vs. Boca Juniors)",
                        venue = "Sede Neutral (A confirmar - 2 de diciembre)",
                        officialBadge = "Final Copa Argentina",
                        match = finalMatch,
                        onClick = { finalMatch?.let(onMatchClick) }
                    )
                }
            } else {
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
    }
}

@Composable
fun FixtureMatchCard(
    match: Match,
    onClick: () -> Unit = {}
) {
    val isLive = match.isMatchLive()
    val isFinished = !match.isFutureMatch() && match.status.uppercase() in listOf("FINISHED", "FT", "FINALIZADO")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                com.example.worldcup2026.data.util.SoundManager.playTic()
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isLive) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935)) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Surface(
                    color = when {
                        isLive -> Color(0xFFE53935).copy(alpha = 0.2f)
                        isFinished -> MaterialTheme.colorScheme.surfaceVariant
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
                            isFinished -> MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.onSurface,
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
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.onSurface,
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
