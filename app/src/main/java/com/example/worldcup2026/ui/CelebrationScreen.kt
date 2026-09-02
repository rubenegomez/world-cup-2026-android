package com.example.worldcup2026.ui

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.worldcup2026.R
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import kotlinx.coroutines.delay

/**
 * Overlay de Gala a pantalla completa 100% transparente para consagración del Campeón.
 */
@Composable
fun TournamentChampionOverlay(
    champion: Team,
    tournamentName: String = "DEL TORNEO",
    durationMillis: Long = 5000L,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scaleAnim = remember { Animatable(0.0f) }
    val alphaAnim = remember { Animatable(0.0f) }
    val rotateAnim = remember { Animatable(0.0f) }

    DisposableEffect(Unit) {
        val player = try {
            MediaPlayer.create(context, R.raw.gooolll)?.apply {
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

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alphaAnim.animateTo(1.0f, animationSpec = tween(250))
        rotateAnim.animateTo(
            targetValue = 360f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )

        delay(durationMillis)
        alphaAnim.animateTo(0.0f, animationSpec = tween(400))
        onDismiss()
    }

    val titleText = when {
        tournamentName.contains("Libertadores", ignoreCase = true) -> "COPA LIBERTADORES"
        tournamentName.contains("Sudamericana", ignoreCase = true) -> "COPA SUDAMERICANA"
        tournamentName.contains("Liga", ignoreCase = true) -> "LIGA PROFESIONAL"
        tournamentName.contains("Argentina", ignoreCase = true) -> "COPA ARGENTINA"
        else -> tournamentName.uppercase()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Fondo radial translúcido festivo dorado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.88f)
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                    transformOrigin = TransformOrigin.Center
                }
        ) {
            CurvedGoalText(
                text = "🏆 ¡CAMPEÓN!",
                primaryColor = Color(0xFFFFD700),
                strokeColor = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Escudo Gigante del Campeón con halo dorado
            Box(
                modifier = Modifier
                    .size(115.dp)
                    .shadow(32.dp, CircleShape, spotColor = Color(0xFFFFD700), ambientColor = Color(0xFFFFD700))
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(3.5.dp, Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!champion.flagUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(champion.flagUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = champion.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = champion.name.take(2).uppercase(),
                        color = Color(0xFFFFD700),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Placa dorada de consagración
            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
                ) {
                    Text(
                        text = champion.name.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¡NUEVO CAMPEÓN DE $titleText!",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Delegación compatible para CelebrationScreen
 */
@Composable
fun CelebrationScreen(
    champion: Team,
    tournamentName: String = "DEL TORNEO",
    onDismiss: () -> Unit
) {
    TournamentChampionOverlay(
        champion = champion,
        tournamentName = tournamentName,
        onDismiss = onDismiss
    )
}

/**
 * Delegación compatible para GoalCelebrationDialog hacia los nuevos overlays transparentes
 */
@Composable
fun GoalCelebrationDialog(
    match: Match,
    onDismiss: () -> Unit
) {
    if (match.status.equals("Finished", ignoreCase = true)) {
        MatchFinishedOverlay(
            match = match,
            onDismiss = onDismiss
        )
    } else {
        GoalCelebrationOverlay(
            match = match,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun TeamBadge(team: Team, modifier: Modifier = Modifier) {
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
