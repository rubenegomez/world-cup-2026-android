package com.example.worldcup2026.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WorldCupViewModel = viewModel()) {
    val uiState by viewModel.uiState
    var selectedScreen by remember { mutableIntStateOf(0) }
    var showSplash by remember { mutableStateOf(true) }

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
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
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
                when (uiState) {
                    is WorldCupUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is WorldCupUiState.Success -> {
                        val state = uiState as WorldCupUiState.Success
                        when (selectedScreen) {
                            0 -> FixtureScreen(
                                matches = state.matches,
                                onScoreChange = { id, home, away -> 
                                    viewModel.updateMatchScore(id, home, away)
                                },
                                onPenaltiesChange = { id, home, away ->
                                    viewModel.updateMatchPenalties(id, home, away)
                                }
                            )
                            1 -> StandingsScreen(matches = state.matches)
                            2 -> AboutScreen()
                        }
                    }
                    is WorldCupUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("Error al cargar datos")
                        }
                    }
                }
            }
        }
    }
}
