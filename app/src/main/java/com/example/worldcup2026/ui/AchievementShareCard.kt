package com.example.worldcup2026.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
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

    val shareText = when (achievement.type) {
        AchievementType.TOURNAMENT_CHAMPION ->
            "¡Terminé en el puesto #${achievement.position ?: 1} en ${achievement.subtitle} sumando ${achievement.points} pts! 🏆 Sumate en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=${achievement.referralCode}"
        AchievementType.DAILY_TOP ->
            "¡Quedé en el puesto #${achievement.position ?: 1} del Ranking Global sumando ${achievement.points} pts! 🏆 Demostrá lo que sabés en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=${achievement.referralCode}"
        AchievementType.PERFECT_MATCH ->
            "¡Metí pleno exacto! 🎯 Sumate a mi torneo en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=${achievement.referralCode}"
        AchievementType.STREAK ->
            "¡Llevo racha imparable en el podio de Arena Prode! 🚀 Sumate: https://ellocodelpedal.duckdns.org/join?ref=${achievement.referralCode}"
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
                        text = "PRESUMIR LOGRO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preview Card
                AchievementCardPreview(achievement = achievement)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Seleccioná la red para compartir la tarjeta:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // WhatsApp
                    SocialShareButton("WhatsApp", Color(0xFF25D366)) {
                        shareAchievementWithImage(context, achievement, shareText, "com.whatsapp")
                    }

                    // Instagram
                    SocialShareButton("Instagram", Color(0xFFE4405F)) {
                        shareAchievementWithImage(context, achievement, shareText, "com.instagram.android")
                    }

                    // Telegram
                    SocialShareButton("Telegram", Color(0xFF0088CC)) {
                        shareAchievementWithImage(context, achievement, shareText, "org.telegram.messenger")
                    }

                    // X / Twitter
                    SocialShareButton("X (Twitter)", Color(0xFF1DA1F2)) {
                        shareAchievementWithImage(context, achievement, shareText, "com.twitter.android")
                    }

                    // General
                    SocialShareButton("Más...", Color(0xFF673AB7)) {
                        shareAchievementWithImage(context, achievement, shareText, null)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCardPreview(achievement: AchievementData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1E2E)
                    )
                )
            )
            .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = achievement.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64B5F6),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!achievement.userAvatarUrl.isNullOrEmpty()) {
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

            Spacer(modifier = Modifier.height(10.dp))

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

private fun shareAchievementWithImage(context: Context, achievement: AchievementData, text: String, packageName: String?) {
    val imageUri = generateAchievementCardBitmap(context, achievement)
    val intent = Intent(Intent.ACTION_SEND).apply {
        if (imageUri != null) {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }
        putExtra(Intent.EXTRA_TEXT, text)
        if (!packageName.isNullOrEmpty()) {
            setPackage(packageName)
        }
    }

    try {
        if (!packageName.isNullOrEmpty()) {
            context.startActivity(intent)
        } else {
            context.startActivity(Intent.createChooser(intent, "Presumir Logro"))
        }
    } catch (e: Exception) {
        val chooserIntent = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = if (imageUri != null) "image/png" else "text/plain"
                if (imageUri != null) {
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                putExtra(Intent.EXTRA_TEXT, text)
            },
            "Presumir Logro"
        )
        context.startActivity(chooserIntent)
    }
}

private fun generateAchievementCardBitmap(context: Context, achievement: AchievementData): Uri? {
    try {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background Gradient
        val bgPaint = android.graphics.Paint()
        bgPaint.shader = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intColor(0xFF0F172A), intColor(0xFF1E1E2E),
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Gold Frame
        val borderPaint = android.graphics.Paint().apply {
            color = intColor(0xFFFFD700)
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 24f
        }
        canvas.drawRoundRect(40f, 40f, width - 40f, height - 40f, 48f, 48f, borderPaint)

        val maxWidth = width - 160f

        // App Title
        val titlePaint = android.graphics.Paint().apply {
            color = intColor(0xFFFFD700)
            textSize = 52f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawFittedText(canvas, "ARENA PRODE Y TORNEOS", width / 2f, 220f, titlePaint, maxWidth)

        // Subtitle
        val subPaint = android.graphics.Paint().apply {
            color = intColor(0xFF64B5F6)
            textSize = 36f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawFittedText(canvas, achievement.subtitle, width / 2f, 300f, subPaint, maxWidth)

        // Trophy Emoji
        val trophyPaint = android.graphics.Paint().apply {
            color = intColor(0xFFFFD700)
            textSize = 140f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("🏆", width / 2f, 580f, trophyPaint)

        // Achievement Title
        val badgePaint = android.graphics.Paint().apply {
            color = intColor(0xFFFFFFFF)
            textSize = 54f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawFittedText(canvas, achievement.title, width / 2f, 780f, badgePaint, maxWidth)

        // User Name
        val userPaint = android.graphics.Paint().apply {
            color = intColor(0xFFFFC107)
            textSize = 64f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawFittedText(canvas, "👤 ${achievement.userName}", width / 2f, 960f, userPaint, maxWidth)

        // Points & Position
        val ptsPaint = android.graphics.Paint().apply {
            color = intColor(0xFF00E676)
            textSize = 72f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val posStr = if (achievement.position != null) " • PUESTO #${achievement.position}" else ""
        drawFittedText(canvas, "${achievement.points} PTS$posStr", width / 2f, 1140f, ptsPaint, maxWidth)

        // Footer Call To Action
        val refPaint = android.graphics.Paint().apply {
            color = intColor(0xFFFFFFFF)
            textSize = 34f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawFittedText(canvas, "Sumate y ganá +12h Sin Anuncios:", width / 2f, 1620f, refPaint, maxWidth)

        val linkPaint = android.graphics.Paint().apply {
            color = intColor(0xFFFFD700)
            textSize = 36f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawFittedText(canvas, "https://ellocodelpedal.duckdns.org/download/ArenaProde?ref=${achievement.referralCode}", width / 2f, 1700f, linkPaint, maxWidth)

        // Save PNG to cache
        val imagesFolder = File(context.cacheDir, "images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "achievement_card.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()

        return FileProvider.getUriForFile(context, "com.example.worldcup2026.fileprovider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

private fun drawFittedText(canvas: Canvas, text: String, centerX: Float, y: Float, paint: android.graphics.Paint, maxWidth: Float) {
    val originalTextSize = paint.textSize
    var currentSize = originalTextSize
    while (paint.measureText(text) > maxWidth && currentSize > 20f) {
        currentSize -= 2f
        paint.textSize = currentSize
    }
    canvas.drawText(text, centerX, y, paint)
    paint.textSize = originalTextSize
}

private fun intColor(colorLong: Long): Int = colorLong.toInt()
