package com.example.worldcup2026.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.util.SoundManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

object FeaturedMatchDetector {
    private val CLASICOS = listOf(
        setOf("boca", "river"),
        setOf("racing", "independiente"),
        setOf("san lorenzo", "huracán"),
        setOf("san lorenzo", "huracan"),
        setOf("rosario central", "newell"),
        setOf("estudiantes", "gimnasia"),
        setOf("belgrano", "talleres"),
        setOf("colón", "unión"),
        setOf("colon", "union"),
        setOf("lanús", "banfield"),
        setOf("lanus", "banfield")
    )

    fun findFeaturedMatch(matches: List<Match>): Pair<Match, String>? {
        val now = LocalDateTime.now()
        val upcomingMatches = matches.filter {
            it.status.equals("Scheduled", ignoreCase = true) || it.status.equals("LIVE", ignoreCase = true)
        }.mapNotNull { match ->
            try {
                val dt = LocalDateTime.parse(match.date?.replace(" ", "T"))
                if (dt.isAfter(now.minusHours(2))) match to dt else null
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.second }

        for ((match, _) in upcomingMatches) {
            val h = match.homeTeam.name.lowercase()
            val a = match.awayTeam.name.lowercase()

            // 1. Selección Argentina
            if (h.contains("argentina") || a.contains("argentina")) {
                val label = if (match.tournament_id == 14) "🇦🇷 AMISTOSO INTERNACIONAL" else "🇦🇷 SELECCIÓN ARGENTINA"
                return match to label
            }

            // 2. Superclásico o Clásicos de Fútbol
            if (CLASICOS.any { pair -> (pair.any { h.contains(it) } && pair.any { a.contains(it) }) }) {
                val label = if ((h.contains("boca") && a.contains("river")) || (h.contains("river") && a.contains("boca"))) {
                    "🔥 EL SUPERCLÁSICO"
                } else {
                    "⚔️ CLÁSICO DESTACADO"
                }
                return match to label
            }

            // 3. Partidos destacados por API o torneos internacionales / finales
            if (match.is_featured || match.tournament_id in listOf(3, 4, 12, 22, 23)) {
                return match to "🏆 PARTIDO DESTACADO"
            }
        }

        // Fallback: Si no hay clásico, devolver el próximo partido más cercano si faltan menos de 7 días
        if (upcomingMatches.isNotEmpty()) {
            val (nextMatch, nextDt) = upcomingMatches.first()
            if (Duration.between(now, nextDt).toDays() <= 7) {
                return nextMatch to "⚽ PRÓXIMO ENCUENTRO"
            }
        }

        return null
    }
}

@Composable
fun FeaturedMatchSpotlight(
    matches: List<Match>,
    onNavigateToProde: () -> Unit,
    onNavigateToMatch: (Match) -> Unit,
    modifier: Modifier = Modifier
) {
    val featured = remember(matches) { FeaturedMatchDetector.findFeaturedMatch(matches) } ?: return
    val match = featured.first
    val badgeLabel = featured.second
    var isDismissed by remember { mutableStateOf(false) }

    if (isDismissed) return

    val matchDateTime = remember(match.date) {
        try {
            LocalDateTime.parse(match.date?.replace(" ", "T"))
        } catch (e: Exception) {
            LocalDateTime.now().plusDays(2)
        }
    }

    var remainingDuration by remember { mutableStateOf(Duration.between(LocalDateTime.now(), matchDateTime)) }

    LaunchedEffect(matchDateTime) {
        while (true) {
            remainingDuration = Duration.between(LocalDateTime.now(), matchDateTime)
            delay(1000)
        }
    }

    val days = remainingDuration.toDays().coerceAtLeast(0)
    val hours = (remainingDuration.toHours() % 24).coerceAtLeast(0)
    val minutes = (remainingDuration.toMinutes() % 60).coerceAtLeast(0)
    val seconds = (remainingDuration.seconds % 60).coerceAtLeast(0)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFFD700).copy(alpha = borderAlpha),
                        Color(0xFFFF9800).copy(alpha = borderAlpha)
                    )
                ),
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141722)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Fondo con gradiente sutil
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFF9800).copy(alpha = 0.12f),
                                Color(0xFF0D0F18).copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con Etiqueta y Botón Cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = badgeLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { isDismissed = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Equipos y Escudos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Local
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!match.homeTeam.flagUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = match.homeTeam.flagUrl,
                                contentDescription = match.homeTeam.name,
                                modifier = Modifier.size(44.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚽", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = match.homeTeam.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // VS y Fecha
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "VS",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = try {
                                matchDateTime.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + " hs"
                            } catch (e: Exception) {
                                match.date ?: ""
                            },
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Visitante
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!match.awayTeam.flagUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = match.awayTeam.flagUrl,
                                contentDescription = match.awayTeam.name,
                                modifier = Modifier.size(44.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚽", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = match.awayTeam.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cuenta Regresiva
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (days > 0) "Faltan ${days}d ${hours}h ${minutes}m ${seconds}s" else "Faltan ${hours}h ${minutes}m ${seconds}s",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Acción: Jugar en el Prode
                Button(
                    onClick = {
                        SoundManager.playTic()
                        onNavigateToProde()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "¡CARGAR PRONÓSTICO EN EL PRODE!",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
