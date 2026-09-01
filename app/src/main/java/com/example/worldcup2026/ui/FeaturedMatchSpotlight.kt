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
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFFD700).copy(alpha = borderAlpha),
                        Color(0xFFFF9800).copy(alpha = borderAlpha)
                    )
                ),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
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
                                Color(0xFFFF9800).copy(alpha = 0.10f),
                                Color(0xFF0D0F18).copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con Etiqueta y Botón Cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = badgeLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                letterSpacing = 0.3.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Cuenta Regresiva compacta
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (days > 0) "${days}d ${hours}h ${minutes}m" else "${hours}h ${minutes}m ${seconds}s",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { isDismissed = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Equipos, Escudos y Botón Prode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Local
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = match.homeTeam.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (!match.homeTeam.flagUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = match.homeTeam.flagUrl,
                                contentDescription = match.homeTeam.name,
                                modifier = Modifier.size(26.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text("⚽", fontSize = 16.sp)
                        }
                    }

                    // VS / Fecha
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "VS",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Text(
                            text = try {
                                matchDateTime.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                            } catch (e: Exception) {
                                match.date ?: ""
                            },
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Visitante
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (!match.awayTeam.flagUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = match.awayTeam.flagUrl,
                                contentDescription = match.awayTeam.name,
                                modifier = Modifier.size(26.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text("⚽", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = match.awayTeam.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Botón Acción: Jugar en el Prode Compacto
                Button(
                    onClick = {
                        SoundManager.playTic()
                        onNavigateToProde()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "¡CARGAR PRONÓSTICO EN EL PRODE!",
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }
    }
}
