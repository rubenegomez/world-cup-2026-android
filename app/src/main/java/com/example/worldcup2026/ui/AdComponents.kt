package com.example.worldcup2026.ui

import com.example.worldcup2026.data.model.Match
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SponsorCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder para Logo de Sponsor
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "SPONSOR OFICIAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "¡Viví el mundial con la mejor tecnología!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                
                Button(
                    onClick = { /* Ir a URL de sponsor */ },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("VER MÁS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            // Etiqueta "AD" o "ANUNCIO"
            Text(
                "AD",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun AdWatchingScreen(onComplete: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
        label = "AdProgress"
    )

    LaunchedEffect(Unit) {
        progress = 1f
        delay(3000)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "CARGANDO ANUNCIO VIP...",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.1f),
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Obteniendo estadísticas tácticas avanzadas",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

fun parsePasses(passesStr: String): Pair<String, String> {
    if (passesStr.isEmpty() || !passesStr.contains("/")) return Pair("-", "-%")
    try {
        val totalAndPct = passesStr.trim().split(" ")
        val accurateAndTotal = totalAndPct[0].split("/")
        val accurate = accurateAndTotal.getOrNull(0) ?: "-"
        val pct = totalAndPct.getOrNull(1)?.replace("(", "")?.replace(")", "") ?: "-%"
        return Pair(accurate, pct)
    } catch (e: Exception) {
        return Pair("-", "-%")
    }
}

@Composable
fun FlashscoreStatRow(homeValue: String, label: String, awayValue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Caja valor local
        Box(
            modifier = Modifier
                .width(55.dp)
                .background(Color(0xFF242424), shape = RoundedCornerShape(8.dp))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = homeValue,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Nombre de la estadística
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // Caja valor visita
        Box(
            modifier = Modifier
                .width(55.dp)
                .background(Color(0xFF242424), shape = RoundedCornerShape(8.dp))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = awayValue,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipStatsDialog(match: Match, onDismiss: () -> Unit) {
    val random = remember(match.id) { java.util.Random(match.id.toLong()) }

    // Parsear estadísticas VIP reales
    val statsMap = remember(match.vipStats) {
        match.vipStats?.split("|")?.associate {
            val parts = it.split(":")
            if (parts.size >= 2) parts[0] to parts[1] else "" to ""
        } ?: emptyMap()
    }

    val homeFouls = statsMap["fouls"]?.split(",")?.getOrNull(0) ?: "0"
    val awayFouls = statsMap["fouls"]?.split(",")?.getOrNull(1) ?: "0"
    val homeCorners = statsMap["corners"]?.split(",")?.getOrNull(0) ?: "0"
    val awayCorners = statsMap["corners"]?.split(",")?.getOrNull(1) ?: "0"
    val homeSaves = statsMap["saves"]?.split(",")?.getOrNull(0) ?: "0"
    val awaySaves = statsMap["saves"]?.split(",")?.getOrNull(1) ?: "0"
    val homeYellow = statsMap["yellow"]?.split(",")?.getOrNull(0) ?: "0"
    val awayYellow = statsMap["yellow"]?.split(",")?.getOrNull(1) ?: "0"
    val homeRed = statsMap["red"]?.split(",")?.getOrNull(0) ?: "0"
    val awayRed = statsMap["red"]?.split(",")?.getOrNull(1) ?: "0"
    val homePassesRaw = statsMap["passes"]?.split(",")?.getOrNull(0) ?: ""
    val awayPassesRaw = statsMap["passes"]?.split(",")?.getOrNull(1) ?: ""

    val (homePasses, homePassesPct) = parsePasses(homePassesRaw)
    val (awayPasses, awayPassesPct) = parsePasses(awayPassesRaw)

    // Posesión de balón
    val homePossession = match.homePossession ?: (45 + random.nextInt(16))
    val awayPossession = match.awayPossession ?: (100 - homePossession)

    // Tiros al arco
    val homeShots = match.homeShots ?: (5 + random.nextInt(11))
    val awayShots = match.awayShots ?: (5 + random.nextInt(11))

    // Goleadores
    val actualHomeScore = match.homeScore ?: 0
    val actualAwayScore = match.awayScore ?: 0
    val generatedScorers = remember(match.id, actualHomeScore, actualAwayScore) {
        if (match.scorers.isNotEmpty()) {
            match.scorers
        } else {
            val scorersList = mutableListOf<String>()
            val localRandom = java.util.Random(match.id.toLong() + 99)
            
            fun getFictionalScorer(teamName: String): String {
                val commonNames = when (teamName.lowercase()) {
                    "argentina" -> listOf("Messi", "Lautaro", "Álvarez", "Di María", "Mac Allister", "Enzo", "De Paul")
                    "brasil" -> listOf("Neymar Jr", "Vinícius Jr", "Rodrygo", "Richarlison", "Martinelli", "Raphinha", "Casemiro")
                    "méxico" -> listOf("S. Giménez", "C. Lozano", "H. Martín", "L. Chávez", "O. Pineda")
                    "españa" -> listOf("Morata", "Dani Olmo", "N. Williams", "L. Yamal", "Pedri", "Gavi", "F. Torres")
                    "francia" -> listOf("Mbappé", "Griezmann", "Giroud", "Dembélé", "Tchouaméni", "Coman", "K. Muani")
                    "alemania" -> listOf("Füllkrug", "Musiala", "Wirtz", "Havertz", "Sané", "Gnabry", "Gündogan")
                    "inglaterra" -> listOf("Harry Kane", "Bellingham", "Saka", "Foden", "Rashford", "Watkins", "Palmer")
                    "portugal" -> listOf("C. Ronaldo", "B. Fernandes", "B. Silva", "J. Félix", "R. Leão", "G. Ramos")
                    "uruguay" -> listOf("D. Núñez", "L. Suárez", "F. Valverde", "De Arrascaeta", "F. Pellistri", "Bentancur")
                    "colombia" -> listOf("Luis Díaz", "James R.", "R. Borré", "J. Durán", "L. Sinisterra", "S. Arias")
                    "estados unidos" -> listOf("Pulisic", "Weah", "Balogun", "Reyna", "McKennie", "Aaronson")
                    "canadá" -> listOf("J. David", "A. Davies", "C. Larin", "T. Buchanan", "S. Eustáquio")
                    else -> listOf("Gómez", "Rodríguez", "Smith", "Silva", "Müller", "Jones", "Dupont", "Novak", "Petrov", "Kim")
                }
                val lastName = commonNames[localRandom.nextInt(commonNames.size)]
                val minute = 1 + localRandom.nextInt(90)
                return "$lastName ($minute')"
            }

            repeat(actualHomeScore) {
                scorersList.add("⚽ ${match.homeTeam.name}: ${getFictionalScorer(match.homeTeam.name)}")
            }
            repeat(actualAwayScore) {
                scorersList.add("⚽ ${match.awayTeam.name}: ${getFictionalScorer(match.awayTeam.name)}")
            }
            scorersList
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F0F0F),
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(match.homeTeam.flagUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp))
                )
                
                Text(
                    "ESTADÍSTICA",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(match.awayTeam.flagUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp))
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${match.homeTeam.name} vs ${match.awayTeam.name}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    if (match.homeScore != null && match.awayScore != null) {
                        Text(
                            text = "${match.homeScore} x ${match.awayScore}",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Lista de estadísticas estilo Flashscore
                item {
                    FlashscoreStatRow(homeValue = homeFouls, label = "Patadas (Faltas)", awayValue = awayFouls)
                    FlashscoreStatRow(homeValue = homeShots.toString(), label = "Tiros a puerta", awayValue = awayShots.toString())
                    FlashscoreStatRow(homeValue = "$homePossession%", label = "Posesión del balón", awayValue = "$awayPossession%")
                    FlashscoreStatRow(homeValue = homePasses, label = "Pasa (Pases)", awayValue = awayPasses)
                    FlashscoreStatRow(homeValue = homePassesPct, label = "Precisión de pase", awayValue = awayPassesPct)
                    FlashscoreStatRow(homeValue = homeYellow, label = "Tarjetas amarillas", awayValue = awayYellow)
                    FlashscoreStatRow(homeValue = homeRed, label = "Tarjetas rojas", awayValue = awayRed)
                    FlashscoreStatRow(homeValue = "0", label = "Impedimentos", awayValue = "0")
                    FlashscoreStatRow(homeValue = homeCorners, label = "Esquina (Corners)", awayValue = awayCorners)
                    FlashscoreStatRow(homeValue = homeSaves, label = "Atajadas del portero", awayValue = awaySaves)
                }

                if (generatedScorers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Goleadores",
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(10.dp)
                            ) {
                                generatedScorers.forEach { scorer ->
                                    Text(
                                        scorer,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bloque de información adicional (Flashscore)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp)
                        ) {
                            // Saque central (Fecha/Hora)
                            val displayDate = match.date ?: "A definir"
                            InfoRow(" Saque central", displayDate)
                            
                            // Estadio
                            val stadium = match.stadium ?: "Estadio a Definir"
                            val city = match.city ?: ""
                            val venue = if (city.isNotEmpty()) "$stadium, $city" else stadium
                            InfoRow(" Estadio", venue)

                            // Transmisión (Solo si no finalizó)
                            if (match.status.uppercase() != "FINISHED") {
                                InfoRow(" Transmisión", "CazeTV, TV Globo, SporTV")
                            }
                            
                            // Campeonato
                            InfoRow(" Campeonato", "World Cup (World)")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var showEventsDialog by remember { mutableStateOf(false) }
                
                Button(
                    onClick = { showEventsDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "VER INCIDENCIAS EN VIVO",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
                
                if (showEventsDialog) {
                    VipEventsDialog(match = match, onDismiss = { showEventsDialog = false })
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "ENTENDIDO",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    )
}

data class ParsedEvent(
    val emoji: String,
    val minute: String,
    val team: String,
    val detail: String,
    val isHome: Boolean
)

fun parseEventString(eventStr: String, homeTeamName: String): ParsedEvent {
    try {
        val emojiMatch = if (eventStr.isNotEmpty()) {
            val firstCodePoint = eventStr.codePointAt(0)
            String(Character.toChars(firstCodePoint))
        } else ""
        val startBracket = eventStr.indexOf('[')
        val endBracket = eventStr.indexOf(']')
        val minute = if (startBracket != -1 && endBracket != -1) {
            eventStr.substring(startBracket + 1, endBracket)
        } else ""
        
        val firstColon = eventStr.indexOf(':', endBracket.coerceAtLeast(0))
        val team = if (endBracket != -1 && firstColon != -1) {
            eventStr.substring(endBracket + 1, firstColon).trim()
        } else ""
        
        val detail = if (firstColon != -1) {
            eventStr.substring(firstColon + 1).trim()
        } else eventStr
        
        val isHome = team.lowercase().trim() == homeTeamName.lowercase().trim()
        return ParsedEvent(emojiMatch, minute, team, detail, isHome)
    } catch (e: Exception) {
        return ParsedEvent("", "", "", eventStr, true)
    }
}

@Composable
fun VipEventsDialog(match: Match, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF121212)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "DETALLES EN VIVO",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color(0xFFFFD700)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Encabezado Comparativo de Equipos (Local | INCIDENCIAS | Visitante)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match.homeTeam.name.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "INCIDENCIAS",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        modifier = Modifier.width(50.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = match.awayTeam.name.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                if (match.events.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay incidencias registradas para este partido.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(match.events.size) { index ->
                            val event = match.events[index]
                            val parsed = remember(event) { parseEventString(event, match.homeTeam.name) }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Lado Local (Alineado a la Izquierda)
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (parsed.isHome) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                EventIcon(parsed.emoji)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                FormatEventDetail(parsed.detail, modifier = Modifier.weight(1f, fill = false))
                                            }
                                        }
                                    }
                                }
                                
                                // Minuto en el Centro (Eje de línea de tiempo)
                                Box(
                                    modifier = Modifier.width(50.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (parsed.minute.isNotEmpty()) "${parsed.minute}'" else "",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                                
                                // Lado Visitante (Alineado a la Derecha)
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (!parsed.isHome) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                FormatEventDetail(parsed.detail, modifier = Modifier.weight(1f, fill = false), textAlign = TextAlign.End)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                EventIcon(parsed.emoji)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CERRAR", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatRow(homeValue: String, label: String, awayValue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Valor Local
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = homeValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Métrica central
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.width(140.dp),
            textAlign = TextAlign.Center
        )
        
        // Valor Visitante
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = awayValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun EventIcon(emoji: String, modifier: Modifier = Modifier) {
    val cleanEmoji = emoji.replace("\uFE0F", "")
    when (cleanEmoji) {
        "🟨" -> {
            Box(
                modifier = modifier
                    .size(width = 11.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFFFD700)) // Amarillo tarjeta
            )
        }
        "🟥" -> {
            Box(
                modifier = modifier
                    .size(width = 11.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE53935)) // Rojo tarjeta
            )
        }
        "🔄" -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                modifier = modifier
            ) {
                Text("▲", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("▼", color = Color(0xFFE53935), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        else -> {
            Text(cleanEmoji, fontSize = 14.sp, modifier = modifier)
        }
    }
}

@Composable
fun FormatEventDetail(detail: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    val cleanText = remember(detail) {
        detail
            .replace("🟢\uFE0F", "▲")
            .replace("🟢", "▲")
            .replace("🔴\uFE0F", "▼")
            .replace("🔴", "▼")
            .replace("▲", "▲ ")
            .replace("▼", "▼ ")
    }
    val annotatedString = remember(cleanText) {
        androidx.compose.ui.text.buildAnnotatedString {
            var currentIdx = 0
            while (currentIdx < cleanText.length) {
                val greenIdx = cleanText.indexOf("▲", currentIdx)
                val redIdx = cleanText.indexOf("▼", currentIdx)
                
                val nextIdx = when {
                    greenIdx != -1 && redIdx != -1 -> minOf(greenIdx, redIdx)
                    greenIdx != -1 -> greenIdx
                    redIdx != -1 -> redIdx
                    else -> -1
                }
                
                if (nextIdx == -1) {
                    append(cleanText.substring(currentIdx))
                    break
                }
                
                append(cleanText.substring(currentIdx, nextIdx))
                
                if (nextIdx == greenIdx) {
                    pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.Black))
                    append("▲ ")
                    pop()
                    currentIdx = greenIdx + 2
                } else {
                    pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFFE53935), fontWeight = FontWeight.Black))
                    append("▼ ")
                    pop()
                    currentIdx = redIdx + 2
                }
            }
        }
    }
    Text(
        text = annotatedString,
        fontSize = 11.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        textAlign = textAlign,
        modifier = modifier
    )
}

