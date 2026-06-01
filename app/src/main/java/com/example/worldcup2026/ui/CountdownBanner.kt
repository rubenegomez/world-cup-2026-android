package com.example.worldcup2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.worldcup2026.data.util.NotificationHelper
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.delay

@Composable
fun CountdownBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    // 1. Definición de la fecha objetivo (Inauguración Mundial: 11 de Junio, 2026)
    val targetDateTime = remember {
        ZonedDateTime.of(
            LocalDateTime.of(2026, 6, 11, 0, 0, 0),
            ZoneId.systemDefault()
        ).toInstant()
    }

    // States para almacenar el tiempo restante de forma reactiva
    var days by remember { mutableStateOf("00") }
    var hours by remember { mutableStateOf("00") }
    var minutes by remember { mutableStateOf("00") }
    var seconds by remember { mutableStateOf("00") }
    var isEventStarted by remember { mutableStateOf(false) }

    // 2. Bucle del temporizador acoplado al ciclo de vida del Composable
    LaunchedEffect(key1 = targetDateTime) {
        var notificationShown = false
        while (!isEventStarted) {
            try {
                val now = Instant.now()
                if (now.isAfter(targetDateTime) || now.equals(targetDateTime)) {
                    isEventStarted = true
                    if (!notificationShown) {
                        NotificationHelper.showStartNotification(context)
                        notificationShown = true
                    }
                } else {
                    val duration = Duration.between(now, targetDateTime)
                    
                    val d = duration.toDays()
                    val h = duration.toHours() % 24
                    val m = duration.toMinutes() % 60
                    val s = duration.getSeconds() % 60

                    // Formateo con ceros a la izquierda para mantener la simetría
                    days = String.format("%02d", d)
                    hours = String.format("%02d", h)
                    minutes = String.format("%02d", m)
                    seconds = String.format("%02d", s)
                }
            } catch (e: Exception) {
                // Fallback seguro en caso de error del sistema de tiempo del dispositivo
                isEventStarted = true
                if (!notificationShown) {
                    NotificationHelper.showStartNotification(context)
                    notificationShown = true
                }
            }
            delay(1000L) // Actualización cada segundo
        }
    }

    // 3. Renderizado de la Interfaz (Estructura de la Pancarta con diseño Premium)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B263B).copy(alpha = 0.85f),
                            Color(0xFF0D1B2A).copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isEventStarted) {
                    // Estado: Evento Iniciado
                    Text(
                        text = "⚽ ¡EL MUNDIAL HA COMENZADO! ⚽",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFFFFD700)
                    )
                } else {
                    // Estado: Cuenta Regresiva Activa
                    Text(
                        text = "CAMINO AL MUNDIAL 2026",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFFFFD700)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TimeUnitDisplay(value = days, label = "DÍAS")
                        TimeDivider()
                        TimeUnitDisplay(value = hours, label = "HRS")
                        TimeDivider()
                        TimeUnitDisplay(value = minutes, label = "MIN")
                        TimeDivider()
                        TimeUnitDisplay(value = seconds, label = "SEG")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeUnitDisplay(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(55.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            ),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun TimeDivider() {
    Text(
        text = ":",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
        color = Color(0xFFFFD700).copy(alpha = 0.5f),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}
