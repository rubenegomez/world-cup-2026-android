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

data class ScreenTourStep(
    val id: Int,
    val icon: String,
    val title: String,
    val description: String,
    val targetXPercent: Float,
    val targetYPercent: Float,
    val targetRadiusDp: Float,
    val isBottomCard: Boolean
)

// 1. TOUR PANTALLA INICIO (CALENDARIO)
val calendarScreenSteps = listOf(
    ScreenTourStep(
        id = 1,
        icon = "🌟",
        title = "Partido Destacado",
        description = "Al iniciar la app, este cartel estelar te anuncia el clásico o partido más importante de la fecha con cuenta regresiva y acceso directo a votar.",
        targetXPercent = 0.5f,
        targetYPercent = 0.40f,
        targetRadiusDp = 80f,
        isBottomCard = true
    ),
    ScreenTourStep(
        id = 2,
        icon = "🗓️",
        title = "Vistas del Calendario & Hoy",
        description = "Alterna entre vista por Mes, Semana o Día para explorar partidos. Toca el botón 'HOY' para saltar instantáneamente a la fecha actual.",
        targetXPercent = 0.5f,
        targetYPercent = 0.14f,
        targetRadiusDp = 65f,
        isBottomCard = true
    ),
    ScreenTourStep(
        id = 3,
        icon = "⭐",
        title = "Pestaña Prode & Ajustes",
        description = "En la barra inferior entra al Prode para cargar tus apuestas y competir. En el engranaje superior podrás configurar alertas y favoritos.",
        targetXPercent = 0.5f,
        targetYPercent = 0.94f,
        targetRadiusDp = 48f,
        isBottomCard = false
    )
)

// 2. TOUR PANTALLA PARTIDOS DEL DÍA
val dailyMatchesScreenSteps = listOf(
    ScreenTourStep(
        id = 1,
        icon = "🏆",
        title = "Selector de Torneos",
        description = "Filtra los partidos del día por Liga Profesional, Libertadores, Copa Argentina, Ascenso o el Mundial.",
        targetXPercent = 0.5f,
        targetYPercent = 0.12f,
        targetRadiusDp = 58f,
        isBottomCard = true
    ),
    ScreenTourStep(
        id = 2,
        icon = "✍️",
        title = "Carga tu Pronóstico",
        description = "Ingresa los goles de cada equipo antes de que empiece el partido. ¡Los pronósticos cerrarán automáticamente al inicio!",
        targetXPercent = 0.5f,
        targetYPercent = 0.46f,
        targetRadiusDp = 75f,
        isBottomCard = true
    ),
    ScreenTourStep(
        id = 3,
        icon = "❓",
        title = "Ayuda y Reglas de Puntos",
        description = "Usa el botón '?' para conocer cómo funciona esta pantalla y '📖 Reglas' para ver cómo se suman los 3 puntos por acierto exacto o 1 por ganador.",
        targetXPercent = 0.85f,
        targetYPercent = 0.20f,
        targetRadiusDp = 42f,
        isBottomCard = true
    )
)

// 3. TOUR PANTALLA PRODE
val prodeScreenSteps = listOf(
    ScreenTourStep(
        id = 1,
        icon = "👥",
        title = "Ligas de Amigos",
        description = "Crea tu propia liga privada o únete con un código de invitación para competir mano a mano con tus amigos.",
        targetXPercent = 0.5f,
        targetYPercent = 0.22f,
        targetRadiusDp = 65f,
        isBottomCard = true
    ),
    ScreenTourStep(
        id = 2,
        icon = "📊",
        title = "Ranking y Posiciones",
        description = "Sigue tu puesto en la tabla global, tus aciertos exactos, porcentajes de efectividad y rachas ganadoras.",
        targetXPercent = 0.5f,
        targetYPercent = 0.52f,
        targetRadiusDp = 75f,
        isBottomCard = true
    )
)

// 4. TOUR PANTALLA AJUSTES
val settingsScreenSteps = listOf(
    ScreenTourStep(
        id = 1,
        icon = "⭐",
        title = "Equipos y Torneos Favoritos",
        description = "Marca tus clubes favoritos para recibir alertas prioritarias de goles, inicio y tarjetas.",
        targetXPercent = 0.5f,
        targetYPercent = 0.35f,
        targetRadiusDp = 60f,
        isBottomCard = true
    ),
    ScreenTourStep(
        id = 2,
        icon = "🔋",
        title = "Batería Sin Restricciones",
        description = "Pon la aplicación 'Sin Restricciones' para garantizar que las notificaciones de goles y eventos en vivo lleguen al instante.",
        targetXPercent = 0.5f,
        targetYPercent = 0.55f,
        targetRadiusDp = 60f,
        isBottomCard = false
    )
)

/**
 * Overlay de Showcase Reutilizable por pantalla en Jetpack Compose.
 */
@Composable
fun ContextualShowcaseTour(
    steps: List<ScreenTourStep>,
    onTourFinished: () -> Unit
) {
    if (steps.isEmpty()) return

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]
    val totalSteps = steps.size

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
                    currentStepIndex++
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
                .graphicsLayer {
                    alpha = 0.99f
                }
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
                                text = "Omitir",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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

                    Text(
                        text = step.description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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
                                text = if (currentStepIndex < totalSteps - 1) "Siguiente ➔" else "¡Entendido! ⭐",
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
