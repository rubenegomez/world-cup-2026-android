package com.example.worldcup2026.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.worldcup2026.R
import java.io.File
import java.io.FileOutputStream

enum class AchievementType {
    TOURNAMENT_CHAMPION,
    DAILY_TOP,
    PERFECT_MATCH,
    STREAK
}

data class AchievementData(
    val userName: String,
    val userAvatarUrl: String? = null,
    val type: AchievementType,
    val title: String,
    val subtitle: String,
    val points: Int,
    val position: Int? = null,
    val referralCode: String = "prode"
)

@Composable
fun ShareAchievementModal(
    achievement: AchievementData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSharing by remember { mutableStateOf(false) }

    val shareText = when (achievement.type) {
        AchievementType.TOURNAMENT_CHAMPION ->
            "¡Terminé en el puesto #${achievement.position ?: 1} en el torneo ${achievement.subtitle} sumando ${achievement.points} pts! 🏆 Sumate a la próxima fecha en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=${achievement.referralCode}"
        AchievementType.DAILY_TOP ->
            "¡Quedé en el puesto #${achievement.position ?: 1} del Ranking Global Diario sumando ${achievement.points} pts! 🏆 Demostrá lo que sabés en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=${achievement.referralCode}"
        AchievementType.PERFECT_MATCH ->
            "¡Metí pleno exacto en esta fecha! 🎯 Sumate a mi torneo en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=${achievement.referralCode}"
        AchievementType.STREAK ->
            "¡Llevo racha imparable en la zona de podio en Arena Prode! 🚀 Sumate con mi link: https://ellocodelpedal.duckdns.org/join?ref=${achievement.referralCode}"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141421))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMPARTIR LOGRO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            letterSpacing = 1.sp
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tarjeta 9:16 previa
                AchievementCardPreview(achievement = achievement)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¡Al invitar a un amigo sumás +12 HORAS SIN ANUNCIOS gratis! 🎁",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Yellow,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Redes Sociales
                Text(
                    text = "Seleccioná la red social:",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SocialShareButton(
                        name = "WhatsApp",
                        bgColor = Color(0xFF25D366),
                        onClick = {
                            shareTextDirectly(context, shareText, "com.whatsapp")
                        }
                    )

                    SocialShareButton(
                        name = "Instagram",
                        bgColor = Color(0xFFE1306C),
                        onClick = {
                            shareTextDirectly(context, shareText, "com.instagram.android")
                        }
                    )

                    SocialShareButton(
                        name = "Facebook",
                        bgColor = Color(0xFF1877F2),
                        onClick = {
                            shareTextDirectly(context, shareText, "com.facebook.katana")
                        }
                    )

                    SocialShareButton(
                        name = "X (Twitter)",
                        bgColor = Color(0xFF000000),
                        onClick = {
                            shareTextDirectly(context, shareText, "com.twitter.android")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón General
                Button(
                    onClick = {
                        shareTextDirectly(context, shareText, null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("COMPARTIR EN OTRAS APPS", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AchievementCardPreview(achievement: AchievementData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF0F0F1B), Color(0xFF232338))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // Insignia
            Icon(
                imageVector = when (achievement.type) {
                    AchievementType.TOURNAMENT_CHAMPION -> Icons.Default.EmojiEvents
                    else -> Icons.Default.Stars
                },
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.Yellow,
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = achievement.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!achievement.userAvatarUrl.isNull_or_Empty()) {
                    AsyncImage(
                        model = achievement.userAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon_main),
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = achievement.userName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
            ) {
                Text(
                    text = "🏆 ${achievement.points} PTS ${if (achievement.position != null) "• Puesto #${achievement.position}" else ""}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }
    }
}

@Composable
private fun SocialShareButton(
    name: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
        )
    }
}

private fun String?.isNull_or_Empty(): Boolean = this == null || this.trim().isEmpty()

private fun shareTextDirectly(context: Context, text: String, packageName: String?) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        if (!packageName.isNullOrEmpty()) {
            setPackage(packageName)
        }
    }
    try {
        if (!packageName.isNullOrEmpty()) {
            context.startActivity(intent)
        } else {
            context.startActivity(Intent.createChooser(intent, "Compartir Logro"))
        }
    } catch (e: Exception) {
        // Fallback al chooser general si la app no está instalada
        val chooserIntent = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            },
            "Compartir Logro"
        )
        context.startActivity(chooserIntent)
    }
}
