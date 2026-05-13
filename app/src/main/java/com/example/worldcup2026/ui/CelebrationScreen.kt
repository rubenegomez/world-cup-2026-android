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
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotate by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Confetti Effect
        ConfettiLayer()

        // Botón cerrar arriba a la derecha por si el de abajo no se ve
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 64.dp)
        ) {
            Text(
                text = "¡CAMPEÓN DEL MUNDO!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Yellow,
                fontWeight = FontWeight.Black,
                modifier = Modifier.graphicsLayer { rotationZ = rotate }
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Trophy with scaling
            Box(contentAlignment = Alignment.Center) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(Color.Yellow.copy(alpha = 0.15f), CircleShape)
                        .scale(scale * 1.8f)
                )
                
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.trophy),
                    contentDescription = null,
                    modifier = Modifier
                        .size(240.dp)
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
                    .size(80.dp)
                    .clip(CircleShape),
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text("CERRAR FESTEJO", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ConfettiLayer() {
    val particles = remember { List(60) { ConfettiParticle() } }
    
    particles.forEach { particle ->
        val transition = rememberInfiniteTransition()
        val yPos by transition.animateFloat(
            initialValue = -100f,
            targetValue = 2000f,
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
                .size(particle.size.dp)
                .background(particle.color)
                .graphicsLayer { 
                    rotationZ = yPos * 0.5f 
                    alpha = 0.8f
                }
        )
    }
}

class ConfettiParticle {
    val xStart = Random.nextInt(0, 500).toFloat()
    val drift = Random.nextInt(-100, 100).toFloat()
    val duration = Random.nextInt(3500, 7000)
    val delay = Random.nextInt(0, 4000)
    val size = Random.nextInt(4, 12)
    val color = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFFFFFF), // White
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFF44336)  // Red
    ).random()
}
