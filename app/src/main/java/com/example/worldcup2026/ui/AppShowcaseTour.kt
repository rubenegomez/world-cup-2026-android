package com.example.worldcup2026.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.worldcup2026.data.util.SoundManager

data class InteractiveTourStep(
    val id: Int,
    val icon: String,
    val title: String,
    val description: String,
    val callToAction: String,
    val targetXPercent: Float,
    val targetYPercent: Float,
    val targetRadiusDp: Float,
    val isBottomCard: Boolean,
    val screenTarget: Int // 0: Calendario, 4: Partidos del Día, 1: Prode, 3: Ajustes
)

val interactiveJourneySteps = listOf(
    // PASO 1: Inicio en Calendario - Partido Destacado
    InteractiveTourStep(
        id = 1,
        icon = "🌟",
        title = "1. Partido Destacado de la Fecha",
        description = "Al ingresar, este cartel estelar te anuncia el clásico o partido más importante con cuenta regresiva. Toca 'Ver Partido' para ir al encuentro.",
        callToAction = "Ir a Ver Partido ➔",
        targetXPercent = 0.5f,
        targetYPercent = 0.35f,
        targetRadiusDp = 78f,
        isBottomCard = true,
        screenTarget = 0
    ),
    // PASO 2: En Partidos del Día - Selector de Torneo
    InteractiveTourStep(
        id = 2,
        icon = "🏆",
        title = "2. Selector de Torneos",
        description = "Aquí puedes filtrar los partidos del día entre Liga Profesional, Libertadores, Copa Argentina, Ascenso o el Mundial.",
        callToAction = "Siguiente ➔",
        targetXPercent = 0.5f,
        targetYPercent = 0.12f,
        targetRadiusDp = 58f,
        isBottomCard = true,
        screenTarget = 4
    ),
    // PASO 3: En Partidos del Día - Cómo Cargar Pronóstico
    InteractiveTourStep(
        id = 3,
        icon = "✍️",
        title = "3. Carga tu Pronóstico (Prode)",
        description = "Ingresa los goles del Local y Visitante antes de que empiece el partido. ¡El sistema guarda tu predicción automáticamente!",
        callToAction = "Siguiente ➔",
        targetXPercent = 0.5f,
        targetYPercent = 0.48f,
        targetRadiusDp = 80f,
        isBottomCard = true,
        screenTarget = 4
    ),
    // PASO 4: En Prode - Botón de Ayuda y Reglas
    InteractiveTourStep(
        id = 4,
        icon = "❓",
        title = "4. Ayuda y Reglas de Puntuación",
        description = "En la sección Prode toca '❓ Ayuda' para ver el sistema de puntos: 3 puntos por acierto exacto o 1 punto por acertar el ganador/empate.",
        callToAction = "Siguiente ➔",
        targetXPercent = 0.22f,
        targetYPercent = 0.28f,
        targetRadiusDp = 48f,
        isBottomCard = true,
        screenTarget = 1
    ),
    // PASO 5: En Prode - Ligas de Amigos
    InteractiveTourStep(
        id = 5,
        icon = "👥",
        title = "5. Ligas Privadas con Amigos",
        description = "Crea tu propia liga de amigos o únete con un código de invitación para competir mano a mano durante todo el torneo.",
        callToAction = "Siguiente ➔",
        targetXPercent = 0.5f,
        targetYPercent = 0.38f,
        targetRadiusDp = 65f,
        isBottomCard = true,
        screenTarget = 1
    ),
    // PASO 6: En Ajustes - Favoritos y Batería
    InteractiveTourStep(
        id = 6,
        icon = "🔋",
        title = "6. Alertas y Batería Sin Restricciones",
        description = "En Ajustes marca tus clubes favoritos y activa el modo 'Sin Restricciones' para no perderte ningún gol ni aviso en vivo.",
        callToAction = "¡Terminar Tour! ⭐",
        targetXPercent = 0.5f,
        targetYPercent = 0.45f,
        targetRadiusDp = 70f,
        isBottomCard = true,
        screenTarget = 3
    )
)

/**
 * Showcase Interactivo Guiado ("Camino del Usuario") que navega entre pantallas automáticamente.
 */
@Composable
fun InteractiveJourneyTour(
    currentStepIndex: Int,
    onStepChange: (Int) -> Unit,
    onTourFinished: () -> Unit
) {
    val totalSteps = interactiveJourneySteps.size
    if (currentStepIndex !in 0 until totalSteps) return

    val step = interactiveJourneySteps[currentStepIndex]

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

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
                    onStepChange(currentStepIndex + 1)
                } else {
                    onTourFinished()
                }
            }
    ) {
        val targetCenterX = screenWidthPx * step.targetXPercent
        val targetCenterY = screenHeightPx * step.targetYPercent
        val targetRadiusPx = with(density) { step.targetRadiusDp.dp.toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.99f }
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.78f))

            drawCircle(
                color = Color.Transparent,
                radius = targetRadiusPx,
                center = Offset(targetCenterX, targetCenterY),
                blendMode = BlendMode.Clear
            )

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = pulseAlpha),
                radius = targetRadiusPx * pulseScale,
                center = Offset(targetCenterX, targetCenterY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )

            drawCircle(
                color = Color(0xFFFFD700),
                radius = targetRadiusPx,
                center = Offset(targetCenterX, targetCenterY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 40.dp),
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
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = step.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step.title,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            repeat(totalSteps) { idx ->
                                val isActive = idx == currentStepIndex
                                Box(
                                    modifier = Modifier
                                        .size(if (isActive) 16.dp else 7.dp, 7.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isActive) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f))
                                )
                            }
                        }

                        Button(
                            onClick = {
                                SoundManager.playTic()
                                if (currentStepIndex < totalSteps - 1) {
                                    onStepChange(currentStepIndex + 1)
                                } else {
                                    onTourFinished()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = step.callToAction,
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
