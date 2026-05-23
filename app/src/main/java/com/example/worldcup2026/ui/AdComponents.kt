package com.example.worldcup2026.ui

import com.example.worldcup2026.data.model.Match
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun VipStatsDialog(match: Match, onDismiss: () -> Unit) {
    val random = remember(match.id) { java.util.Random(match.id.toLong()) }

    // 1. Posesión de balón
    val homePossession = match.homePossession ?: (45 + random.nextInt(16))
    val awayPossession = match.awayPossession ?: (100 - homePossession)

    // 2. Tiros al arco
    val homeShots = match.homeShots ?: (5 + random.nextInt(11))
    val awayShots = match.awayShots ?: (5 + random.nextInt(11))

    // 3. xG (Goles esperados)
    val homeXg = String.format(java.util.Locale.US, "%.2f", 0.5f + homeShots * 0.12f + random.nextFloat() * 0.3f)
    val awayXg = String.format(java.util.Locale.US, "%.2f", 0.5f + awayShots * 0.12f + random.nextFloat() * 0.3f)

    // 4. Probabilidades de victoria (1x2)
    val homeWeight = 30 + random.nextInt(40)
    val drawWeight = 20 + random.nextInt(20)
    val awayWeight = 30 + random.nextInt(40)
    val totalWeight = (homeWeight + drawWeight + awayWeight).toFloat()
    val homeProb = ((homeWeight / totalWeight) * 100).toInt()
    val drawProb = ((drawWeight / totalWeight) * 100).toInt()
    val awayProb = 100 - homeProb - drawProb

    // 5. Goleadores
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
        containerColor = Color(0xFF121212),
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "MÉTRICAS TÁCTICAS VIP",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFFFFD700)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarjeta de Enfrentamiento
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${match.homeTeam.name} vs ${match.awayTeam.name}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        if (match.homeScore != null && match.awayScore != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Resultado Final: ${match.homeScore} - ${match.awayScore}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Probabilidad 1X2
                Text(
                    "Probabilidad de Victoria",
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Barra de probabilidad segmentada
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(maxOf(homeProb.toFloat(), 1f))
                            .background(Color(0xFF4CAF50))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(maxOf(drawProb.toFloat(), 1f))
                            .background(Color(0xFFFFC107))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(maxOf(awayProb.toFloat(), 1f))
                            .background(Color(0xFF2196F3))
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Local: $homeProb%", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Text("Empate: $drawProb%", fontSize = 10.sp, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
                    Text("Visita: $awayProb%", fontSize = 10.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Posesión Proyectada/Real
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Posesión", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text("$homePossession% - $awayPossession%", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(homePossession.toFloat())
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(awayPossession.toFloat())
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tiros al Arco
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tiros al Arco", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text("$homeShots - $awayShots", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(maxOf(homeShots.toFloat(), 1f))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(maxOf(awayShots.toFloat(), 1f))
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Goles Esperados (xG)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("xG (Goles Esperados)", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text("$homeXg - $awayXg", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }

                if (generatedScorers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Goleadores",
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
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
        },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val apiId = when (match.id) {
                    131 -> 863234
                    132 -> 863233
                    1 -> 863172
                    else -> 863234 // Fallback de demostración para cualquier otro partido
                }
                
                if (apiId != null) {
                    var showWidgetDialog by remember { mutableStateOf(false) }
                    
                    Button(
                        onClick = { showWidgetDialog = true },
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
                            "VER WIDGET EN VIVO (VIP)",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                    
                    if (showWidgetDialog) {
                        VipWidgetDialog(fixtureId = apiId, onDismiss = { showWidgetDialog = false })
                    }
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

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun VipWidgetWebView(htmlContent: String, modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { context ->
            android.webkit.WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    userAgentString = "Mozilla/5.0 (Linux; Android 13; SM-S906E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36"
                }
                webViewClient = android.webkit.WebViewClient()
                webChromeClient = android.webkit.WebChromeClient()
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "https://widgets.api-sports.io/",
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier
    )
}

@Composable
fun VipWidgetDialog(fixtureId: Int, onDismiss: () -> Unit) {
    val htmlContent = remember(fixtureId) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    background-color: #121212;
                    color: #ffffff;
                    margin: 0;
                    padding: 8px;
                }
            </style>
        </head>
        <body>
            <div id="wg-api-football-match"
                 data-host="v3.football.api-sports.io"
                 data-key="13f1fe9cefbb89f9b748728c80582fa4"
                 data-fixture="$fixtureId"
                 data-theme="dark"
                 data-show-errors="true"
                 class="api_football_loader">
            </div>
            <script type="module" src="https://widgets.api-sports.io/2.0.3/widgets.js"></script>
        </body>
        </html>
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "WIDGET INTERACTIVO VIP",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFFFD700)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
            ) {
                VipWidgetWebView(
                    htmlContent = htmlContent,
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        confirmButton = {}
    )
}
