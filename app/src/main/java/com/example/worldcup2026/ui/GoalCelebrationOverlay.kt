package com.example.worldcup2026.ui

import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.RectF as AndroidRectF
import android.graphics.Typeface
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
 * Overlay a pantalla completa 100% transparente y sin bordes para celebrar goles en tiempo real.
 */
@Composable
fun GoalCelebrationOverlay(
    match: Match,
    scoringTeam: Team? = null,
    scorerName: String? = null,
    minute: String? = null,
    durationMillis: Long = 4000L,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scaleAnim = remember { Animatable(0.0f) }
    val alphaAnim = remember { Animatable(0.0f) }
    var showDetails by remember { mutableStateOf(false) }

    val resolvedTeam = scoringTeam ?: run {
        val h = match.homeScore ?: 0
        val a = match.awayScore ?: 0
        if (h > a) match.homeTeam else if (a > h) match.awayTeam else match.homeTeam
    }

    val resolvedScorer = scorerName ?: run {
        match.scorers.lastOrNull()?.replace("[Penales]", "")?.replace("[Tanda de penales]", "")?.trim()
            ?: "¡Gran Definición!"
    }

    // Reproducción de audio con liberación segura
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
            } catch (e: Exception) {
                // Ignorar error al liberar audio
            }
        }
    }

    // Secuencia de animación y autocierre
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alphaAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
        delay(250)
        showDetails = true

        delay(durationMillis)
        onDismiss()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                    transformOrigin = TransformOrigin.Center
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. TEXTO "¡GOOOOOOL!" EN SEMICÍRCULO SUPERIOR
                CurvedGoalText(
                    text = "¡GOOOOOOL!",
                    primaryColor = Color(0xFFFFD700),
                    strokeColor = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. ESCUDO CENTRAL DEL CLUB CON DESTELLO
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(24.dp, CircleShape, spotColor = Color(0xFFFFD700), ambientColor = Color(0xFFFFD700))
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(3.dp, Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!resolvedTeam.flagUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(resolvedTeam.flagUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = resolvedTeam.name,
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = resolvedTeam.name.take(2).uppercase(),
                            color = Color(0xFFFFD700),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. TARJETA FLOTANTE CON DATOS DEL GOL Y MARCADOR
                AnimatedVisibility(
                    visible = showDetails,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                        initialOffsetY = { 80 },
                        animationSpec = tween(400)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0F172A).copy(alpha = 0.92f),
                                        Color(0xFF1E293B).copy(alpha = 0.90f)
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                            .padding(vertical = 16.dp, horizontal = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Club y Marcador
                            Text(
                                text = resolvedTeam.name.uppercase(),
                                color = Color(0xFFFFD700),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Marcador Actual
                            val hScore = match.homeScore ?: 0
                            val aScore = match.awayScore ?: 0
                            Text(
                                text = "${match.homeTeam.name} $hScore  -  $aScore ${match.awayTeam.name}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Goleador y Minuto
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "⚽", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = resolvedScorer,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                if (!minute.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFFD700).copy(alpha = 0.25f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$minute'",
                                            color = Color(0xFFFFD700),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renderizado de texto curvado en semicírculo mediante Canvas y drawTextOnPath.
 */
@Composable
fun CurvedGoalText(
    text: String,
    primaryColor: Color,
    strokeColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawIntoCanvas { composeCanvas ->
            val nativeCanvas = composeCanvas.nativeCanvas

            val arcRect = AndroidRectF(
                canvasWidth * 0.05f,
                -canvasHeight * 0.15f,
                canvasWidth * 0.95f,
                canvasHeight * 1.55f
            )

            val path = AndroidPath().apply {
                addArc(arcRect, 200f, 140f)
            }

            // Pincel contorno negro grueso para máximo contraste
            val strokePaint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.STROKE
                strokeWidth = 24f
                color = AndroidColor.BLACK
                textSize = 100f
                textAlign = AndroidPaint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                strokeCap = AndroidPaint.Cap.ROUND
                strokeJoin = AndroidPaint.Join.ROUND
            }

            // Pincel relleno primario brillante
            val fillPaint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.FILL
                color = primaryColor.hashCode()
                textSize = 100f
                textAlign = AndroidPaint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(30f, 0f, 8f, AndroidColor.argb(220, 0, 0, 0))
            }

            nativeCanvas.drawTextOnPath(text, path, 0f, 0f, strokePaint)
            nativeCanvas.drawTextOnPath(text, path, 0f, 0f, fillPaint)
        }
    }
}

/**
 * Overlay a pantalla completa 100% transparente para final del partido (FINALIZADO).
 */
@Composable
fun MatchFinishedOverlay(
    match: Match,
    durationMillis: Long = 4000L,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scaleAnim = remember { Animatable(0.0f) }
    val alphaAnim = remember { Animatable(0.0f) }
    var showDetails by remember { mutableStateOf(false) }

    val hScore = match.homeScore ?: 0
    val aScore = match.awayScore ?: 0
    val isHomeWinner = hScore > aScore
    val isAwayWinner = aScore > hScore
    val isDraw = hScore == aScore

    DisposableEffect(Unit) {
        val player = try {
            MediaPlayer.create(context, R.raw.silbato)?.apply {
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
        showDetails = true

        delay(durationMillis)
        alphaAnim.animateTo(0.0f, animationSpec = tween(400))
        onDismiss()
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
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
                text = "🏁 ¡FINAL!",
                primaryColor = Color(0xFFFFD700),
                strokeColor = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Escudos Enfrentados + Marcador Gigante
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
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(if (isHomeWinner) 20.dp else 6.dp, CircleShape, spotColor = Color(0xFFFFD700))
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(if (isHomeWinner) 3.dp else 1.dp, if (isHomeWinner) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!match.homeTeam.flagUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = match.homeTeam.flagUrl,
                                contentDescription = match.homeTeam.name,
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(match.homeTeam.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.homeTeam.name,
                        color = if (isHomeWinner) Color(0xFFFFD700) else Color.White,
                        fontWeight = if (isHomeWinner) FontWeight.Black else FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }

                // Marcador Central
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.8f)),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "$hScore - $aScore",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Visitante
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(if (isAwayWinner) 20.dp else 6.dp, CircleShape, spotColor = Color(0xFFFFD700))
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(if (isAwayWinner) 3.dp else 1.dp, if (isAwayWinner) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!match.awayTeam.flagUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = match.awayTeam.flagUrl,
                                contentDescription = match.awayTeam.name,
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(match.awayTeam.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.awayTeam.name,
                        color = if (isAwayWinner) Color(0xFFFFD700) else Color.White,
                        fontWeight = if (isAwayWinner) FontWeight.Black else FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cartel de Resultado / Ganador
            AnimatedVisibility(
                visible = showDetails,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 40 })
            ) {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.95f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = when {
                            isHomeWinner -> "🏆 ¡VICTORIA DE ${match.homeTeam.name.uppercase()}!"
                            isAwayWinner -> "🏆 ¡VICTORIA DE ${match.awayTeam.name.uppercase()}!"
                            else -> "🤝 ¡EMPATE FINAL!"
                        },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Overlay a pantalla completa 100% transparente para tarjetas rojas (EXPULSIÓN).
 */
@Composable
fun RedCardOverlay(
    playerName: String,
    teamName: String,
    teamFlagUrl: String? = null,
    minute: String? = null,
    durationMillis: Long = 3500L,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scaleAnim = remember { Animatable(0.0f) }
    val alphaAnim = remember { Animatable(0.0f) }

    DisposableEffect(Unit) {
        val player = try {
            MediaPlayer.create(context, R.raw.silbato)?.apply { start() }
        } catch (e: Exception) { null }
        onDispose {
            try { player?.stop(); player?.release() } catch (e: Exception) { }
        }
    }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        alphaAnim.animateTo(1.0f, animationSpec = tween(200))
        delay(durationMillis)
        alphaAnim.animateTo(0.0f, animationSpec = tween(300))
        onDismiss()
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF990000).copy(alpha = 0.45f), Color.Black.copy(alpha = 0.88f)),
                        radius = 1200f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                }
        ) {
            CurvedGoalText(
                text = "🟥 ¡ROJA!",
                primaryColor = Color(0xFFFF1744),
                strokeColor = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tarjeta Roja 3D
            Box(
                modifier = Modifier
                    .size(width = 65.dp, height = 95.dp)
                    .shadow(24.dp, RoundedCornerShape(8.dp), spotColor = Color(0xFFFF1744))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFD50000))
                    .border(2.dp, Color(0xFFFF5252), RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF1744))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp)
                ) {
                    Text(
                        text = playerName.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (!minute.isNullOrBlank()) "$teamName · Min $minute'" else teamName,
                        color = Color(0xFFFF8A80),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Cápsula Flotante Superior (Dynamic Island) para eventos rápidos como Tarjeta Amarilla y Sustituciones.
 */
@Composable
fun LiveEventTopCapsule(
    icon: String,
    title: String,
    subtitle: String,
    accentColor: Color = Color(0xFFFFD700),
    durationMillis: Long = 2500L,
    onDismiss: () -> Unit
) {
    val offsetY = remember { Animatable(-100f) }
    val alphaAnim = remember { Animatable(0.0f) }

    LaunchedEffect(Unit) {
        offsetY.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        alphaAnim.animateTo(1.0f, animationSpec = tween(200))
        delay(durationMillis)
        alphaAnim.animateTo(0.0f, animationSpec = tween(300))
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.95f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.8f)),
            modifier = Modifier
                .graphicsLayer {
                    translationY = offsetY.value
                    alpha = alphaAnim.value
                }
                .clickable { onDismiss() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(text = icon, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(text = subtitle, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
