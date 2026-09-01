package com.example.worldcup2026.ui

import com.example.worldcup2026.data.model.Match
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worldcup2026.ui.CelebrationScreen
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WorldCupViewModel = viewModel(),
    initialMatchId: String? = null,
    initialJoinCode: String? = null
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val livePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )
    val prefs = remember { context.getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE) }
    val uiState by viewModel.uiState
    val pendingReward by viewModel.pendingRewardDialog
    var selectedScreen by remember { mutableIntStateOf(0) }
    var selectedTournamentName by remember { mutableStateOf("Calendario de Partidos") }
    var isWatchingAd by remember { mutableStateOf(false) }
    var showVipDialog by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    var selectedTournamentForStandings by remember { mutableStateOf<Int?>(null) }
    
    var selectedMatchForVip by remember { mutableStateOf<Match?>(null) }
    
    val adFreeUntil by viewModel.adFreeUntil
    val isAdsEnabled = remember(adFreeUntil) { System.currentTimeMillis() > adFreeUntil }
    
    val celebrationMatch by viewModel.celebrationMatch
    
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == "com.example.worldcup2026.MATCH_EVENT") {
                    val matchId = intent.getIntExtra("match_id", -1)
                    val homeTeamStr = intent.getStringExtra("homeTeam") ?: "Lanús"
                    val awayTeamStr = intent.getStringExtra("awayTeam") ?: "San Lorenzo"
                    val homeScoreVal = intent.getStringExtra("homeScore")?.toIntOrNull() ?: 1
                    val awayScoreVal = intent.getStringExtra("awayScore")?.toIntOrNull() ?: 0
                    val eventTypeStr = intent.getStringExtra("eventType") ?: "goal"
                    val scorerStr = intent.getStringExtra("scorer") ?: "Edinson Cavani"
                    val minuteStr = intent.getStringExtra("minute") ?: "78"
                    val homeFlagStr = intent.getStringExtra("homeFlag") ?: "https://a.espncdn.com/i/teamlogos/soccer/500/5.png"
                    val awayFlagStr = intent.getStringExtra("awayFlag") ?: "https://a.espncdn.com/i/teamlogos/soccer/500/16.png"
                    
                    val fallbackMatch = com.example.worldcup2026.data.model.Match(
                        id = if (matchId != -1) matchId else 9999,
                        tournament_id = 5,
                        homeTeam = com.example.worldcup2026.data.model.Team(id = 991, name = homeTeamStr, flagUrl = homeFlagStr, group = "Zona A"),
                        awayTeam = com.example.worldcup2026.data.model.Team(id = 992, name = awayTeamStr, flagUrl = awayFlagStr, group = "Zona A"),
                        homeScore = homeScoreVal,
                        awayScore = awayScoreVal,
                        date = "2026-09-01",
                        status = if (eventTypeStr == "end") "Finished" else "LIVE",
                        stadium = "Estadio Alberto J. Armando (La Bombonera)",
                        scorers = listOf("$scorerStr $minuteStr'")
                    )
                    
                    if (eventTypeStr == "goal") {
                        viewModel.triggerCelebration(if (matchId != -1) matchId else 9999, fallbackMatch)
                    }
                }
            }
        }
        val filter = IntentFilter("com.example.worldcup2026.MATCH_EVENT")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    
    LaunchedEffect(selectedScreen) {
        val screenName = when (selectedScreen) {
            0 -> "Calendario"
            1 -> "Prode"
            2 -> "Acerca de"
            3 -> "Ajustes"
            else -> "Desconocido"
        }
        com.example.worldcup2026.data.util.AnalyticsManager.logScreenView(screenName)
    }

    LaunchedEffect(initialJoinCode) {
        if (!initialJoinCode.isNullOrBlank()) {
            selectedScreen = 1
        }
    }

    LaunchedEffect(initialMatchId, uiState) {
        if (initialMatchId != null && uiState is WorldCupUiState.Success) {
            val state = uiState as WorldCupUiState.Success
            val m = state.matches.find { it.id.toString() == initialMatchId }
            if (m != null) {
                selectedScreen = 0 // Navegar a la pantalla de partidos
                try {
                    val datePart = m.date?.substringBefore(" ")
                    if (!datePart.isNullOrBlank()) {
                        selectedDate = java.time.LocalDate.parse(datePart)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(uiState) {
        val state = uiState as? WorldCupUiState.Success ?: return@LaunchedEffect
        if (state.champion != null) {
            val hasShown = prefs.getBoolean("has_shown_celebration_${state.champion.id}", false)
            showCelebration = !hasShown
        } else {
            showCelebration = false
        }
    }

    LaunchedEffect(context) {
        AdManager.loadInterstitialAd(context)
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo del Estadio
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.worldcup2026.R.drawable.stadium_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alpha = 0.6f
            )
            
            // Capa de oscurecimiento para legibilidad
            Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.82f)))
 
            if (uiState is WorldCupUiState.Success && (uiState as WorldCupUiState.Success).champion != null && showCelebration) {
                // Pantalla de Celebración EXCLUYENTE
                val state = uiState as WorldCupUiState.Success
                CelebrationScreen(
                    champion = state.champion!!,
                    onDismiss = { 
                        showCelebration = false 
                        prefs.edit().putBoolean("has_shown_celebration_${state.champion.id}", true).apply()
                    }
                )
            } else if (isWatchingAd) {
                AdWatchingScreen(onComplete = {
                    isWatchingAd = false
                    showVipDialog = true
                })
            } else {
                // Scaffold normal solo si no hay festejo ni anuncio
                Scaffold(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    topBar = {
                        if (selectedScreen != -1) {
                            val hasLiveMatches = viewModel.liveTournaments.value.any { it.value }
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Siempre mostramos el título general, excepto en vistas anidadas si quisiéramos botón atrás
                                        if (selectedScreen == 4 || selectedScreen == 5) {
                                            IconButton(onClick = { selectedScreen = 0 }) {
                                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                                            }
                                        }
                                        Text(
                                            text = "ARENA PRODE",
                                            fontWeight = FontWeight.Black,
                                            color = androidx.compose.ui.graphics.Color.White,
                                            fontSize = 18.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                        if (hasLiveMatches) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF4CAF50).copy(alpha = livePulseAlpha))
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "EN VIVO",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF4CAF50),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ),
                                actions = {
                                    if (isRefreshing) {
                                        Box(modifier = Modifier.padding(end = 16.dp)) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp,
                                                    color = androidx.compose.ui.graphics.Color.White
                                                )
                                            }
                                        } else {
                                            IconButton(
                                                onClick = {
                                                    isRefreshing = true
                                                    viewModel.syncLiveResults { success ->
                                                        isRefreshing = false
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            if (success) "Resultados en vivo actualizados" else "Error al sincronizar resultados",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Sincronizar resultados",
                                                    tint = androidx.compose.ui.graphics.Color.White
                                                )
                                            }
                                        }
                                        IconButton(onClick = { 
                                            com.example.worldcup2026.data.util.SoundManager.playTic()
                                            selectedScreen = 3 
                                        }) {
                                            Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = androidx.compose.ui.graphics.Color.White)
                                        }
                                    }
                                )
                            }
                        },
                    bottomBar = {
                        Column {
                            if (isAdsEnabled) {
                                    AdmobBanner()
                                }
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
                                    contentColor = androidx.compose.ui.graphics.Color.White
                                ) {
                                    NavigationBarItem(
                                        selected = selectedScreen == 0,
                                        onClick = { 
                                            com.example.worldcup2026.data.util.SoundManager.playTic()
                                            selectedScreen = 0 
                                        },
                                        icon = { Icon(Icons.Default.Today, contentDescription = null) },
                                        label = { Text("Calendario", style = MaterialTheme.typography.labelSmall) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = selectedScreen == 1,
                                        onClick = { 
                                            com.example.worldcup2026.data.util.SoundManager.playTic()
                                            selectedScreen = 1 
                                        },
                                        icon = { Icon(Icons.Default.Star, contentDescription = null) },
                                        label = { Text("Prode", style = MaterialTheme.typography.labelSmall) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = selectedScreen == 2,
                                        onClick = { 
                                            com.example.worldcup2026.data.util.SoundManager.playTic()
                                            selectedScreen = 2 
                                        },
                                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                                        label = { Text("Acerca de", style = MaterialTheme.typography.labelSmall) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                                        )
                                    )
                            }
                        }
                    }
                ) { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        val isConnected by viewModel.isServerConnected
                        if (!isConnected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE53935).copy(alpha = 0.15f))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Desconectado",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sin conexión con el servidor. Datos sin actualizar.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE53935),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Box(modifier = Modifier.weight(1f)) {
                            when (val state = uiState) {
                            is WorldCupUiState.Loading -> {
                                SplashScreen(isLoading = true)
                            }
                            is WorldCupUiState.Success -> {
                                when (selectedScreen) {
                                    0 -> CalendarScreen(
                                        viewModel = viewModel,
                                        matches = state.matches,
                                        onNavigateToMatches = { date ->
                                            selectedDate = date
                                            selectedScreen = 4
                                        }
                                    )
                                    1 -> ProdeScreen(worldCupViewModel = viewModel, initialJoinCode = initialJoinCode, onNavigateToSettings = { selectedScreen = 3 })
                                    2 -> AboutScreen()
                                    3 -> SettingsContainer(viewModel)
                                    4 -> {
                                        if (selectedDate != null) {
                                            DailyMatchesScreen(
                                                date = selectedDate!!,
                                                matches = state.matches,
                                                viewModel = viewModel,
                                                onNavigateToTournament = { id ->
                                                    selectedTournamentForStandings = id
                                                    selectedScreen = 5
                                                },
                                                onShowVipStats = { match ->
                                                    selectedMatchForVip = match
                                                    if (isAdsEnabled) {
                                                        com.example.worldcup2026.ui.AdManager.showInterstitialAd(context) {
                                                            showVipDialog = true
                                                        }
                                                    } else {
                                                        showVipDialog = true
                                                    }
                                                }
                                            )
                                        } else {
                                            selectedScreen = 0
                                        }
                                    }
                                     5 -> {
                                         if (selectedTournamentForStandings != null) {
                                             StandingsScreen(
                                                 matches = state.matches,
                                                 initialTournamentId = selectedTournamentForStandings
                                             )
                                         } else {
                                         }
                                     }
                                 }

                                  if (celebrationMatch != null) {
                                      val match = celebrationMatch!!
                                      if (match.status.equals("Finished", ignoreCase = true)) {
                                          com.example.worldcup2026.ui.GoalCelebrationDialog(
                                              match = match,
                                              onDismiss = { viewModel.dismissCelebration() }
                                          )
                                      } else {
                                          com.example.worldcup2026.ui.GoalCelebrationOverlay(
                                              match = match,
                                              onDismiss = { viewModel.dismissCelebration() }
                                          )
                                      }
                                  }

                                 if (showVipDialog && selectedMatchForVip != null) {
                                     VipStatsDialog(
                                         match = selectedMatchForVip!!,
                                         onDismiss = { 
                                             showVipDialog = false
                                             selectedMatchForVip = null
                                         }
                                     )
                                 }

                                if (pendingReward != null) {
                                    val reward = pendingReward!!
                                    
                                    AlertDialog(
                                        onDismissRequest = { viewModel.dismissRewardDialog(reward.round) },
                                        containerColor = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = androidx.compose.ui.graphics.Color(0xFFFFD700),
                                                modifier = Modifier.size(40.dp)
                                            )
                                        },
                                        title = {
                                            Text(
                                                text = "¡Resultados Procesados!",
                                                fontWeight = FontWeight.Black,
                                                color = androidx.compose.ui.graphics.Color.White,
                                                fontSize = 20.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        },
                                        text = {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                            ) {
                                                if (reward.points > 0) {
                                                    Text(
                                                        text = "Acertaste y sumaste",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "${reward.points} PUNTOS",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 24.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "¡Obtuviste ${reward.hours} horas sin publicidad como recompensa!",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = androidx.compose.ui.graphics.Color.White,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                } else {
                                                    Text(
                                                        text = "No lograste sumar puntos en el Prode para esta fecha.",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "¡Mucha suerte en la siguiente fecha! Sigues participando.",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = androidx.compose.ui.graphics.Color.White,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = { viewModel.dismissRewardDialog(reward.round) },
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = if (reward.points > 0) "¡EXCELENTE!" else "ENTENDIDO",
                                                    color = androidx.compose.ui.graphics.Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                            is WorldCupUiState.Error -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Text("Error al cargar datos", color = androidx.compose.ui.graphics.Color.White)
                                }
                            }
                        }

                        val updateInfo by viewModel.appUpdateInfo
                        if (updateInfo != null) {
                            UpdateAvailableDialog(
                                updateInfo = updateInfo!!,
                                onDismiss = { viewModel.dismissUpdateDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}
}
