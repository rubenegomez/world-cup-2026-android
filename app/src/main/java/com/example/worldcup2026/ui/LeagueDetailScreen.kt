package com.example.worldcup2026.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.worldcup2026.data.api.StandingDto
import com.example.worldcup2026.data.local.LeagueEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueDetailScreen(
    league: LeagueEntity,
    viewModel: ProdeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var standings by remember { mutableStateOf<List<StandingDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    var selectedStandingForBreakdown by remember { mutableStateOf<StandingDto?>(null) }
    var breakdownList by remember { mutableStateOf<List<com.example.worldcup2026.data.api.MatchBreakdownDto>>(emptyList()) }
    var isBreakdownLoading by remember { mutableStateOf(false) }

    LaunchedEffect(league.id) {
        isLoading = true
        standings = viewModel.getStandings(league.id)
        isLoading = false
    }

    LaunchedEffect(selectedStandingForBreakdown) {
        selectedStandingForBreakdown?.let { standing ->
            isBreakdownLoading = true
            breakdownList = viewModel.getMemberBreakdown(league.id, standing.id)
            isBreakdownLoading = false
        }
    }
    
    val currentUser by viewModel.currentUser.collectAsState()

    if (selectedStandingForBreakdown != null) {
        val standing = selectedStandingForBreakdown!!
        AlertDialog(
            onDismissRequest = { selectedStandingForBreakdown = null },
            confirmButton = {
                TextButton(onClick = { selectedStandingForBreakdown = null }) {
                    Text("CERRAR", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Column {
                    Text("🔍 Desglose de Puntos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${standing.name} (${standing.points} Pts acumulados)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (isBreakdownLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (breakdownList.isEmpty()) {
                    Text("No hay partidos computados registrados aún para este participante.", color = Color.Gray, fontSize = 13.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(breakdownList) { _, item ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (item.matchday != null) "Fecha ${item.matchday}" else "Partido",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (item.points > 0) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.3f)
                                        ) {
                                            Text(
                                                text = if (item.points > 0) "+${item.points} PTS" else "0 PTS",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${item.homeTeamName} ${item.homeScore ?: "-"} - ${item.awayScore ?: "-"} ${item.awayTeamName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Pronóstico: ${item.predictedWinner ?: "-"} (${item.predictedHomeScore ?: 0}-${item.predictedAwayScore ?: 0})",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(league.name, color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        val modeDesc = when (league.mode) {
            "SINGLE_MATCHDAY" -> "Fecha ${league.startMatchday ?: 1}"
            "RANGE_MATCHDAYS" -> "Fechas ${league.startMatchday ?: 1} a ${league.endMatchday ?: 5}"
            else -> "Torneo Completo"
        }
        val isFinished = league.status?.uppercase() == "FINISHED"

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("ℹ️ Configuración y Puntuación de la Liga", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("• Modalidad: $modeDesc", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                        if (!league.customPrize.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("• 🎁 Premio Configurado: ${league.customPrize}", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• 🎯 Ganador / Empate Simple (L, E, V): +2 pts", color = Color(0xFFFFC107), fontSize = 11.sp)
                        Text("• 🎲 Apuesta Doble (ej: L+E, E+V, L+V): +1 pt", color = Color(0xFFFFC107), fontSize = 11.sp)
                        Text("• ⚽ Marcador Exacto: +3 pts", color = Color(0xFF4CAF50), fontSize = 11.sp)
                        Text("• 📐 Misma Diferencia de Gol: +2 pts", color = Color(0xFF4CAF50), fontSize = 11.sp)
                        Text("• ⭐ Comodín x2: Duplica todos los puntos del partido", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            if (!isFinished) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⏳", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                            Column {
                                Text(
                                    "LIGA EN CURSO / PARTIDOS PENDIENTES",
                                    color = Color(0xFFFF9800),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Esta liga no se cerrará ni otorgará el podio final hasta que se hayan completado todos los partidos correspondientes a la fecha (incluyendo partidos postergados o suspendidos).",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            if (isFinished && standings.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏆 LIGA FINALIZADA - PODIO FINAL", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            val winner = standings.firstOrNull()?.name ?: "-"
                            val second = standings.getOrNull(1)?.name
                            val third = standings.getOrNull(2)?.name
                            Text("🥇 1º Puesto: $winner (${standings.firstOrNull()?.points ?: 0} Pts)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (second != null) Text("🥈 2º Puesto: $second", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                            if (third != null) Text("🥉 3º Puesto: $third", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Código de Invitación", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp)
                        Text(league.code, fontWeight = FontWeight.Black, fontSize = 26.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                val directUrl = "https://ellocodelpedal.duckdns.org/join?code=${league.code}"
                                val inviteMsg = "🏆 *¡Unite a mi Liga Privada '${league.name}' en Arena Prode!*\n\n" +
                                        "👉 Tocá este enlace para unirte automáticamente:\n$directUrl\n\n" +
                                        "🔑 O ingresá el código de liga: *${league.code}*"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, inviteMsg)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartir por WhatsApp", fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tabla de Posiciones", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("💡 Tocá un jugador para ver desglose", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (standings.isEmpty()) {
                item {
                    Text("Todavía no hay posiciones para mostrar.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            } else {
                itemsIndexed(standings) { index, standing ->
                    val badgeEmoji = if (standings.size > 1) {
                        when (index) {
                            0 -> "🥇"
                            1 -> "🥈"
                            2 -> "🥉"
                            else -> null
                        }
                    } else null
                    
                    val isCurrentUser = currentUser?.fullName?.trim()?.equals(standing.name.trim(), ignoreCase = true) == true
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                com.example.worldcup2026.data.util.SoundManager.playTic()
                                selectedStandingForBreakdown = standing 
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentUser) Color(0xFF2E7D32).copy(alpha = 0.3f) else Color(0xFF1E1E1E)
                        ),
                        border = if (isCurrentUser) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.Gray,
                                modifier = Modifier.width(28.dp)
                            )
                            if (badgeEmoji != null) {
                                Text(
                                    text = badgeEmoji,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            coil.compose.AsyncImage(
                                model = coil.request.ImageRequest.Builder(LocalContext.current)
                                    .data(standing.avatar.ifBlank { "https://flagcdn.com/w160/ar.png" })
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = standing.name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                if (isCurrentUser) {
                                    Text(
                                        text = "Tú",
                                        color = Color(0xFF81C784),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFC107).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${standing.points} pts",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFC107),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
