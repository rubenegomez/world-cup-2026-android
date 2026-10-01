package com.example.worldcup2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.worldcup2026.data.api.AuthManager
import com.example.worldcup2026.data.api.NetworkModule
import com.example.worldcup2026.data.api.ScraperStatusItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AdminScrapersDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var statusList by remember { mutableStateOf<List<ScraperStatusItem>>(emptyList()) }
    var runningTournamentId by remember { mutableStateOf<Int?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val adminEmail = remember { AuthManager.ADMIN_EMAIL }

    fun loadStatus() {
        isLoading = true
        coroutineScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    NetworkModule.apiService.getScrapersStatus(adminEmail)
                }
                statusList = res.scrapers
            } catch (e: Exception) {
                feedbackMessage = "Error al conectar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadStatus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f))
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E676).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("PANEL ADMIN: SCRAPERS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                            Text("Control de Torneos y Scrapers", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.15f))

                // Feedback Banner
                if (feedbackMessage != null) {
                    Surface(
                        color = Color(0xFF263238),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(feedbackMessage ?: "", fontSize = 12.sp, color = Color(0xFFFFD54F), modifier = Modifier.weight(1f))
                            IconButton(onClick = { feedbackMessage = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // Botón Forzar Todos los Scrapers
                Button(
                    onClick = {
                        runningTournamentId = -1
                        coroutineScope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    NetworkModule.apiService.runScrapersAdmin(adminEmail, null)
                                }
                                feedbackMessage = "✅ Sincronización completa solicitada al servidor."
                                loadStatus()
                            } catch (e: Exception) {
                                feedbackMessage = "❌ Error: ${e.message}"
                            } finally {
                                runningTournamentId = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    enabled = runningTournamentId == null
                ) {
                    if (runningTournamentId == -1) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SINCRONIZANDO SERVIDOR...", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Black)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FORZAR ESCANEO DE TODOS LOS TORNEOS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lista de Categorías y Scrapers
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00E676))
                    }
                } else {
                    val groupedScrapers = remember(statusList) {
                        statusList.groupBy { it.category ?: "Otros Torneos" }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        groupedScrapers.forEach { (catName, scrapersInCat) ->
                            item {
                                Surface(
                                    color = Color(0xFF00E676).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp)
                                ) {
                                    Text(
                                        text = catName.uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color(0xFF00E676),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            items(scrapersInCat, key = { it.tournament_id }) { scraper ->
                                ScraperStatusCard(
                                    scraper = scraper,
                                    isRunning = runningTournamentId == scraper.tournament_id,
                                    onToggleActive = { newActive ->
                                        // Actualización optimista inmediata en la UI
                                        statusList = statusList.map {
                                            if (it.tournament_id == scraper.tournament_id) it.copy(is_active = newActive) else it
                                        }
                                        coroutineScope.launch {
                                            try {
                                                withContext(Dispatchers.IO) {
                                                    NetworkModule.apiService.toggleScraperAdmin(adminEmail, scraper.tournament_id, newActive)
                                                }
                                                feedbackMessage = "${if (newActive) "✅ Activado" else "⏸️ Pausado"}: ${scraper.tournament_name}"
                                                loadStatus()
                                            } catch (e: Exception) {
                                                feedbackMessage = "❌ Error: ${e.message}"
                                                loadStatus()
                                            }
                                        }
                                    },
                                    onRun = {
                                        runningTournamentId = scraper.tournament_id
                                        coroutineScope.launch {
                                            try {
                                                withContext(Dispatchers.IO) {
                                                    NetworkModule.apiService.runScrapersAdmin(adminEmail, scraper.tournament_id)
                                                }
                                                feedbackMessage = "✅ Scraper de ${scraper.tournament_name} ejecutado con éxito."
                                                loadStatus()
                                            } catch (e: Exception) {
                                                feedbackMessage = "❌ Error en ${scraper.tournament_name}: ${e.message}"
                                            } finally {
                                                runningTournamentId = null
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScraperStatusCard(
    scraper: ScraperStatusItem,
    isRunning: Boolean,
    onToggleActive: (Boolean) -> Unit,
    onRun: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2536))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = scraper.tournament_name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (scraper.is_active && scraper.match_count > 0) Color(0xFF00E676) else if (!scraper.is_active) Color(0xFFFF9800) else Color(0xFFFF5252),
                        modifier = Modifier.size(7.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (!scraper.is_active) "PAUSADO (${scraper.match_count} partidos)" else "${scraper.match_count} partidos en BD",
                        fontSize = 11.sp,
                        color = if (!scraper.is_active) Color(0xFFFFB74D) else Color.White.copy(alpha = 0.7f),
                        fontWeight = if (!scraper.is_active) FontWeight.Bold else FontWeight.Normal
                    )
                }
                if (!scraper.latest_date.isNullOrEmpty()) {
                    Text(
                        text = "Último escaneo: ${scraper.latest_date}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Switch Activar / Pausar
                Switch(
                    checked = scraper.is_active,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E676),
                        checkedTrackColor = Color(0xFF00E676).copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.scale(0.8f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = onRun,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3A52)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    enabled = !isRunning
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ESCANEAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
