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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.worldcup2026.R
import com.example.worldcup2026.data.model.Team
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun CelebrationScreen(champion: Team, onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    
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
                text = "¡CAMPEÓN DEL MUNDO!",
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
        Color.White // Predomina el blanco como en el video
    } else {
        listOf(
            Color(0xFFFFD700), // Oro
            Color(0xFF2196F3), // Azul
            Color(0xFF4CAF50), // Verde
            Color(0xFFF44336)  // Rojo
        ).random()
    }
}
