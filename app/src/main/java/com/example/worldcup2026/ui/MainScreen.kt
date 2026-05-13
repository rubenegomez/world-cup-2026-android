package com.example.worldcup2026.ui

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WorldCupViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("world_cup_prefs", android.content.Context.MODE_PRIVATE) }
    val uiState by viewModel.uiState
    var selectedScreen by remember { mutableIntStateOf(0) }
    var showSplash by remember { mutableStateOf(true) }
    var showVipDialog by remember { mutableStateOf(false) }
    
    // Almacenamos si ya se mostró el festejo para el campeón actual
    var showCelebration by remember { 
        mutableStateOf(false) 
    }

    LaunchedEffect(uiState) {
        val state = uiState as? WorldCupUiState.Success ?: return@LaunchedEffect
        if (state.champion != null) {
            val lastChampionId = prefs.getInt("last_champion_id", -1)
            val hasShown = prefs.getBoolean("has_shown_celebration_${state.champion.id}", false)
            
            if (!hasShown || lastChampionId != state.champion.id) {
                showCelebration = true
                prefs.edit().putInt("last_champion_id", state.champion.id).apply()
            }
        } else {
            showCelebration = false
            // Si no hay campeón, buscamos cuál fue el último y le quitamos la marca de "mostrado"
            // para que cuando lo vuelvan a finalizar, salte de nuevo.
            val lastId = prefs.getInt("last_champion_id", -1)
            if (lastId != -1) {
                prefs.edit()
                    .putBoolean("has_shown_celebration_$lastId", false)
                    .putInt("last_champion_id", -1)
                    .apply()
            }
        }
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("MUNDIAL 2026", fontWeight = FontWeight.Black) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedScreen == 0,
                        onClick = { selectedScreen = 0 },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        label = { Text("Fixture", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationBarItem(
                        selected = selectedScreen == 1,
                        onClick = { selectedScreen = 1 },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("Posiciones", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationBarItem(
                        selected = selectedScreen == 2,
                        onClick = { selectedScreen = 2 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Créditos", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (val state = uiState) {
                    is WorldCupUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is WorldCupUiState.Success -> {
                        if (state.champion != null && showCelebration) {
                            CelebrationScreen(
                                champion = state.champion,
                                onDismiss = { 
                                    showCelebration = false 
                                    prefs.edit().putBoolean("has_shown_celebration_${state.champion.id}", true).apply()
                                }
                            )
                        } else {
                            if (state.champion == null) {
                                showCelebration = true
                            }
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
                                        showVipDialog = true
                                    },
                                    onPredictionChange = { id, winner, home, away ->
                                        viewModel.updateMatchPrediction(id, winner, home, away)
                                    }
                                )
                                1 -> StandingsScreen(matches = state.matches)
                                2 -> AboutScreen()
                            }
                        }

                        if (showVipDialog) {
                            VipStatsDialog(onDismiss = { showVipDialog = false })
                        }
                    }
                    is WorldCupUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("Error: ${state.message}")
                        }
                    }
                }
            }
        }
    }
}
