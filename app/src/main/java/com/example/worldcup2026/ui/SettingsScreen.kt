package com.example.worldcup2026.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import kotlinx.coroutines.launch
import com.example.worldcup2026.data.api.AuthManager

@Composable
fun SettingsContainer(viewModel: WorldCupViewModel) {
    var showTeamsList by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E1E1E)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (showTeamsList) {
                TeamsListScreen(viewModel = viewModel, onBack = { showTeamsList = false })
            } else {
                SettingsMenuScreen(onShowTeamsList = { showTeamsList = true }, worldCupViewModel = viewModel)
            }
        }
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit, viewModel: WorldCupViewModel) {
    var showTeamsList by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1E1E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showTeamsList) "Equipos y Selecciones" else "Ajustes",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }
                Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                if (showTeamsList) {
                    TeamsListScreen(viewModel = viewModel, onBack = { showTeamsList = false })
                } else {
                    SettingsMenuScreen(onShowTeamsList = { showTeamsList = true }, worldCupViewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SettingsMenuScreen(
    onShowTeamsList: () -> Unit,
    viewModel: ProdeViewModel = viewModel(),
    worldCupViewModel: WorldCupViewModel
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingSection(title = "Cuenta") {
                if (isAuthenticated && user != null) {
                    // Profile Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        coil.compose.AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = user.displayName ?: "Usuario", fontWeight = FontWeight.Bold)
                            Text(text = user.email ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    SettingItem(
                        icon = Icons.Default.Logout,
                        title = "Cerrar Sesión",
                        subtitle = "Desconectar tu cuenta de la nube",
                        onClick = { 
                            authManager.signOut()
                            viewModel.signOut()
                        }
                    )
                } else {
                    SettingItem(
                        icon = Icons.Default.Login,
                        title = "Iniciar Sesión con Google",
                        subtitle = "Guarda tu progreso del Prode en la nube",
                        onClick = { 
                            coroutineScope.launch {
                                val firebaseIdToken = authManager.signInWithGoogle()
                                if (firebaseIdToken != null) {
                                    viewModel.handleSignIn(firebaseIdToken)
                                }
                            }
                        }
                    )
                }
            }
        }
        item {
            SettingSection(title = "Permisos") {
                val sharedPrefs = remember { context.getSharedPreferences("world_cup_prefs", Context.MODE_PRIVATE) }
                var showNotifSettings by remember { mutableStateOf(false) }

                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    subtitle = "Configurar alertas en vivo",
                    onClick = { showNotifSettings = true }
                )
                
                if (showNotifSettings) {
                    var notifScope by remember { mutableStateOf(sharedPrefs.getString("notif_scope", "ALL") ?: "ALL") }
                    var receiveGoals by remember { mutableStateOf(sharedPrefs.getBoolean("notif_goals", true)) }
                    var receiveStart by remember { mutableStateOf(sharedPrefs.getBoolean("notif_start", true)) }
                    var receiveEnd by remember { mutableStateOf(sharedPrefs.getBoolean("notif_end", true)) }
                    var receiveYellow by remember { mutableStateOf(sharedPrefs.getBoolean("notif_yellow", true)) }
                    var receiveRed by remember { mutableStateOf(sharedPrefs.getBoolean("notif_red", true)) }
                    var receiveSubs by remember { mutableStateOf(sharedPrefs.getBoolean("notif_subs", true)) }
                    var receivePenalties by remember { mutableStateOf(sharedPrefs.getBoolean("notif_penalties", true)) }
                    var receiveExtraTime by remember { mutableStateOf(sharedPrefs.getBoolean("notif_extra_time", true)) }
                    var receiveShootout by remember { mutableStateOf(sharedPrefs.getBoolean("notif_shootout", true)) }

                    AlertDialog(
                        onDismissRequest = { showNotifSettings = false },
                        title = { Text("Configurar Notificaciones", color = Color.White, fontWeight = FontWeight.Bold) },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text("🎯 Alcance de Notificaciones:", color = Color(0xFFFFC107), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val scopes = listOf(
                                    "ALL" to "🌐 Todos los partidos y torneos",
                                    "FAV_TOURNAMENTS" to "🏆 Solo mis Torneos Favoritos",
                                    "FAV_TEAMS" to "⚽ Solo mis Equipos Favoritos",
                                    "FAV_BOTH" to "⭐ Torneos o Equipos Favoritos"
                                )
                                scopes.forEach { (scopeKey, scopeLabel) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                notifScope = scopeKey
                                                sharedPrefs.edit().putString("notif_scope", scopeKey).apply()
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = notifScope == scopeKey,
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(scopeLabel, fontSize = 12.sp, color = Color.White)
                                    }
                                }

                                Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                                Text("🔔 Tipos de Eventos / Alertas:", color = Color(0xFFFFC107), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                val eventsList = listOf(
                                    "notif_goals" to ("⚽ Goles y marcador" to receiveGoals),
                                    "notif_start" to ("🚀 Inicio de partido" to receiveStart),
                                    "notif_end" to ("🏁 Final del partido" to receiveEnd),
                                    "notif_yellow" to ("🟨 Tarjeta amarilla" to receiveYellow),
                                    "notif_red" to ("🟥 Tarjeta roja" to receiveRed),
                                    "notif_subs" to ("🔄 Cambios de jugadores" to receiveSubs),
                                    "notif_penalties" to ("🎯 Penal en partido" to receivePenalties),
                                    "notif_extra_time" to ("⏱️ Alargue / Prórroga" to receiveExtraTime),
                                    "notif_shootout" to ("🥅 Tanda de penales" to receiveShootout)
                                )

                                eventsList.forEach { (key, pair) ->
                                    val (label, isChecked) = pair
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val next = !isChecked
                                                when (key) {
                                                    "notif_goals" -> receiveGoals = next
                                                    "notif_start" -> receiveStart = next
                                                    "notif_end" -> receiveEnd = next
                                                    "notif_yellow" -> receiveYellow = next
                                                    "notif_red" -> receiveRed = next
                                                    "notif_subs" -> receiveSubs = next
                                                    "notif_penalties" -> receivePenalties = next
                                                    "notif_extra_time" -> receiveExtraTime = next
                                                    "notif_shootout" -> receiveShootout = next
                                                }
                                                sharedPrefs.edit().putBoolean(key, next).apply()
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Switch(
                                            checked = isChecked,
                                            onCheckedChange = null,
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFC107))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(label, fontSize = 13.sp, color = Color.White)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showNotifSettings = false }) {
                                Text("Aceptar", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
                            }
                        },
                        containerColor = Color(0xFF1E1E1E)
                    )
                }

                SettingItem(
                    icon = Icons.Default.BatteryAlert,
                    title = "Optimización de Batería (Sin Restricciones)",
                    subtitle = "Configurar fondo sin restricciones para no perder goles",
                    onClick = {
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        } catch (e: Exception) {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }
                    }
                )

                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Ajustes de Notificaciones del Sistema",
                    subtitle = "Abrir configuración de avisos y sonido del sistema",
                    onClick = {
                        val intent = android.content.Intent().apply {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                            } else {
                                action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
        item {
            SettingSection(title = "Personalización del Calendario") {
                val sharedPrefs = remember { context.getSharedPreferences("world_cup_prefs", Context.MODE_PRIVATE) }
                
                var currentDefaultView by remember { 
                    mutableStateOf(sharedPrefs.getString("default_calendar_view", "MONTH") ?: "MONTH") 
                }
                var showViewDialog by remember { mutableStateOf(false) }

                SettingItem(
                    icon = Icons.Default.DateRange,
                    title = "Vista por defecto del calendario",
                    subtitle = "Actual: ${
                        when(currentDefaultView) {
                            "MONTH" -> "Mes"
                            "WEEK" -> "Semana"
                            "DAY" -> "Día"
                            else -> "Mes"
                        }
                    }",
                    onClick = { showViewDialog = true }
                )
                
                if (showViewDialog) {
                    AlertDialog(
                        onDismissRequest = { showViewDialog = false },
                        title = { Text("Seleccionar vista") },
                        text = {
                            Column {
                                val views = listOf(
                                    "MONTH" to "Mes",
                                    "WEEK" to "Semana",
                                    "DAY" to "Día"
                                )
                                views.forEach { (viewKey, viewName) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                sharedPrefs.edit().putString("default_calendar_view", viewKey).apply()
                                                currentDefaultView = viewKey
                                                showViewDialog = false
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = currentDefaultView == viewKey,
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(viewName)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showViewDialog = false }) {
                                Text("Cerrar")
                            }
                        }
                    )
                }

                SettingItem(
                    icon = Icons.Default.Favorite,
                    title = "Equipos y Selecciones",
                    subtitle = "Gestiona tus favoritos y revisa la base de datos",
                    onClick = onShowTeamsList
                )
            }
        }
    }
}

@Composable
fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun TeamsListScreen(
    viewModel: WorldCupViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("world_cup_prefs", Context.MODE_PRIVATE) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var favoriteTournamentId by remember { mutableIntStateOf(sharedPrefs.getInt("favorite_tournament_id", 5)) }
    var updateTrigger by remember { mutableIntStateOf(0) }

    val uiState = viewModel.uiState.value
    val allTeamsFromVm by viewModel.allTeamsState
    val allTeams = remember(allTeamsFromVm, uiState) {
        if (allTeamsFromVm.isNotEmpty()) {
            allTeamsFromVm.filter { it.name.isNotBlank() && it.name.lowercase() != "por definir" && !it.name.contains("Ganador", ignoreCase = true) }
        } else if (uiState is WorldCupUiState.Success) {
            val matches = (uiState as WorldCupUiState.Success).matches
            matches.flatMap { listOf(it.homeTeam, it.awayTeam) }
                .filter { it.name.isNotBlank() && it.name.lowercase() != "por definir" && !it.name.contains("Ganador", ignoreCase = true) }
                .distinctBy { it.name.trim().lowercase() }
                .sortedBy { it.name }
        } else {
            emptyList()
        }
    }

    val tournamentsList = listOf(
        TournamentInfo(5, "Liga Profesional Argentina", "🏆 Liga Profesional"),
        TournamentInfo(6, "Copa Argentina", "🇦🇷 Copa Argentina"),
        TournamentInfo(7, "Primera Nacional (B)", "⚽ Primera Nacional"),
        TournamentInfo(8, "Primera B Metropolitana", "🏟️ Primera B"),
        TournamentInfo(15, "Torneo Federal A", "🏔️ Torneo Federal A"),
        TournamentInfo(10, "Primera C Metropolitana", "🥅 Primera C"),
        TournamentInfo(16, "Torneo Regional Federal Amateur", "🚩 Torneo Regional Amateur"),
        TournamentInfo(13, "Torneo Promocional Amateur", "🎖️ Promocional Amateur"),
        TournamentInfo(3, "Copa Libertadores", "🏆 Copa Libertadores"),
        TournamentInfo(4, "Copa Sudamericana", "🏆 Copa Sudamericana"),
        TournamentInfo(2, "Eliminatorias Sudamericanas", "🌎 Eliminatorias"),
        TournamentInfo(12, "Finalíssima", "🏆 Finalíssima"),
        TournamentInfo(14, "Amistosos Internacionales", "⚽ Amistosos FIFA"),
        TournamentInfo(1, "Campeonato Mundial", "🌍 Mundial 2026")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(
                text = "Equipos y Selecciones",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("🏆 Torneos", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("🇦🇷 Selecciones", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("⚽ Equipos Nac.", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("🌎 Equipos Int.", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == 0) {
            // TAB TORNEOS FAVORITOS
            val favIds = viewModel.favoriteTournamentIds.value
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tournamentsList, key = { it.id }) { t ->
                    val isFav = t.id in favIds
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                            .clickable {
                                viewModel.toggleFavoriteTournament(t.id)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFav) Color(0xFFFFC107).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = t.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Icon(
                                imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Torneo Favorito",
                                tint = if (isFav) Color(0xFFFFC107) else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // TAB EQUIPOS / SELECCIONES
            val favTeamNames = viewModel.favoriteTeamNames.value
            val nationalIds = setOf(5, 6, 7, 8, 15, 10, 16, 13)

            val filteredTeams = remember(allTeams, selectedTab) {
                when (selectedTab) {
                    1 -> allTeams.filter { t -> t.tournament_id in listOf(1, 2) || isNationalSelection(t.name) }
                    2 -> allTeams.filter { t -> (t.tournament_id in nationalIds || t.tournament_id == null) && !isNationalSelection(t.name) && isArgentineTeam(t.name) }
                    3 -> allTeams.filter { t -> (!isArgentineTeam(t.name) && !isNationalSelection(t.name)) || t.tournament_id in listOf(3, 4) }
                    else -> allTeams
                }
            }

            val tournamentOrder = listOf(5, 6, 7, 8, 15, 10, 16, 13, 1, 2, 3, 4, 12, 14)
            val tournamentNamesMap = mapOf(
                5 to "🏆 LIGA PROFESIONAL",
                6 to "🇦🇷 COPA ARGENTINA",
                7 to "⚽ PRIMERA NACIONAL",
                8 to "🏟️ PRIMERA B METROPOLITANA",
                15 to "🏔️ TORNEO FEDERAL A",
                10 to "🥅 PRIMERA C METROPOLITANA",
                16 to "🚩 TORNEO REGIONAL FEDERAL AMATEUR",
                13 to "🎖️ TORNEO PROMOCIONAL AMATEUR",
                1 to "🌍 MUNDIAL 2026",
                2 to "🌎 ELIMINATORIAS SUDAMERICANAS",
                3 to "🏆 COPA LIBERTADORES",
                4 to "🏆 COPA SUDAMERICANA"
            )

            val groupedTeams = remember(filteredTeams, favTeamNames) {
                filteredTeams.groupBy { it.tournament_id ?: 5 }
                    .mapValues { (_, teamList) ->
                        teamList.distinctBy { it.name.trim().lowercase() }
                            .sortedWith(
                                compareByDescending<com.example.worldcup2026.data.model.Team> { it.name in favTeamNames }
                                    .thenBy { it.name }
                            )
                    }
                    .entries
                    .sortedBy { (tId, _) ->
                        val idx = tournamentOrder.indexOf(tId)
                        if (idx != -1) idx else 999
                    }
            }

            if (groupedTeams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando equipos...", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedTeams.forEach { (tId, teamList) ->
                        val tTitle = tournamentNamesMap[tId] ?: "⚽ TORNEO #$tId"
                        item {
                            Text(
                                text = "$tTitle (${teamList.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
                            )
                        }
                        items(teamList, key = { "${tId}_${it.name}" }) { team ->
                            val isFavorite = team.name in favTeamNames
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 3.dp)
                                    .clickable {
                                        viewModel.toggleFavoriteTeam(team.name)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isFavorite) Color(0xFFFFC107).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (!team.flagUrl.isNullOrBlank()) {
                                            coil.compose.AsyncImage(
                                                model = team.flagUrl,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                        }
                                        Text(text = team.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    }
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Favorito",
                                        tint = if (isFavorite) Color(0xFFFFC107) else Color.Gray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class TournamentInfo(val id: Int, val fullName: String, val displayName: String)

private fun isNationalSelection(name: String): Boolean {
    val selections = listOf(
        "argentina", "brasil", "uruguay", "colombia", "chile", "perú", "ecuador", "paraguay", "venezuela", "bolivia",
        "españa", "francia", "alemania", "inglaterra", "italia", "holanda", "países bajos", "portugal", "méxico", "estados unidos"
    )
    return selections.any { name.lowercase().trim() == it }
}

private fun isArgentineTeam(name: String): Boolean {
    val nonArg = listOf(
        "flamengo", "palmeiras", "sao paulo", "gremio", "botafogo", "fluminense", "inter de porto alegre",
        "colo colo", "universidad de chile", "peñarol", "nacional", "bolivar", "the strongest", "olimpia", "libertad",
        "barcelona sc", "ldu quito", "independiente del valle"
    )
    return nonArg.none { name.lowercase().contains(it) }
}
