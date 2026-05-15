package com.example.worldcup2026.ui

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worldcup2026.ui.CelebrationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WorldCupViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE) }
    val uiState by viewModel.uiState
    var selectedScreen by remember { mutableIntStateOf(0) }
    var isWatchingAd by remember { mutableStateOf(false) }
    var showVipDialog by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    
    val adFreeUntil = remember { prefs.getLong("ad_free_until", 0) }
    val isAdsEnabled = remember(adFreeUntil) { System.currentTimeMillis() > adFreeUntil }
    
    LaunchedEffect(uiState) {
        val state = uiState as? WorldCupUiState.Success ?: return@LaunchedEffect
        if (state.champion != null) {
            val hasShown = prefs.getBoolean("has_shown_celebration_${state.champion.id}", false)
            if (!hasShown) {
                showCelebration = true
            }
        } else {
            showCelebration = false
        }
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
                            )
                        )
                    },
                    bottomBar = {
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
                                        onShowVipStats = {
                                            isWatchingAd = true
                                        },
                                        onPredictionChange = { id, winner, home, away ->
                                            viewModel.updateMatchPrediction(id, winner, home, away)
                                        },
                                        showAds = isAdsEnabled
                                    )
                                    1 -> StandingsScreen(matches = state.matches)
                                    2 -> AboutScreen()
                                }

                                if (showVipDialog) {
                                    VipStatsDialog(onDismiss = { showVipDialog = false })
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
