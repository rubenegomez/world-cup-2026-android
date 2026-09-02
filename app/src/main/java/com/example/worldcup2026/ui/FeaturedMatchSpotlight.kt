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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Stars
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
                val label = if (match.tournament_id == 14) "🇦🇷 AMISTOSO SELECCIÓN ARGENTINA" else "🇦🇷 SELECCIÓN ARGENTINA"
                return match to label
            }

            // 2. Superclásico o Clásicos
            if (CLASICOS.any { pair -> (pair.any { h.contains(it) } && pair.any { a.contains(it) }) }) {
                val label = if ((h.contains("boca") && a.contains("river")) || (h.contains("river") && a.contains("boca"))) {
                    "🔥 EL SUPERCLÁSICO ARGENTINO"
                } else {
                    "⚔️ CLÁSICO DE LA FECHA"
                }
                return match to label
            }

            // 3. Partidos destacados por API o finales
            if (match.is_featured || match.tournament_id in listOf(3, 4, 12, 22, 23)) {
                return match to "🏆 PARTIDO DESTACADO"
            }
        }

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
    onNavigateToMatch: (Match) -> Unit = {},
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
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(14.dp, RoundedCornerShape(20.dp))
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFFD700).copy(alpha = borderAlpha),
                        Color(0xFFFF8F00).copy(alpha = borderAlpha)
                    )
                ),
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131722)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Fondo con brillo degradado de gala ajustado al tamaño exacto del contenido
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFF9800).copy(alpha = 0.16f),
                                Color(0xFF0C0E17).copy(alpha = 0.96f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Etiqueta de Gala y Botón Cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = badgeLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                letterSpacing = 0.6.sp
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
                            tint = Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fila de Equipos: Escudos GRANDES y Nombres en segundo renglón
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Equipo Local
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!match.homeTeam.flagUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = match.homeTeam.flagUrl,
                                contentDescription = match.homeTeam.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .shadow(6.dp, CircleShape),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚽", fontSize = 28.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = match.homeTeam.name,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // VS Central y Horario
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "VS",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = try {
                                    matchDateTime.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + " hs"
                                } catch (e: Exception) {
                                    match.date ?: ""
                                },
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Equipo Visitante
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!match.awayTeam.flagUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = match.awayTeam.flagUrl,
                                contentDescription = match.awayTeam.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .shadow(6.dp, CircleShape),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚽", fontSize = 28.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = match.awayTeam.name,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cuenta Regresiva Grande e Imponente
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (days > 0) "FALTAN ${days}d ${hours}h ${minutes}m ${seconds}s" else "FALTAN ${hours}h ${minutes}m ${seconds}s",
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botones de Acción divididos: 1. Ir al partido / 2. Ir al Prode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón 1: Ir directo al partido para votar / ver estadísticas
                    Button(
                        onClick = {
                            SoundManager.playTic()
                            onNavigateToMatch(match)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VER PARTIDO / VOTAR",
                            fontWeight = FontWeight.Black,
                            fontSize = 10.5.sp,
                            maxLines = 1
                        )
                    }

                    // Botón 2: Ir a la sección del Prode
                    Button(
                        onClick = {
                            SoundManager.playTic()
                            onNavigateToProde()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.12f),
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "IR AL PRODE",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fila de información extra (Estadio / Sede e Incentivo de Puntos)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (!match.stadium.isNullOrBlank()) match.stadium else "Estadio Principal",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hasta +5 Pts Prode",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
