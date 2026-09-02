package com.example.worldcup2026.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.worldcup2026.data.util.SoundManager

data class TourStep(
    val id: Int,
    val icon: String,
    val title: String,
    val description: String,
    val targetXPercent: Float,
    val targetYPercent: Float,
    val targetRadiusDp: Float,
    val isBottomCard: Boolean
)

val defaultTourSteps = listOf(
    TourStep(
        id = 1,
        icon = "🏆",
        title = "Selector de Torneos",
        description = "Explora la Liga Profesional, Copa Libertadores, Copa Sudamericana, Copa Argentina, Ascenso y el Mundial.",
        targetXPercent = 0.5f,
        targetYPercent = 0.12f,
        targetRadiusDp = 58f,
        isBottomCard = true
    ),
    TourStep(
        id = 2,
        icon = "📅",
        title = "Fixture y Tablas",
        description = "Consulta el cronograma fecha por fecha, tablas de posiciones, zonas y promedios actualizados en vivo.",
        targetXPercent = 0.5f,
        targetYPercent = 0.38f,
        targetRadiusDp = 75f,
        isBottomCard = true
    ),
    TourStep(
        id = 3,
        icon = "⭐",
        title = "Prode y Predicciones",
        description = "Vota el resultado de cada partido antes de que comience para sumar puntos y competir en el ranking.",
        targetXPercent = 0.5f,
        targetYPercent = 0.93f,
        targetRadiusDp = 48f,
        isBottomCard = false
    ),
    TourStep(
        id = 4,
        icon = "🎁",
        title = "Puntos y Beneficios",
        description = "Canjea tus puntos acumulados para desactivar la publicidad o activar estadísticas exclusivas.",
        targetXPercent = 0.88f,
        targetYPercent = 0.06f,
        targetRadiusDp = 36f,
        isBottomCard = true
    )
)

/**
 * Overlay de Showcase nativo en Jetpack Compose con corte Spotlight y halo dorado pulsante.
 */
@Composable
fun AppShowcaseTour(
    onTourFinished: () -> Unit
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = defaultTourSteps[currentStepIndex]
    val totalSteps = defaultTourSteps.size

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    // Animación de pulso continuo del halo dorado
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                SoundManager.playTic()
                if (currentStepIndex < totalSteps - 1) {
                    currentStepIndex++
                } else {
                    onTourFinished()
                }
            }
    ) {
        val targetCenterX = screenWidthPx * step.targetXPercent
        val targetCenterY = screenHeightPx * step.targetYPercent
        val targetRadiusPx = with(density) { step.targetRadiusDp.dp.toPx() }

        // Capa Canvas con corte Spotlight usando Clear blend mode
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.99f
                }
        ) {
            // Fondo oscuro general
            drawRect(color = Color.Black.copy(alpha = 0.78f))

            // Corte transparente sobre el elemento destacado
            drawCircle(
                color = Color.Transparent,
                radius = targetRadiusPx,
                center = Offset(targetCenterX, targetCenterY),
                blendMode = BlendMode.Clear
            )

            // Halo pulsante exterior
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = pulseAlpha),
                radius = targetRadiusPx * pulseScale,
                center = Offset(targetCenterX, targetCenterY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )

            // Borde dorado nítido del recorte
            drawCircle(
                color = Color(0xFFFFD700),
                radius = targetRadiusPx,
                center = Offset(targetCenterX, targetCenterY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
            )
        }

        // Tarjeta Explicativa Flotante
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 50.dp),
            contentAlignment = if (step.isBottomCard) Alignment.BottomCenter else Alignment.TopCenter
        ) {
            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.96f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.85f)),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { }
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Badge del paso
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "PASO ${currentStepIndex + 1} DE $totalSteps",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        TextButton(
                            onClick = {
                                SoundManager.playTic()
                                onTourFinished()
                            }
                        ) {
                            Text(
                                text = "Omitir Tour",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ícono y Título
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = step.icon, fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = step.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Descripción
                    Text(
                        text = step.description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Indicadores de progreso (dots) + Botón Siguiente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(totalSteps) { idx ->
                                val isActive = idx == currentStepIndex
                                Box(
                                    modifier = Modifier
                                        .size(if (isActive) 18.dp else 8.dp, 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isActive) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f))
                                )
                            }
                        }

                        Button(
                            onClick = {
                                SoundManager.playTic()
                                if (currentStepIndex < totalSteps - 1) {
                                    currentStepIndex++
                                } else {
                                    onTourFinished()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = if (currentStepIndex < totalSteps - 1) "Siguiente ➔" else "¡Comenzar! ⭐",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
