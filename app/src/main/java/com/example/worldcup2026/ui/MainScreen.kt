package com.example.worldcup2026.ui

import com.example.worldcup2026.data.model.Match
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worldcup2026.ui.CelebrationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WorldCupViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE) }
    val uiState by viewModel.uiState
    val pendingReward by viewModel.pendingRewardDialog
    var selectedScreen by remember { mutableIntStateOf(0) }
    var isWatchingAd by remember { mutableStateOf(false) }
    var showVipDialog by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    var selectedMatchForVip by remember { mutableStateOf<Match?>(null) }
    
    val adFreeUntil by viewModel.adFreeUntil
    val isAdsEnabled = remember(adFreeUntil) { System.currentTimeMillis() > adFreeUntil }
    
    LaunchedEffect(selectedScreen) {
        val screenName = when (selectedScreen) {
            0 -> "Fixture"
            1 -> "Posiciones"
            2 -> "Creditos"
            else -> "Desconocido"
        }
        com.example.worldcup2026.data.util.AnalyticsManager.logScreenView(screenName)
    }

    LaunchedEffect(uiState) {
        val state = uiState as? WorldCupUiState.Success ?: return@LaunchedEffect
        if (state.champion != null) {
            // Siempre mostramos el festejo si hay un campeón
            showCelebration = true
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
            Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f)))
 
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
                        CenterAlignedTopAppBar(
                            title = { Text("MUNDIAL 2026", fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color.White) },
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
                            }
                        )
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
                                    onClick = { selectedScreen = 0 },
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                                    label = { Text("Fixture", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedScreen == 1,
                                    onClick = { selectedScreen = 1 },
                                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                                    label = { Text("Posiciones", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedScreen == 2,
                                    onClick = { selectedScreen = 2 },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    label = { Text("Créditos", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        when (val state = uiState) {
                            is WorldCupUiState.Loading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            is WorldCupUiState.Success -> {
                                when (selectedScreen) {
                                    0 -> FixtureScreen(
                                        matches = state.matches,
                                        onScoreChange = { id, home, away -> 
                                            viewModel.updateMatchScore(id, home, away)
                                        },
                                        onPenaltiesChange = { id, home, away ->
                                            viewModel.updateMatchPenalties(id, home, away)
                                        },
                                        onStatusChange = { id, status ->
                                            viewModel.updateMatchStatus(id, status)
                                        },
                                        onShowVipStats = { match ->
                                            selectedMatchForVip = match
                                            viewModel.downloadVipStats(match.id)
                                            AdManager.showInterstitialAd(context) {
                                                showVipDialog = true
                                            }
                                        },
                                        onPredictionChange = { id, winner, home, away ->
                                            viewModel.updateMatchPrediction(id, winner, home, away)
                                        },
                                        showAds = isAdsEnabled
                                    )
                                    1 -> StandingsScreen(matches = state.matches)
                                    2 -> AboutScreen()
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
                                    val roundName = when (reward.round) {
                                        1 -> "Fecha 1 (Grupos)"
                                        2 -> "Fecha 2 (Grupos)"
                                        3 -> "Fecha 3 (Grupos)"
                                        4 -> "Dieciseisavos de Final"
                                        5 -> "Octavos de Final"
                                        6 -> "Cuartos de Final"
                                        7 -> "Semifinales"
                                        8 -> "Final"
                                        else -> "Fecha ${reward.round}"
                                    }
                                    
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
                                                text = "¡$roundName Finalizada!",
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
                    }
                }
            }
        }
    }
}
