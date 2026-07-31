package com.example.worldcup2026.ui

import androidx.compose.foundation.layout.*
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
                    var receiveAll by remember { mutableStateOf(sharedPrefs.getBoolean("notif_all", true)) }
                    var receiveGoals by remember { mutableStateOf(sharedPrefs.getBoolean("notif_goals", true)) }
                    var receiveStartEnd by remember { mutableStateOf(sharedPrefs.getBoolean("notif_start_end", true)) }

                    AlertDialog(
                        onDismissRequest = { showNotifSettings = false },
                        title = { Text("Configurar Notificaciones") },
                        text = {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        receiveAll = !receiveAll
                                        sharedPrefs.edit().putBoolean("notif_all", receiveAll).apply()
                                    }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Switch(checked = receiveAll, onCheckedChange = null)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Recibir todas las notificaciones")
                                }
                                
                                if (!receiveAll) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            receiveGoals = !receiveGoals
                                            sharedPrefs.edit().putBoolean("notif_goals", receiveGoals).apply()
                                        }.padding(vertical = 8.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Switch(checked = receiveGoals, onCheckedChange = null)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Goles y eventos de score")
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            receiveStartEnd = !receiveStartEnd
                                            sharedPrefs.edit().putBoolean("notif_start_end", receiveStartEnd).apply()
                                        }.padding(vertical = 8.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Switch(checked = receiveStartEnd, onCheckedChange = null)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Inicios y finales de partido")
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showNotifSettings = false }) {
                                Text("Aceptar")
                            }
                        }
                    )
                }

                SettingItem(
                    icon = Icons.Default.BatteryAlert,
                    title = "Optimización de Batería",
                    subtitle = "Permite a la app actualizar en segundo plano",
                    onClick = { /* TODO */ }
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
    val allTeams = remember(uiState) {
        if (uiState is WorldCupUiState.Success) {
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
        TournamentInfo(6, "Primera Nacional (B)", "⚽ Primera Nacional"),
        TournamentInfo(7, "Copa Argentina", "🏆 Copa Argentina"),
        TournamentInfo(3, "Copa Libertadores", "🌎 Copa Libertadores"),
        TournamentInfo(4, "Copa Sudamericana", "🌎 Copa Sudamericana"),
        TournamentInfo(8, "Primera B Metropolitana", "⚽ Primera B"),
        TournamentInfo(9, "Primera C", "⚽ Primera C"),
        TournamentInfo(1, "Mundial FIFA 2026", "🌍 Mundial 2026"),
        TournamentInfo(2, "Eliminatorias Conmebol", "🌍 Eliminatorias")
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tournamentsList, key = { it.id }) { t ->
                    val isFav = t.id == favoriteTournamentId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                            .clickable {
                                favoriteTournamentId = t.id
                                sharedPrefs.edit().putInt("favorite_tournament_id", t.id).apply()
                                viewModel.setTournament(t.id)
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
            val checkFavorite: (com.example.worldcup2026.data.model.Team) -> Boolean = { team ->
                sharedPrefs.getBoolean("favorite_team_${team.id}", false) ||
                sharedPrefs.getBoolean("favorite_team_${team.name.lowercase().trim()}", false)
            }

            val filteredTeams = remember(allTeams, selectedTab) {
                when (selectedTab) {
                    1 -> allTeams.filter { t -> t.tournament_id in listOf(1, 2) || isNationalSelection(t.name) }
                    2 -> allTeams.filter { t -> t.tournament_id in listOf(5, 6, 7, 8, 9) && !isNationalSelection(t.name) && isArgentineTeam(t.name) }
                    3 -> allTeams.filter { t -> (!isArgentineTeam(t.name) && !isNationalSelection(t.name)) || t.tournament_id in listOf(3, 4) }
                    else -> allTeams
                }
            }

            val sortedTeams = remember(filteredTeams, updateTrigger) {
                filteredTeams.sortedWith(
                    compareByDescending<com.example.worldcup2026.data.model.Team> { checkFavorite(it) }
                        .thenBy { it.name }
                )
            }

            if (sortedTeams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando equipos...", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sortedTeams, key = { it.name }) { team ->
                        val isFavorite = remember(updateTrigger, team.id, team.name) {
                            checkFavorite(team)
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .clickable {
                                    val nextState = !isFavorite
                                    sharedPrefs.edit()
                                        .putBoolean("favorite_team_${team.id}", nextState)
                                        .putBoolean("favorite_team_${team.name.lowercase().trim()}", nextState)
                                        .apply()
                                    updateTrigger++
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = team.flagUrl,
                                    contentDescription = team.name,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = team.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorito",
                                    tint = if (isFavorite) Color(0xFFFF5252) else Color.White.copy(alpha = 0.2f),
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
