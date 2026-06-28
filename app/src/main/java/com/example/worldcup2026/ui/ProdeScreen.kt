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
import androidx.compose.material.icons.filled.Person
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
import com.example.worldcup2026.data.api.AuthManager
import com.example.worldcup2026.data.local.LeagueEntity
import com.example.worldcup2026.data.model.Match

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdeScreen(viewModel: ProdeViewModel = viewModel()) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d("ProdeSignIn", "Result code: ${result.resultCode}, data: ${result.data}")
        val idToken = authManager.handleSignInResult(result.data)
        android.util.Log.d("ProdeSignIn", "idToken: $idToken")
        if (idToken != null) {
            viewModel.handleSignIn(idToken)
        } else {
            android.util.Log.e("ProdeSignIn", "idToken es null - revisar SHA1 en Firebase o Web Client ID")
        }
    }

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
                        try {
                            android.util.Log.d("ProdeSignIn", "Lanzando Google Sign-In...")
                            val client = authManager.getGoogleSignInClient()
                            // Forzar re-login para obtener un token fresco
                            client.signOut().addOnCompleteListener {
                                val signInIntent = client.signInIntent
                                signInLauncher.launch(signInIntent)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ProdeSignIn", "Error al lanzar sign-in", e)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Iniciar sesión con Google", color = Color.White)
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
            
            Column(modifier = Modifier.fillMaxSize()) {
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
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Predicciones") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Ligas") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Ranking") })
                }
                
                when (selectedTab) {
                    0 -> MisPrediccionesTab(viewModel)
                    1 -> MisLigasTab(viewModel, onLeagueClick = { selectedLeague = it })
                    2 -> RankingTab(viewModel)
                }
            }
        }
    }
}

@Composable
fun MisPrediccionesTab(viewModel: ProdeViewModel) {
    val matches by viewModel.allMatches.collectAsState()
    val predictions by viewModel.predictions.collectAsState()
    // Filtramos solo los partidos no jugados
    val pendingMatches = matches.filter { it.status == "Scheduled" || it.status == "Timed" }
    
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Ingresa tus pronósticos", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(pendingMatches) { match ->
            val pred = predictions[match.id]
            val hScore = pred?.predictedHomeScore?.toString() ?: "-"
            val aScore = pred?.predictedAwayScore?.toString() ?: "-"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(match.homeTeam?.name ?: "TBD", color = Color.White, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ScoreControl(
                            score = pred?.predictedHomeScore,
                            onScoreChange = { newScore ->
                                viewModel.updatePrediction(
                                    match.id,
                                    newScore,
                                    pred?.predictedAwayScore ?: 0
                                )
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("vs", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        ScoreControl(
                            score = pred?.predictedAwayScore,
                            onScoreChange = { newScore ->
                                viewModel.updatePrediction(
                                    match.id,
                                    pred?.predictedHomeScore ?: 0,
                                    newScore
                                )
                            }
                        )
                    }
                    Text(match.awayTeam?.name ?: "TBD", color = Color.White, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            }
        }
        item {
            Button(
                onClick = {
                    viewModel.syncPredictions(predictions.values.toList())
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar Predicciones", color = Color.White)
            }
        }
    }
}

@Composable
fun MisLigasTab(viewModel: ProdeViewModel, onLeagueClick: (LeagueEntity) -> Unit) {
    val leagues by viewModel.leagues.collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var leagueNameInput by remember { mutableStateOf("") }
    var leagueCodeInput by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { showCreateDialog = true }, modifier = Modifier.weight(1f)) {
                Text("Crear Liga")
            }
            Button(onClick = { showJoinDialog = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                Text("Unirse con Código", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(leagues) { league ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onLeagueClick(league) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(league.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Código: ${league.code}", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Crear Nueva Liga", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = leagueNameInput,
                    onValueChange = { leagueNameInput = it },
                    label = { Text("Nombre de la Liga", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (leagueNameInput.isNotBlank()) {
                        viewModel.createLeague(leagueNameInput)
                        leagueNameInput = ""
                        showCreateDialog = false
                    }
                }) {
                    Text("Crear")
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

@Composable
fun ScoreControl(
    score: Int?,
    onScoreChange: (Int) -> Unit
) {
    val currentScore = score ?: 0
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color.DarkGray, androidx.compose.foundation.shape.CircleShape)
                .clickable { if (currentScore > 0) onScoreChange(currentScore - 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("-", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Text(
            text = score?.toString() ?: "-",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp).width(20.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color.DarkGray, androidx.compose.foundation.shape.CircleShape)
                .clickable { onScoreChange(currentScore + 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

