package com.example.worldcup2026.ui

import android.app.Activity
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
        item {
            SettingSection(title = "Publicidad y Anuncios") {
                val adFreeUntil by worldCupViewModel.adFreeUntil
                val isAdFree = adFreeUntil > System.currentTimeMillis()
                val remainingMinutes = if (isAdFree) ((adFreeUntil - System.currentTimeMillis()) / 60000) + 1 else 0
                val activityContext = context as? Activity

                SettingItem(
                    icon = Icons.Default.Star,
                    title = if (isAdFree) "Modo Sin Anuncios Activo ($remainingMinutes min restantes)" else "Publicidad Habilitada",
                    subtitle = if (isAdFree) "Toca para reactivar anuncios y probar banners/videos" else "Toca para ver video y obtener +2 horas sin anuncios",
                    onClick = {
                        if (isAdFree) {
                            worldCupViewModel.resetAdFreeTime()
                            android.widget.Toast.makeText(context, "Publicidad reactivada para pruebas", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            if (activityContext != null) {
                                AdManager.showRewardedAd(activityContext) {
                                    worldCupViewModel.addAdFreeTime(2 * 60 * 60 * 1000L)
                                }
                            }
                        }
                    }
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
            val domesticLeagueIds = setOf(5, 7, 15, 8)
            val internationalClubTournamentIds = setOf(3, 4)

            val argTeamNames = remember(allTeams) {
                allTeams.filter { it.tournament_id in setOf(5, 6, 7, 8, 9, 10, 13, 15, 16, 20) }
                    .map { it.name.trim().lowercase() }
                    .toSet()
            }

            val filteredTeams = remember(allTeams, selectedTab, argTeamNames) {
                when (selectedTab) {
                    // Tab 1: Selecciones Nacionales (Mundial + Eliminatorias/Amistosos CONMEBOL)
                    1 -> allTeams.filter { t -> t.tournament_id in listOf(1, 2, 14) || isNationalSelection(t.name) }
                        .distinctBy { it.name.trim().lowercase() }
                    
                    // Tab 2: Equipos Nacionales (Liga Profesional, Primera Nacional, Torneo Federal A, Primera B Metro)
                    2 -> allTeams.filter { t -> 
                        t.tournament_id in domesticLeagueIds && !isNationalSelection(t.name)
                    }.distinctBy { normalizeTeamName(it.name) }
                    
                    // Tab 3: Equipos Internacionales (Clubes Libertadores y Sudamericana SIN equipos argentinos)
                    3 -> allTeams.filter { t -> 
                        t.tournament_id in internationalClubTournamentIds &&
                        !isNationalSelection(t.name) && 
                        !argTeamNames.contains(t.name.trim().lowercase()) &&
                        !isArgentineTeamName(t.name)
                    }.distinctBy { normalizeTeamName(it.name) }
                    
                    else -> allTeams
                }
            }

            val LPF_CANONICAL_NAMES = setOf(
                "boca juniors", "central córdoba", "defensa y justicia", "deportivo riestra",
                "estudiantes de la plata", "gimnasia y esgrima (mendoza)", "gimnasia (mendoza)", "independiente", "instituto",
                "lanús", "newell", "platense", "san lorenzo", "talleres (córdoba)", "unión (santa fe)", "union",
                "vélez", "aldosivi", "argentinos juniors", "atlético tucumán", "banfield", "barracas central",
                "belgrano", "estudiantes de río cuarto", "estudiantes (río cuarto)", "gimnasia la plata", "huracán", "independiente rivadavia",
                "racing club", "river plate", "rosario central", "sarmiento (junín)", "tigre"
            )

            val PN_CANONICAL_NAMES = setOf(
                "acassuso", "all boys", "almirante brown", "central norte", "chaco for ever",
                "ciudad de bolívar", "colón", "defensores de belgrano", "deportivo madryn",
                "deportivo morón", "estudiantes (buenos aires)", "estudiantes ba", "ferro carril oeste", "ferro", "godoy cruz",
                "los andes", "mitre", "racing (córdoba)", "san miguel", "san telmo",
                "agropecuario", "almagro", "atlanta", "atlético de rafaela", "atletico rafaela", "chacarita",
                "colegiales", "deportivo maipú", "gimnasia y esgrima (jujuy)", "gimnasia (jujuy)", "gimnasia y tiro",
                "güemes", "midland", "nueva chicago", "patronato", "quilmes",
                "san martín (san juan)", "san martín (tucumán)", "temperley", "tristán suárez"
            )

            val FA_CANONICAL_NAMES = setOf(
                "9 de julio", "alvarado", "argentino monte maíz", "atenas río cuarto", "atlético escobar",
                "bartolomé mitre", "boca unidos", "chivilcoy", "cipolletti",
                "costa brava", "círculo deportivo", "vilelas",
                "villa ramallo", "deportivo rincón", "douglas haig", "el linqueño",
                "fundación amigos", "germinal", "gimnasia de concepción",
                "guillermo brown", "huracán las heras",
                "juventud antoniana", "juventud unida universitario", "kimberley", "olimpo",
                "santamarina", "san martín de formosa", "san martín de mendoza", "sarmiento (la banda)",
                "sarmiento (resistencia)", "sol de américa", "sol de mayo", "sportivo belgrano",
                "sportivo las parejas", "tucumán central", "villa mitre"
            )

            val groupedTeams: List<Pair<String, List<com.example.worldcup2026.data.model.Team>>> = remember(filteredTeams, favTeamNames, selectedTab) {
                when (selectedTab) {
                    // Tab 1: Selecciones Nacionales (Lista única y unificada)
                    1 -> {
                        val list = filteredTeams.distinctBy { normalizeTeamName(it.name).lowercase() }
                            .sortedWith(
                                compareByDescending<com.example.worldcup2026.data.model.Team> { it.name in favTeamNames }
                                    .thenBy { it.name }
                            )
                        if (list.isNotEmpty()) listOf("🌍 SELECCIONES NACIONALES" to list) else emptyList()
                    }
                    // Tab 2: Equipos Nacionales (Separados estrictamente por categoría)
                    2 -> {
                        val distinctNational = filteredTeams.distinctBy { normalizeTeamName(it.name).lowercase() }
                        val lpf = mutableListOf<com.example.worldcup2026.data.model.Team>()
                        val pn = mutableListOf<com.example.worldcup2026.data.model.Team>()
                        val fa = mutableListOf<com.example.worldcup2026.data.model.Team>()
                        val pb = mutableListOf<com.example.worldcup2026.data.model.Team>()

                        distinctNational.forEach { team ->
                            val norm = normalizeTeamName(team.name).lowercase()
                            when {
                                LPF_CANONICAL_NAMES.any { norm.contains(it) || it.contains(norm) } || team.tournament_id == 5 -> lpf.add(team)
                                PN_CANONICAL_NAMES.any { norm.contains(it) || it.contains(norm) } || team.tournament_id == 7 -> pn.add(team)
                                FA_CANONICAL_NAMES.any { norm.contains(it) || it.contains(norm) } || team.tournament_id == 15 -> fa.add(team)
                                else -> pb.add(team)
                            }
                        }

                        val result = mutableListOf<Pair<String, List<com.example.worldcup2026.data.model.Team>>>()
                        if (lpf.isNotEmpty()) result.add("🏆 LIGA PROFESIONAL" to lpf.sortedWith(compareByDescending<com.example.worldcup2026.data.model.Team> { it.name in favTeamNames }.thenBy { it.name }))
                        if (pn.isNotEmpty()) result.add("⚽ PRIMERA NACIONAL" to pn.sortedWith(compareByDescending<com.example.worldcup2026.data.model.Team> { it.name in favTeamNames }.thenBy { it.name }))
                        if (fa.isNotEmpty()) result.add("🏔️ TORNEO FEDERAL A" to fa.sortedWith(compareByDescending<com.example.worldcup2026.data.model.Team> { it.name in favTeamNames }.thenBy { it.name }))
                        if (pb.isNotEmpty()) result.add("🏟️ PRIMERA B METROPOLITANA" to pb.sortedWith(compareByDescending<com.example.worldcup2026.data.model.Team> { it.name in favTeamNames }.thenBy { it.name }))
                        result
                    }
                    // Tab 3: Equipos Internacionales (Lista única y desduplicada)
                    3 -> {
                        val list = filteredTeams.distinctBy { normalizeTeamName(it.name).lowercase() }
                            .sortedWith(
                                compareByDescending<com.example.worldcup2026.data.model.Team> { it.name in favTeamNames }
                                    .thenBy { it.name }
                            )
                        if (list.isNotEmpty()) listOf("🏆 CLUBES INTERNACIONALES (CONMEBOL)" to list) else emptyList()
                    }
                    else -> emptyList()
                }
            }

            if (groupedTeams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando equipos...", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedTeams.forEach { (tTitle, teamList) ->
                        item {
                            Text(
                                text = "$tTitle (${teamList.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
                            )
                        }
                        items(teamList, key = { "${tTitle}_${it.name}" }) { team ->
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
        "argentina", "bolivia", "brasil", "chile", "colombia", "ecuador", "paraguay", "perú", "peru", "uruguay", "venezuela",
        "españa", "francia", "alemania", "inglaterra", "italia", "holanda", "países bajos", "portugal", "méxico", "mexico", "estados unidos", "usa"
    )
    val lower = name.lowercase().trim()
    return selections.any { lower == it || lower.startsWith("$it (") }
}

private fun isArgentineTeamName(name: String): Boolean {
    val lower = name.lowercase().trim()
    val argKeywords = listOf(
        "boca", "river", "racing", "independiente", "san lorenzo", "vélez", "estudiantes", "gimnasia",
        "talleres", "belgrano", "instituto", "rosario central", "newell", "lanús", "banfield", "huracán",
        "argentinos", "defensa y justicia", "godoy cruz", "central córdoba", "tucumán", "platense", "tigre",
        "unión", "colón", "barracas", "riestra", "sarmiento", "aldosivi", "chacarita", "ferro", "quilmes", "dock sud"
    )
    return argKeywords.any { lower.contains(it) }
}

fun normalizeTeamName(name: String): String {
    val lower = name.lowercase().trim()
    return when {
        lower.contains("dock sud") -> "Dock Sud"
        lower.contains("gimnasia") && lower.contains("jujuy") -> "Gimnasia y Esgrima (Jujuy)"
        lower.contains("gimnasia") && lower.contains("mendoza") -> "Gimnasia (Mendoza)"
        lower.contains("gimnasia") && lower.contains("tiro") -> "Gimnasia y Tiro (Salta)"
        lower.contains("gimnasia") && (lower.contains("la plata") || lower.contains("lp")) -> "Gimnasia La Plata"
        lower.contains("san martín") && lower.contains("tucumán") || lower.contains("san martin") && lower.contains("tucuman") -> "San Martín (Tucumán)"
        lower.contains("san martín") && lower.contains("juan") || lower.contains("san martin") && lower.contains("juan") -> "San Martín (San Juan)"
        lower.contains("san martín") && lower.contains("burzaco") || lower.contains("san martin") && lower.contains("burzaco") -> "San Martín (Burzaco)"
        lower.contains("racing") && lower.contains("córdoba") || lower.contains("racing") && lower.contains("cordoba") -> "Racing (Córdoba)"
        lower.contains("talleres") && lower.contains("remedios") || lower.contains("talleres re") -> "Talleres de Remedios"
        lower.contains("estudiantes") && (lower.contains("buenos aires") || lower.contains("ba")) -> "Estudiantes (Buenos Aires)"
        lower.contains("rafaela") -> "Atlético Rafaela"
        else -> name.trim()
    }
}
