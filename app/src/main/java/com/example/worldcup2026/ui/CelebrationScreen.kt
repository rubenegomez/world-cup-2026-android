package com.example.worldcup2026.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.worldcup2026.R
import com.example.worldcup2026.data.model.Team
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun CelebrationScreen(champion: Team, tournamentName: String = "EL MUNDIAL", onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val titleText = when {
        tournamentName.contains("Mundial", ignoreCase = true) -> "¡CAMPEÓN DEL MUNDO!"
        tournamentName.contains("Libertadores", ignoreCase = true) -> "¡CAMPEÓN DE LA LIBERTADORES!"
        tournamentName.contains("Liga", ignoreCase = true) -> "¡CAMPEÓN DE LA LIGA!"
        else -> "¡CAMPEÓN!"
    }
    
    // Vibración de pantalla (Simula salto de la hinchada)
    val vibrationY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer { translationY = vibrationY },
        contentAlignment = Alignment.Center
    ) {
        // Smoke Effects (blurred gradients)
        SmokeEffect()

        // Confetti Effect (Massive white paper rain)
        ConfettiLayer()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 64.dp)
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineMedium.copy(
                    letterSpacing = 2.sp,
                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black, blurRadius = 8f)
                ),
                color = Color.Yellow,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Trophy with scaling
            Box(contentAlignment = Alignment.Center) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            Brush.radialGradient(listOf(Color.Yellow.copy(alpha = 0.3f), Color.Transparent)),
                            CircleShape
                        )
                        .scale(scale * 2.5f)
                )
                
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.trophy),
                    contentDescription = null,
                    modifier = Modifier
                        .size(260.dp)
                        .scale(scale)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Champion Flag
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(champion.flagUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = champion.name.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("CERRAR FESTEJO", fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Close button top end
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
        }
    }
}

@Composable
fun SmokeEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val smokeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(3) { i ->
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .offset(x = (i * 100 - 100).dp, y = (i * 200).dp)
                    .graphicsLayer { 
                        alpha = smokeAlpha
                        scaleX = 1.5f
                        scaleY = 1.2f
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                listOf(Color.Blue, Color.Yellow, Color.White).random().copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun ConfettiLayer() {
    // Aumentamos a 150 partículas para efecto masivo
    val particles = remember { List(150) { ConfettiParticle() } }
    
    particles.forEach { particle ->
        val transition = rememberInfiniteTransition()
        val yPos by transition.animateFloat(
            initialValue = -100f,
            targetValue = 2500f,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration, delayMillis = particle.delay, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        val xOffset by transition.animateFloat(
            initialValue = particle.xStart,
            targetValue = particle.xStart + particle.drift,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .offset(x = xOffset.dp, y = yPos.dp)
                .size(particle.size.dp, (particle.size * 1.5).dp)
                .background(particle.color)
                .graphicsLayer { 
                    rotationZ = yPos * particle.rotationSpeed 
                    alpha = 0.9f
                }
        )
    }
}

class ConfettiParticle {
    val xStart = Random.nextInt(-100, 600).toFloat()
    val drift = Random.nextInt(-150, 150).toFloat()
    val duration = Random.nextInt(2000, 5000) // Más rápido
    val delay = Random.nextInt(0, 5000)
    val size = Random.nextInt(4, 10)
    val rotationSpeed = Random.nextFloat() * 2f
    val color = if (Random.nextFloat() > 0.3f) {
        Color.White // Predomina el blanco
    } else {
        listOf(
            Color(0xFFFFD700), // Oro
            Color(0xFF2196F3), // Azul
            Color(0xFF4CAF50), // Verde
            Color(0xFFF44336)  // Rojo
        ).random()
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
