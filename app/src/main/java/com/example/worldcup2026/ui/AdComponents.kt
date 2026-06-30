package com.example.worldcup2026.ui

import com.example.worldcup2026.data.model.Match
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun PremiumStatBar(
    label: String,
    homeValue: Int,
    awayValue: Int,
    homeString: String,
    awayString: String,
    homeColor: Color = Color(0xFF1E88E5), // Azul premium para local
    awayColor: Color = Color(0xFFE53935)  // Rojo premium para visitante
) {
    val total = (homeValue + awayValue).coerceAtLeast(1)
    val homeWeight = homeValue.toFloat() / total.toFloat()
    val awayWeight = awayValue.toFloat() / total.toFloat()

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = homeString,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp
            )
            Text(
                text = label.uppercase(),
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                text = awayString,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            if (homeWeight > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(homeWeight)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(homeColor, homeColor.copy(alpha = 0.7f))
                            )
                        )
                )
            }
            if (awayWeight > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(awayWeight)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(awayColor.copy(alpha = 0.7f), awayColor)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun PenaltyBall(isGoal: Boolean?) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(
                when (isGoal) {
                    true -> Color(0xFF2E7D32)  // Verde esmeralda para gol
                    false -> Color(0xFFC62828) // Rojo carmín para errado
                    null -> Color.Transparent  // Vacío
                }
            )
            .border(
                width = 1.dp,
                color = if (isGoal == null) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                shape = androidx.compose.foundation.shape.CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        when (isGoal) {
            true -> Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            false -> Text("✗", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            null -> { /* Vacío */ }
        }
    }
}

data class ParsedPenaltyTaker(val name: String, val team: String, val isGoal: Boolean)

@Composable
fun PenaltyShootoutView(
    homeTeamName: String,
    awayTeamName: String,
    homePenalties: Int,
    awayPenalties: Int,
    takers: List<ParsedPenaltyTaker>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TANDA DE PENALES",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = Color(0xFFFFD700),
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$homeTeamName ($homePenalties)  -  ($awayPenalties) $awayTeamName",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Lista de Tiradores alternados
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                takers.forEach { taker ->
                    val isHome = taker.team.lowercase().trim() == homeTeamName.lowercase().trim()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isHome) {
                            // Lado Local
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                PenaltyBall(isGoal = taker.isGoal)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = taker.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f)) // Espacio vacío en lado visitante
                        } else {
                            // Lado Visitante
                            Spacer(modifier = Modifier.weight(1f)) // Espacio vacío en lado local
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = taker.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                PenaltyBall(isGoal = taker.isGoal)
                            }
                        }
                    }
                }
            }
        }
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



    val detailedPenalties = remember(match.id, match.events, match.homePenalties, match.awayPenalties) {
        val list = mutableListOf<ParsedPenaltyTaker>()
        val penEvents = match.events.filter { it.contains("tanda de penales", ignoreCase = true) || it.contains("penales", ignoreCase = true) }
        if (penEvents.isNotEmpty()) {
            penEvents.forEach { ev ->
                val cleanEv = ev.replace("[Penales]", "", ignoreCase = true)
                    .replace("[Tanda de penales]", "", ignoreCase = true)
                    .trim()
                val firstAlphaIndex = cleanEv.indexOfFirst { it.isLetterOrDigit() }
                val cleanPrefix = if (firstAlphaIndex != -1) cleanEv.substring(firstAlphaIndex) else cleanEv
                val colonIdx = cleanPrefix.indexOf(':')
                if (colonIdx != -1) {
                    val teamPart = cleanPrefix.substring(0, colonIdx).trim()
                    val playerPart = cleanPrefix.substring(colonIdx + 1).substringBefore("(").trim()
                    val isGoal = !ev.contains("❌") && !ev.contains("errado", ignoreCase = true) && !ev.contains("fallado", ignoreCase = true)
                    if (teamPart.isNotEmpty() && playerPart.isNotEmpty()) {
                        list.add(ParsedPenaltyTaker(playerPart, teamPart, isGoal))
                    }
                }
            }
        }
        if (list.isEmpty() && match.homePenalties != null && match.awayPenalties != null) {
            val homeTakers = when (match.homeTeam.name.lowercase()) {
                "alemania" -> listOf("Füllkrug", "Musiala", "Havertz", "Wirtz", "Kimmich", "Gündogan")
                "argentina" -> listOf("Messi", "Lautaro", "Álvarez", "Mac Allister", "Enzo Fernández", "Paredes")
                else -> listOf("Jugador L1", "Jugador L2", "Jugador L3", "Jugador L4", "Jugador L5", "Jugador L6")
            }
            val awayTakers = when (match.awayTeam.name.lowercase()) {
                "paraguay" -> listOf("Gómez", "Almirón", "Enciso", "Sanabria", "Romero", "Arzamendia")
                "brasil" -> listOf("Neymar Jr", "Vinícius Jr", "Rodrygo", "Raphinha", "Casemiro", "Paquetá")
                else -> listOf("Jugador V1", "Jugador V2", "Jugador V3", "Jugador V4", "Jugador V5", "Jugador V6")
            }
            val homeGoalsCount = match.homePenalties
            val awayGoalsCount = match.awayPenalties
            
            val homeGoalsList = mutableListOf<Boolean>()
            repeat(homeGoalsCount) { homeGoalsList.add(true) }
            repeat((5 - homeGoalsCount).coerceAtLeast(1)) { homeGoalsList.add(false) }
            val randomL = java.util.Random(match.id.toLong() + 500)
            homeGoalsList.shuffle(randomL)

            val awayGoalsList = mutableListOf<Boolean>()
            repeat(awayGoalsCount) { awayGoalsList.add(true) }
            repeat((5 - awayGoalsCount).coerceAtLeast(1)) { awayGoalsList.add(false) }
            val randomV = java.util.Random(match.id.toLong() + 600)
            awayGoalsList.shuffle(randomV)

            for (i in 0 until 5) {
                if (i < homeTakers.size && i < homeGoalsList.size) {
                    list.add(ParsedPenaltyTaker(homeTakers[i], match.homeTeam.name, homeGoalsList[i]))
                }
                if (i < awayTakers.size && i < awayGoalsList.size) {
                    list.add(ParsedPenaltyTaker(awayTakers[i], match.awayTeam.name, awayGoalsList[i]))
                }
            }
        }
        list
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F0F0F),
        scrimColor = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // CABECERA COMPARATIVA PREMIUM
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(match.homeTeam.flagUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeam.name.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                Text(
                    text = "ESTADÍSTICAS VIP",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = Color(0xFFFFD700),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.awayTeam.name.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(match.awayTeam.flagUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MARCADOR GRANDE PREMIUM
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (match.homeScore ?: 0).toString(),
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                        if (match.homePenalties != null && match.awayPenalties != null) {
                            Text(
                                text = " (${match.homePenalties})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                        Text(
                            text = " : ",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        if (match.homePenalties != null && match.awayPenalties != null) {
                            Text(
                                text = "(${match.awayPenalties}) ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                        Text(
                            text = (match.awayScore ?: 0).toString(),
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }
                    if (match.clock != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = match.clock!!.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // LISTA DESLIZANTE DE ESTADÍSTICAS Y MÁS
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                // Tanda de Penales Gráfica si el partido fue a tanda de penales
                if (match.homePenalties != null && match.awayPenalties != null) {
                    item {
                        PenaltyShootoutView(
                            homeTeamName = match.homeTeam.name,
                            awayTeamName = match.awayTeam.name,
                            homePenalties = match.homePenalties,
                            awayPenalties = match.awayPenalties,
                            takers = detailedPenalties
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                // Posesión
                item {
                    PremiumStatBar(
                        label = "Posesión del Balón",
                        homeValue = homePossession,
                        awayValue = awayPossession,
                        homeString = "$homePossession%",
                        awayString = "$awayPossession%"
                    )
                }

                // Tiros
                item {
                    PremiumStatBar(
                        label = "Tiros a Puerta",
                        homeValue = homeShots,
                        awayValue = awayShots,
                        homeString = homeShots.toString(),
                        awayString = awayShots.toString()
                    )
                }

                // Córneres
                item {
                    val hc = homeCorners.toIntOrNull() ?: 0
                    val ac = awayCorners.toIntOrNull() ?: 0
                    PremiumStatBar(
                        label = "Tiros de Esquina (Corners)",
                        homeValue = hc,
                        awayValue = ac,
                        homeString = homeCorners,
                        awayString = awayCorners
                    )
                }

                // Precisión de Pases
                item {
                    val hPct = homePassesPct.replace("%", "").toIntOrNull() ?: 0
                    val aPct = awayPassesPct.replace("%", "").toIntOrNull() ?: 0
                    PremiumStatBar(
                        label = "Precisión de Pase",
                        homeValue = hPct,
                        awayValue = aPct,
                        homeString = homePassesPct,
                        awayString = awayPassesPct
                    )
                }

                // Pases Totales
                item {
                    val hpCount = homePasses.substringBefore("/").toIntOrNull() ?: 0
                    val apCount = awayPasses.substringBefore("/").toIntOrNull() ?: 0
                    PremiumStatBar(
                        label = "Pases Completados",
                        homeValue = hpCount,
                        awayValue = apCount,
                        homeString = homePasses,
                        awayString = awayPasses
                    )
                }

                // Tarjetas y Faltas
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "FALTAS",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(homeFouls, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    Text("-", color = Color.White.copy(alpha = 0.3f))
                                    Text(awayFouls, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "TARJETAS",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Local Cards
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if ((homeYellow.toIntOrNull() ?: 0) > 0) {
                                            Box(modifier = Modifier.size(width = 8.dp, height = 12.dp).background(Color(0xFFFFD700), shape = RoundedCornerShape(1.dp)))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(homeYellow, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                        }
                                        if ((homeRed.toIntOrNull() ?: 0) > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(modifier = Modifier.size(width = 8.dp, height = 12.dp).background(Color(0xFFE53935), shape = RoundedCornerShape(1.dp)))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(homeRed, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                    Text("  -  ", color = Color.White.copy(alpha = 0.3f))
                                    // Away Cards
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if ((awayYellow.toIntOrNull() ?: 0) > 0) {
                                            Box(modifier = Modifier.size(width = 8.dp, height = 12.dp).background(Color(0xFFFFD700), shape = RoundedCornerShape(1.dp)))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(awayYellow, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                        }
                                        if ((awayRed.toIntOrNull() ?: 0) > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(modifier = Modifier.size(width = 8.dp, height = 12.dp).background(Color(0xFFE53935), shape = RoundedCornerShape(1.dp)))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(awayRed, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Goleadores
                if (generatedScorers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "GOLEADORES",
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                generatedScorers.forEach { scorer ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = scorer,
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Ficha Técnica (Estadio, Árbitro, etc.)
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DETALLE DEL ENCUENTRO",
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val displayDate = match.date ?: "A definir"
                            val stadium = match.stadium ?: "Estadio a Definir"
                            val city = match.city ?: ""
                            val venue = if (city.isNotEmpty()) "$stadium, $city" else stadium

                            InfoRow("📅 Saque central", displayDate)
                            InfoRow("🏟️ Estadio", venue)
                            InfoRow("🏆 Campeonato", "World Cup 2026")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTONES DE ACCIÓN (INCIDENCIAS Y ENTENDIDO)
            var showEventsDialog by remember { mutableStateOf(false) }
            
            Button(
                onClick = { showEventsDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    "ENTENDIDO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
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
                
                val filteredEvents = remember(match.events) {
                    match.events.filter { !it.contains("tanda de penales", ignoreCase = true) && !it.contains("[Penales]", ignoreCase = true) }
                }

                if (filteredEvents.isEmpty()) {
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
                        items(filteredEvents.size) { index ->
                            val event = filteredEvents[index]
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

