package com.example.worldcup2026.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.worldcup2026.data.model.Team

@Composable
fun CelebrationScreen(champion: Team, tournamentName: String = "DEL TORNEO", onDismiss: () -> Unit) {
    val titleText = when {
        tournamentName.contains("Libertadores", ignoreCase = true) -> "🏆 ¡CAMPEÓN DE LA COPA LIBERTADORES!"
        tournamentName.contains("Sudamericana", ignoreCase = true) -> "🏆 ¡CAMPEÓN DE LA COPA SUDAMERICANA!"
        tournamentName.contains("Liga", ignoreCase = true) -> "🏆 ¡CAMPEÓN DE LA LIGA PROFESIONAL!"
        tournamentName.contains("Argentina", ignoreCase = true) -> "🏆 ¡CAMPEÓN DE LA COPA ARGENTINA!"
        else -> "🏆 ¡CAMPEÓN $tournamentName!"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = titleText,
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                // Escudo del Campeón
                TeamBadge(
                    team = champion,
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = champion.name.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "¡Felicitaciones al nuevo campeón!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Aceptar",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}



@Composable
fun TeamBadge(team: com.example.worldcup2026.data.model.Team, modifier: Modifier = Modifier) {
    val flag = team.flagUrl
    if (!flag.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(flag)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF333333), Color(0xFF111111))))
                .border(1.5.dp, Color(0xFFFFD700), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (team.name.takeIf { it.isNotBlank() } ?: "T").take(1).uppercase(),
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
    }
}

@Composable
fun GoalCelebrationDialog(match: com.example.worldcup2026.data.model.Match, onDismiss: () -> Unit) {
    val isGoal = match.status.equals("LIVE", ignoreCase = true)
    val hScore = match.homeScore ?: 0
    val aScore = match.awayScore ?: 0
    val isFinished = match.status.equals("Finished", ignoreCase = true)

    val latestScorer = remember(match.scorers) {
        match.scorers.lastOrNull()?.replace("[Penales]", "")?.replace("[Tanda de penales]", "")?.trim()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    DisposableEffect(Unit) {
        val soundRes = if (isGoal) com.example.worldcup2026.R.raw.gooolll else com.example.worldcup2026.R.raw.silbato
        val player = try {
            android.media.MediaPlayer.create(context, soundRes)?.apply {
                start()
            }
        } catch (e: Exception) {
            null
        }
        onDispose {
            try {
                player?.stop()
                player?.release()
            } catch (e: Exception) { }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Título claro del Popup
                if (isGoal) {
                    val goalTeam = if (hScore > aScore) match.homeTeam.name else if (aScore > hScore) match.awayTeam.name else ""
                    val headerGoal = if (goalTeam.isNotEmpty()) "⚽ ¡GOL DE ${goalTeam.uppercase()}!" else "⚽ ¡GOL!"
                    
                    Text(
                        text = headerGoal,
                        color = Color(0xFFFFD700),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (!latestScorer.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Anotó: $latestScorer",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                maxLines = 1
                            )
                        }
                    }
                } else {
                    val headerTitle = when {
                        isFinished && hScore > aScore -> "🏁 FINAL: Ganó ${match.homeTeam.name}"
                        isFinished && aScore > hScore -> "🏁 FINAL: Ganó ${match.awayTeam.name}"
                        isFinished -> "🏁 FINAL: Empate"
                        else -> "🏁 FINAL DEL PARTIDO"
                    }
                    Text(
                        text = headerTitle,
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // Escudos y Marcador
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Local
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        TeamBadge(
                            team = match.homeTeam,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = match.homeTeam.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 2,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    // Marcador
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "$hScore - $aScore",
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    // Visitante
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        TeamBadge(
                            team = match.awayTeam,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = match.awayTeam.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 2,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Botón Aceptar
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Aceptar",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
