package com.example.worldcup2026.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Fixture : Screen("fixture", "Fixture", Icons.Filled.Home)
    object Teams : Screen("teams", "Teams", Icons.Filled.List)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WorldCupViewModel = viewModel()) {
    val uiState by viewModel.uiState

    val items = listOf(
        Screen.Fixture,
        Screen.Teams
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FIFA World Cup 2026") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        // Mostramos la pantalla de equipos directamente para evitar el error de navegación
        when (uiState) {
            is WorldCupUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is WorldCupUiState.Success -> {
                val state = uiState as WorldCupUiState.Success
                Box(modifier = Modifier.padding(paddingValues)) {
                    TeamsScreen(teams = state.teams)
                }
            }
            is WorldCupUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Error loading data")
                }
            }
        }
    }
}

@Composable
fun FixtureContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Fixture & Results",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Automatic Data Sync Active", style = MaterialTheme.typography.titleMedium)
                Text("Fetching latest teams and groups...", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
