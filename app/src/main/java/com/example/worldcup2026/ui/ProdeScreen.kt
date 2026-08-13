package com.example.worldcup2026.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.draw.clip
import com.example.worldcup2026.data.api.AuthManager
import com.example.worldcup2026.data.local.LeagueEntity
import com.example.worldcup2026.data.model.Match
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdeScreen(
    viewModel: ProdeViewModel = viewModel(),
    worldCupViewModel: WorldCupViewModel? = null,
    initialJoinCode: String? = null,
    onNavigateToSettings: () -> Unit = {}
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()

    if (!isAuthenticated) {
        // Pantalla de Login
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Prode con Amigos", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Inicia sesión para competir en ligas privadas.", color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        onNavigateToSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Ir a Ajustes para Iniciar Sesión", color = Color.White)
                }
            }
        }
    } else {
        var selectedLeague by remember { mutableStateOf<LeagueEntity?>(null) }

        if (selectedLeague != null) {
            LeagueDetailScreen(
                league = selectedLeague!!,
                viewModel = viewModel,
                onBack = { selectedLeague = null }
            )
        } else {
            // Pantalla Principal del Prode
            var selectedTab by remember { mutableIntStateOf(0) }
            val currentUser by viewModel.currentUser.collectAsState()
            
            Column(modifier = Modifier.fillMaxSize()) {
                
                // Banner de Recompensas Pendientes
                if (worldCupViewModel != null) {
                    val pendingRounds by worldCupViewModel.pendingClaimableRounds
                    if (pendingRounds.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable {
                                    pendingRounds.forEach { roundId ->
                                        worldCupViewModel.claimReward(roundId)
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE5B842)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🎁 ¡Recompensa Lista!",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "Reclamar 🎁",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Header de Perfil / Status VIP
                if (currentUser != null) {
                    val user = currentUser!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                coil.compose.AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                                        .data(user.avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = user.fullName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "👋 Bienvenid@ • ⏱️ Sin Anuncios (22m/pt)",
                                        color = Color(0xFFFFC107),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            TextButton(
                                onClick = { viewModel.signOut() },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                            ) {
                                Text("Salir", fontSize = 12.sp)
                            }
                        }
                    }
                }
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
                    }
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Ligas") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Ranking") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Reglas") })
                }
                
                when (selectedTab) {
                    0 -> MisLigasTab(viewModel, worldCupViewModel, initialJoinCode, onLeagueClick = { selectedLeague = it })
                    1 -> RankingTab(viewModel)
                    2 -> ReglasTab()
                }
            }
        }
    }
}

@Composable
fun ReglasTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📜 REGLAS OFICIALES DEL PRODE", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text("📱 Participación:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("Podés participar en el Prode Global directamente o crear una Liga Privada e invitar a tus amigos compartiendo el código por WhatsApp. Tenés tiempo hasta el inicio del partido para hacer tu pronóstico; luego se deshabilita y sumás 0 Pts en ese partido.", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("⚽ Puntuación:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("• Apuesta L / E / V: Si acertás quién gana o si empatan, sumás 2 PUNTOS.", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Apuesta Resultado Exacto: Si acertás el marcador correcto (ej. 2-1), sumás 3 PUNTOS.", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🌟 ¡Doble Acierto!: Si jugaste a las dos opciones y acertaste ambas, se te convalidan 5 PUNTOS (2 + 3).", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("✌️ Apuesta Doble (Hasta 3 partidos):", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("Tenés derecho a apostar doble (ej. L y V, o L y marcador 0-1) en sólo 3 partidos a tu elección por fecha. Si acertás en esta modalidad, te asegurás 1 PUNTO.", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("🔥 Partido de la Fecha (Comodín x2):", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800), fontSize = 14.sp)
                    Text("Cada fecha tiene un partido designado con un ícono estelar 🔥. Si acertás en este partido, ¡tus puntos se DUPLICAN! (Ej: 5 Pts se cuentan como 10 Pts, 2 Pts como 4 Pts).", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("🎁 Premios Sin Publicidad:", fontWeight = FontWeight.Bold, color = Color(0xFFFFC107), fontSize = 14.sp)
                    Text("Ganas 22 MINUTOS SIN PUBLICIDAD por cada punto acumulado por fecha. Si acumulás 40 Pts en la fecha, obtendrás 880 Minutos (~14 Horas y 40 Minutos libre de anuncios).", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("🏆 Ranking e Insignias (Ligas Compartidas):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Text("Las insignias digitales (🥇 🥈 🥉) se disputan y otorgan ÚNICAMENTE en Ligas Privadas Compartidas con 2 o más jugadores. Si jugás en el Prode Solo / Individual, tu premio exclusivo es la acumulación de Tiempo Sin Publicidad por cada punto.", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MisLigasTab(
    viewModel: ProdeViewModel,
    worldCupViewModel: WorldCupViewModel? = null,
    initialJoinCode: String? = null,
    onLeagueClick: (LeagueEntity) -> Unit
) {
    val leagues by viewModel.leagues.collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var leagueToDelete by remember { mutableStateOf<LeagueEntity?>(null) }

    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager }
    var detectedClipboardCode by remember { mutableStateOf<String?>(null) }
    
    val sharedPrefs = remember { context.getSharedPreferences("world_cup_prefs", Context.MODE_PRIVATE) }
    val favTournament = remember { sharedPrefs.getInt("favorite_tournament_id", 5) }

    var leagueNameInput by remember { mutableStateOf("") }
    var leagueCodeInput by remember { mutableStateOf("") }
    var customPrizeInput by remember { mutableStateOf("") }
    var selectedTournamentId by remember { mutableStateOf<Int?>(favTournament) }
    var selectedMode by remember { mutableStateOf("FULL_TOURNAMENT") }
    var startMatchday by remember { mutableIntStateOf(1) }
    var endMatchday by remember { mutableIntStateOf(5) }
    var minActiveMatchday by remember { mutableIntStateOf(1) }
    var leaguePointsMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(leagues, currentUser) {
        viewModel.syncAllLocalPredictions()
        val newMap = mutableMapOf<String, Int>()
        leagues.forEach { l ->
            try {
                val standings = viewModel.getStandings(l.id)
                val myStanding = standings.find { 
                    it.name.trim().equals(currentUser?.fullName?.trim(), ignoreCase = true) ||
                    (currentUser?.email?.isNotBlank() == true && currentUser?.email?.startsWith(it.name, ignoreCase = true) == true)
                } ?: standings.firstOrNull()
                if (myStanding != null) {
                    newMap[l.id] = myStanding.points
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        leaguePointsMap = newMap
    }

    LaunchedEffect(initialJoinCode) {
        if (!initialJoinCode.isNullOrBlank()) {
            viewModel.joinLeague(initialJoinCode)
        }
    }

    LaunchedEffect(Unit) {
        val clip = clipboardManager?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString()?.trim() ?: ""
            val extractedCode = if (text.contains("code=")) {
                text.substringAfter("code=").substringBefore("&").substringBefore(" ").take(10)
            } else if (text.length in 4..10 && text.all { it.isLetterOrDigit() }) {
                text
            } else null
            
            if (!extractedCode.isNullOrBlank()) {
                detectedClipboardCode = extractedCode
            }
        }
    }
    
    val tournamentOptions = listOf(
        5 to "🏆 Liga Profesional",
        7 to "⚽ Primera Nacional",
        6 to "🏆 Copa Argentina",
        3 to "🌎 Copa Libertadores",
        4 to "🌎 Copa Sudamericana",
        8 to "⚽ Primera B",
        9 to "⚽ Primera C",
        13 to "⚽ Promocional Amateur",
        2 to "🌍 Eliminatorias",
        12 to "🏆 Finalíssima",
        14 to "🌍 Amistosos FIFA",
        11 to "🏆 Mundial Clubes"
    )

    LaunchedEffect(selectedTournamentId, showCreateDialog) {
        minActiveMatchday = worldCupViewModel?.getCurrentMatchdayForTournament(selectedTournamentId ?: 5) ?: 1
        startMatchday = minActiveMatchday
        endMatchday = minActiveMatchday + 4
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (detectedClipboardCode != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clickable {
                            viewModel.joinLeague(detectedClipboardCode!!)
                            detectedClipboardCode = null
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC107).copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📋 Código detectado en portapapeles:", color = Color(0xFFFFC107), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(detectedClipboardCode!!, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = {
                                viewModel.joinLeague(detectedClipboardCode!!)
                                detectedClipboardCode = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                        ) {
                            Text("Unirme", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { showCreateDialog = true }, modifier = Modifier.weight(1f)) {
                    Text("Crear Liga 🏆", fontSize = 13.sp)
                }
                Button(onClick = { showJoinDialog = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                    Text("Unirse con Código 🔑", color = Color.White, fontSize = 13.sp)
                }
            }
        }

        if (leagues.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No estás en ninguna liga privada aún.\n¡Creá una o unite con un código!", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            items(leagues) { league ->
                val myPts = leaguePointsMap[league.id] ?: 0
                val tName = tournamentOptions.find { it.first == league.tournamentId }?.second ?: "🏆 Liga General"
                val modeDesc = when (league.mode) {
                    "SINGLE_MATCHDAY" -> "📅 Fecha ${league.startMatchday ?: 1}"
                    "RANGE_MATCHDAYS" -> "📅 Fechas ${league.startMatchday ?: 1} a ${league.endMatchday ?: 5}"
                    else -> "📅 Torneo Completo"
                }
                val isFinished = league.status?.uppercase() == "FINISHED"

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onLeagueClick(league) },
                    colors = CardDefaults.cardColors(containerColor = if (isFinished) Color(0xFF2D2610) else Color(0xFF1E1E1E)),
                    border = if (isFinished) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(league.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFC107).copy(alpha = 0.2f),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "⭐ $myPts PTS",
                                        color = Color(0xFFFFC107),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$tName • $modeDesc", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Código: ${league.code}", color = Color.Gray, fontSize = 13.sp)
                            
                            if (!league.customPrize.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("🎁 Premio: ${league.customPrize}", color = Color(0xFFFF9800), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (isFinished) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("🏆 LIGA FINALIZADA", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        IconButton(onClick = { leagueToDelete = league }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Liga", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        val scrollState = rememberScrollState()
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Crear Nueva Liga de Prode", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = leagueNameInput,
                        onValueChange = { leagueNameInput = it },
                        label = { Text("Nombre de la Liga", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Seleccionar Torneo:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        tournamentOptions.chunked(2).forEach { rowTournaments ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                rowTournaments.forEach { (tId, tName) ->
                                    FilterChip(
                                        selected = selectedTournamentId == tId,
                                        onClick = { selectedTournamentId = tId },
                                        label = { Text(tName, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = customPrizeInput,
                        onValueChange = { customPrizeInput = it },
                        label = { Text("🎁 Premio (ej. Un Asado / $10.000)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFC107).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "📍 Jornada actual o próxima: Fecha $minActiveMatchday",
                            color = Color(0xFFFFC107),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Text("Modalidad de Duración:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = selectedMode == "FULL_TOURNAMENT",
                            onClick = { selectedMode = "FULL_TOURNAMENT" },
                            label = { Text("Torneo Completo", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = selectedMode == "SINGLE_MATCHDAY",
                            onClick = { selectedMode = "SINGLE_MATCHDAY" },
                            label = { Text("Fecha Única", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = selectedMode == "RANGE_MATCHDAYS",
                            onClick = { selectedMode = "RANGE_MATCHDAYS" },
                            label = { Text("Rango Fechas", fontSize = 10.sp) }
                        )
                    }

                    if (selectedMode == "SINGLE_MATCHDAY") {
                        Text("Elegir Fecha del Torneo:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { if (startMatchday > minActiveMatchday) startMatchday-- }) {
                                Text("◀", color = if (startMatchday > minActiveMatchday) Color.White else Color.Gray, fontSize = 16.sp)
                            }
                            Text("Fecha $startMatchday", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = { if (startMatchday < 38) startMatchday++ }) {
                                Text("▶", color = Color.White, fontSize = 16.sp)
                            }
                        }
                    } else if (selectedMode == "RANGE_MATCHDAYS") {
                        Text("Rango de Fechas:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Desde:", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.width(50.dp))
                                IconButton(onClick = { if (startMatchday > minActiveMatchday) startMatchday-- }) {
                                    Text("◀", color = if (startMatchday > minActiveMatchday) Color.White else Color.Gray, fontSize = 16.sp)
                                }
                                Text("Fecha $startMatchday", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                IconButton(onClick = { if (startMatchday < endMatchday) startMatchday++ }) {
                                    Text("▶", color = Color.White, fontSize = 16.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Hasta:", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.width(50.dp))
                                IconButton(onClick = { if (endMatchday > startMatchday) endMatchday-- }) {
                                    Text("◀", color = if (endMatchday > startMatchday) Color.White else Color.Gray, fontSize = 16.sp)
                                }
                                Text("Fecha $endMatchday", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                IconButton(onClick = { if (endMatchday < 38) endMatchday++ }) {
                                    Text("▶", color = Color.White, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (leagueNameInput.isNotBlank()) {
                        val finalStart = if (selectedMode == "FULL_TOURNAMENT") 1 else startMatchday
                        val finalEnd = if (selectedMode == "FULL_TOURNAMENT") null else if (selectedMode == "SINGLE_MATCHDAY") startMatchday else endMatchday

                        viewModel.createLeague(
                            name = leagueNameInput,
                            mode = selectedMode,
                            tournamentId = selectedTournamentId,
                            startMatchday = finalStart,
                            endMatchday = finalEnd,
                            customPrize = customPrizeInput.ifBlank { null }
                        )
                        leagueNameInput = ""
                        customPrizeInput = ""
                        showCreateDialog = false
                    }
                }) {
                    Text("Crear Liga 🏆")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    if (leagueToDelete != null) {
        AlertDialog(
            onDismissRequest = { leagueToDelete = null },
            title = { Text("Eliminar / Salir de Liga", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas salir o eliminar la liga '${leagueToDelete?.name}'?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        leagueToDelete?.let { l -> viewModel.deleteLeague(l.id) }
                        leagueToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { leagueToDelete = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Unirse a una Liga", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = leagueCodeInput,
                    onValueChange = { leagueCodeInput = it },
                    label = { Text("Código de la Liga", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (leagueCodeInput.isNotBlank()) {
                        viewModel.joinLeague(leagueCodeInput)
                        leagueCodeInput = ""
                        showJoinDialog = false
                    }
                }) {
                    Text("Unirse")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

@Composable
fun RankingTab(viewModel: ProdeViewModel) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("Selecciona una liga para ver el ranking", color = Color.Gray)
    }
}

