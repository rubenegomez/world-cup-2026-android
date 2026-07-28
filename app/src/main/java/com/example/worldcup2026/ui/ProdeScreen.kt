package com.example.worldcup2026.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    onNavigateToSettings: () -> Unit = {}
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Google Sign-In movido a Ajustes para unificar la UX

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
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "¡Tienes recompensas pendientes!",
                                        color = Color(0xFF4E360F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Toca aquí para reclamarlas",
                                        color = Color(0xFF4E360F).copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Reclamar",
                                    tint = Color(0xFF4E360F),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Cabecera de Perfil del Usuario Logueado
                currentUser?.let { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
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
                                        .size(44.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = user.fullName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "⏱️ Sin Anuncios (22m/pt)",
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
                                Text("SALIR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                    0 -> MisLigasTab(viewModel, onLeagueClick = { selectedLeague = it })
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
fun MisLigasTab(viewModel: ProdeViewModel, onLeagueClick: (LeagueEntity) -> Unit) {
    val leagues by viewModel.leagues.collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var leagueToDelete by remember { mutableStateOf<LeagueEntity?>(null) }

    var leagueNameInput by remember { mutableStateOf("") }
    var leagueCodeInput by remember { mutableStateOf("") }
    var customPrizeInput by remember { mutableStateOf("") }
    var selectedTournamentId by remember { mutableIntStateOf(5) } // Default: Liga Profesional 2026
    var selectedMode by remember { mutableStateOf("FULL_TOURNAMENT") }
    var startMatchday by remember { mutableIntStateOf(18) }
    var endMatchday by remember { mutableIntStateOf(22) }
    
    val tournamentOptions = listOf(
        5 to "🏆 Liga Profesional 2026",
        3 to "🌎 Copa Libertadores",
        4 to "🌐 Copa Sudamericana",
        8 to "⚽ Primera Nacional",
        6 to "🇦🇷 Copa Argentina"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { showCreateDialog = true }, modifier = Modifier.weight(1f)) {
                Text("Crear Liga 🏆")
            }
            Button(onClick = { showJoinDialog = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                Text("Unirse con Código 🔑", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (leagues.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No estás en ninguna liga privada aún.\n¡Creá una o unite con un código!", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(leagues) { league ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onLeagueClick(league) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(league.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "🏆 Liga Privada",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Código: ${league.code}", color = Color.Gray, fontSize = 13.sp)
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
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Crear Nueva Liga de Prode", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            IconButton(onClick = { if (startMatchday > 1) startMatchday-- }) {
                                Text("◀", color = Color.White, fontSize = 16.sp)
                            }
                            Text("Fecha $startMatchday", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = { if (startMatchday < 30) startMatchday++ }) {
                                Text("▶", color = Color.White, fontSize = 16.sp)
                            }
                        }
                    } else if (selectedMode == "RANGE_MATCHDAYS") {
                        Text("Rango de Fechas:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Desde:", color = Color.Gray, fontSize = 12.sp)
                            IconButton(onClick = { if (startMatchday > 1) startMatchday-- }) {
                                Text("◀", color = Color.White, fontSize = 14.sp)
                            }
                            Text("F. $startMatchday", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { if (startMatchday < endMatchday) startMatchday++ }) {
                                Text("▶", color = Color.White, fontSize = 14.sp)
                            }

                            Text("Hasta:", color = Color.Gray, fontSize = 12.sp)
                            IconButton(onClick = { if (endMatchday > startMatchday) endMatchday-- }) {
                                Text("◀", color = Color.White, fontSize = 14.sp)
                            }
                            Text("F. $endMatchday", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { if (endMatchday < 30) endMatchday++ }) {
                                Text("▶", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (leagueNameInput.isNotBlank()) {
                        val finalStart = if (selectedMode == "FULL_TOURNAMENT") 18 else startMatchday
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

