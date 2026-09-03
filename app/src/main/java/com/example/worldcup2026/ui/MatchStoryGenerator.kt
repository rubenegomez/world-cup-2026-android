package com.example.worldcup2026.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class StoryEventType {
    PREVIA,          // Próximo Partido / Información
    ONCE_INICIAL,    // Formación Titular y Suplentes
    INICIO_PARTIDO,  // ¡Comenzó el Partido!
    GOL,             // ¡GOOOL! con Goleador y Marcador
    ENTRETIEMPO,     // Final del Primer Tiempo
    FINAL_PARTIDO    // Resultado Final
}

data class MatchStoryData(
    val match: Match,
    val eventType: StoryEventType = StoryEventType.PREVIA,
    val selectedTeam: Team? = null,
    val scorerName: String? = null,
    val minute: String? = null,
    val lineupPlayers: List<String> = emptyList(),
    val benchPlayers: List<String> = emptyList(),
    val coachName: String? = null
)

@Composable
fun MatchStoryShareModal(
    storyData: MatchStoryData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedType by remember { mutableStateOf(storyData.eventType) }
    var selectedTeam by remember { mutableStateOf(storyData.selectedTeam ?: storyData.match.homeTeam) }
    var isGenerating by remember { mutableStateOf(false) }

    val types = listOf(
        StoryEventType.PREVIA to "⚔️ Previa",
        StoryEventType.ONCE_INICIAL to "📋 11 Inicial",
        StoryEventType.INICIO_PARTIDO to "▶️ Inicio",
        StoryEventType.GOL to "⚽ Gol",
        StoryEventType.ENTRETIEMPO to "⏳ Entretiempo",
        StoryEventType.FINAL_PARTIDO to "🏆 Final"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PUBLICAR HISTORIA (9:16)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Selector de Tipo de Historia
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(types) { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFD700),
                                selectedLabelColor = Color.Black,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                // Selector de Club (Para Once Inicial o Gol)
                if (selectedType == StoryEventType.ONCE_INICIAL || selectedType == StoryEventType.GOL) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ClubSelectChip(
                            team = storyData.match.homeTeam,
                            isSelected = selectedTeam.id == storyData.match.homeTeam.id,
                            onClick = { selectedTeam = storyData.match.homeTeam }
                        )
                        ClubSelectChip(
                            team = storyData.match.awayTeam,
                            isSelected = selectedTeam.id == storyData.match.awayTeam.id,
                            onClick = { selectedTeam = storyData.match.awayTeam }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Previsualización de la Tarjeta
                StoryPreviewBox(
                    match = storyData.match,
                    type = selectedType,
                    team = selectedTeam,
                    scorer = storyData.scorerName,
                    minute = storyData.minute
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de Compartir
                if (isGenerating) {
                    CircularProgressIndicator(color = Color(0xFFFFD700), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Generando imagen HD...", color = Color.White, fontSize = 12.sp)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isGenerating = true
                                    val currentStory = storyData.copy(eventType = selectedType, selectedTeam = selectedTeam)
                                    shareStoryImage(context, currentStory, "com.whatsapp")
                                    isGenerating = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("WhatsApp", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isGenerating = true
                                    val currentStory = storyData.copy(eventType = selectedType, selectedTeam = selectedTeam)
                                    shareStoryImage(context, currentStory, null)
                                    isGenerating = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compartir", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClubSelectChip(team: Team, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFFFFD700).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700) else Color.Transparent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (!team.flagUrl.isNullOrBlank()) {
                AsyncImage(
                    model = team.flagUrl,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = team.name,
                color = if (isSelected) Color(0xFFFFD700) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun StoryPreviewBox(
    match: Match,
    type: StoryEventType,
    team: Team,
    scorer: String?,
    minute: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E1E2E))
                )
            )
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val titleText = when (type) {
                StoryEventType.PREVIA -> "⚔️ PRÓXIMO PARTIDO"
                StoryEventType.ONCE_INICIAL -> "📋 ONCE INICIAL • ${team.name.uppercase()}"
                StoryEventType.INICIO_PARTIDO -> "▶️ ¡COMENZÓ EL PARTIDO!"
                StoryEventType.GOL -> "⚽ ¡GOOOOL DE ${team.name.uppercase()}!"
                StoryEventType.ENTRETIEMPO -> "⏳ FINAL DEL PRIMER TIEMPO"
                StoryEventType.FINAL_PARTIDO -> "🏆 RESULTADO FINAL"
            }

            Text(
                text = titleText,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!match.homeTeam.flagUrl.isNullOrBlank()) {
                        AsyncImage(model = match.homeTeam.flagUrl, contentDescription = null, modifier = Modifier.size(44.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(match.homeTeam.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (type == StoryEventType.PREVIA) {
                        Text(match.date?.substringAfter(" ") ?: "VS", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(match.date?.substringBefore(" ") ?: "", color = Color.Gray, fontSize = 10.sp)
                    } else {
                        val h = match.homeScore ?: 0
                        val a = match.awayScore ?: 0
                        Text("$h - $a", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        if (type == StoryEventType.GOL && !minute.isNullOrBlank()) {
                            Text("$minute'", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!match.awayTeam.flagUrl.isNullOrBlank()) {
                        AsyncImage(model = match.awayTeam.flagUrl, contentDescription = null, modifier = Modifier.size(44.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(match.awayTeam.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (type == StoryEventType.GOL && !scorer.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("⚽ $scorer", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "📲 Seguí el partido en vivo en Arena Prode",
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

suspend fun shareStoryImage(
    context: Context,
    story: MatchStoryData,
    packageName: String? = null
) {
    val imageUri = withContext(Dispatchers.IO) {
        generateStoryBitmap(context, story)
    }

    if (imageUri == null) {
        Toast.makeText(context, "Error al generar la imagen de la historia", Toast.LENGTH_SHORT).show()
        return
    }

    val match = story.match
    val h = match.homeScore ?: 0
    val a = match.awayScore ?: 0
    val teamName = story.selectedTeam?.name ?: match.homeTeam.name

    val shareText = when (story.eventType) {
        StoryEventType.PREVIA ->
            "🔥 ¡Se viene ${match.homeTeam.name} vs ${match.awayTeam.name}! Seguí el minuto a minuto y jugá gratis al Prode en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=story"
        StoryEventType.ONCE_INICIAL ->
            "📋 ¡Formación confirmada de $teamName para enfrentar a ${if (teamName == match.homeTeam.name) match.awayTeam.name else match.homeTeam.name}! Miralo en vivo en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=story"
        StoryEventType.INICIO_PARTIDO ->
            "⚽ ¡Ya comenzó ${match.homeTeam.name} vs ${match.awayTeam.name}! Seguí los goles en tiempo real en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=story"
        StoryEventType.GOL ->
            "⚽ ¡GOOOOOOL DE ${teamName.uppercase()}! ${story.scorerName ?: ""} ($h - $a). Viví el fútbol en directo en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=story"
        StoryEventType.ENTRETIEMPO ->
            "⏳ ¡Final del Primer Tiempo! ${match.homeTeam.name} $h - $a ${match.awayTeam.name}. Mirá las estadísticas en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=story"
        StoryEventType.FINAL_PARTIDO ->
            "🏆 ¡Final del Partido! ${match.homeTeam.name} $h - $a ${match.awayTeam.name}. Revisá la tabla de posiciones y sumá puntos en Arena Prode: https://ellocodelpedal.duckdns.org/join?ref=story"
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        putExtra(Intent.EXTRA_TEXT, shareText)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (!packageName.isNullOrEmpty()) {
            setPackage(packageName)
        }
    }

    try {
        if (!packageName.isNullOrEmpty()) {
            context.startActivity(intent)
        } else {
            context.startActivity(Intent.createChooser(intent, "Compartir Historia"))
        }
    } catch (e: Exception) {
        val chooserIntent = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Compartir Historia"
        )
        context.startActivity(chooserIntent)
    }
}

private suspend fun generateStoryBitmap(context: Context, story: MatchStoryData): Uri? {
    return try {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint()
        bgPaint.shader = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intColor(0xFF0B132B), intColor(0xFF1C2541),
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val borderPaint = Paint().apply {
            color = intColor(0xFFFFD700)
            style = Paint.Style.STROKE
            strokeWidth = 20f
        }
        canvas.drawRoundRect(40f, 40f, width - 40f, height - 40f, 44f, 44f, borderPaint)

        val match = story.match
        val h = match.homeScore ?: 0
        val a = match.awayScore ?: 0
        val teamName = (story.selectedTeam ?: match.homeTeam).name

        val appTitlePaint = Paint().apply {
            color = intColor(0xFFFFD700)
            textSize = 42f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ARENA PRODE & TORNEOS", width / 2f, 160f, appTitlePaint)

        val eventTitle = when (story.eventType) {
            StoryEventType.PREVIA -> "PRÓXIMO PARTIDO"
            StoryEventType.ONCE_INICIAL -> "ONCE INICIAL"
            StoryEventType.INICIO_PARTIDO -> "¡COMENZÓ EL PARTIDO!"
            StoryEventType.GOL -> "¡GOOOOOOOL!"
            StoryEventType.ENTRETIEMPO -> "ENTRETIEMPO"
            StoryEventType.FINAL_PARTIDO -> "RESULTADO FINAL"
        }

        val eventPaint = Paint().apply {
            color = if (story.eventType == StoryEventType.GOL) intColor(0xFFFFD700) else intColor(0xFFFFFFFF)
            textSize = if (story.eventType == StoryEventType.GOL) 74f else 62f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(eventTitle, width / 2f, 260f, eventPaint)

        val homeLogo = fetchBitmap(context, match.homeTeam.flagUrl)
        val awayLogo = fetchBitmap(context, match.awayTeam.flagUrl)

        val logoY = 430f
        val logoSize = 190f

        if (homeLogo != null) {
            canvas.drawBitmap(homeLogo, null, RectF(160f, logoY, 160f + logoSize, logoY + logoSize), null)
        }
        if (awayLogo != null) {
            canvas.drawBitmap(awayLogo, null, RectF(width - 160f - logoSize, logoY, width - 160f, logoY + logoSize), null)
        }

        val teamTextPaint = Paint().apply {
            color = intColor(0xFFFFFFFF)
            textSize = 34f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(match.homeTeam.name, 160f + (logoSize / 2f), logoY + logoSize + 48f, teamTextPaint)
        canvas.drawText(match.awayTeam.name, width - 160f - (logoSize / 2f), logoY + logoSize + 48f, teamTextPaint)

        val centerPaint = Paint().apply {
            color = intColor(0xFFFFD700)
            textSize = if (story.eventType == StoryEventType.PREVIA) 44f else 96f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        if (story.eventType == StoryEventType.PREVIA) {
            canvas.drawText("VS", width / 2f, logoY + 110f, centerPaint)
            val datePaint = Paint().apply {
                color = intColor(0xFF90CAF9)
                textSize = 32f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(match.date ?: "Por Definir", width / 2f, logoY + 165f, datePaint)
        } else {
            canvas.drawText("$h - $a", width / 2f, logoY + 130f, centerPaint)
            if (!story.minute.isNullOrBlank()) {
                val minPaint = Paint().apply {
                    color = intColor(0xFF4CAF50)
                    textSize = 36f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("${story.minute}'", width / 2f, logoY + 185f, minPaint)
            }
        }

        val contentBoxPaint = Paint().apply {
            color = intColor(0xCC131C31)
            style = Paint.Style.FILL
        }
        val contentBorder = Paint().apply {
            color = intColor(0x80FFD700)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val boxRect = RectF(100f, 750f, width - 100f, 1580f)
        canvas.drawRoundRect(boxRect, 32f, 32f, contentBoxPaint)
        canvas.drawRoundRect(boxRect, 32f, 32f, contentBorder)

        if (story.eventType == StoryEventType.ONCE_INICIAL) {
            val secTitlePaint = Paint().apply {
                color = intColor(0xFFFFD700)
                textSize = 40f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("TITULARES • $teamName", width / 2f, 830f, secTitlePaint)

            val playerPaint = Paint().apply {
                color = intColor(0xFFFFFFFF)
                textSize = 34f
                textAlign = Paint.Align.LEFT
            }

            val samplePlayers = if (story.lineupPlayers.isNotEmpty()) story.lineupPlayers else listOf(
                "1. Arquero", "4. Lateral Derecho", "2. Defensor Central",
                "6. Defensor Central", "3. Lateral Izquierdo", "8. Mediocampista",
                "5. Volante Central", "10. Enganche / Creador", "7. Extremo Derecho",
                "9. Delantero Centro", "11. Extremo Izquierdo"
            )

            var startY = 910f
            samplePlayers.take(11).forEachIndexed { index, p ->
                val colX = if (index < 6) 150f else 580f
                val rowY = if (index < 6) startY + (index * 70f) else startY + ((index - 6) * 70f)
                canvas.drawText(p, colX, rowY, playerPaint)
            }

            val dtPaint = Paint().apply {
                color = intColor(0xFF64B5F6)
                textSize = 32f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("DT: ${story.coachName ?: "Cuerpo Técnico"}", width / 2f, 1510f, dtPaint)

        } else if (story.eventType == StoryEventType.GOL) {
            val goalBigPaint = Paint().apply {
                color = intColor(0xFFFFD700)
                textSize = 50f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("AUTOR DEL GOL", width / 2f, 960f, goalBigPaint)

            val scorerPaint = Paint().apply {
                color = intColor(0xFFFFFFFF)
                textSize = 58f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val resolvedScorer = story.scorerName ?: match.scorers.lastOrNull() ?: "¡Gran Remate!"
            canvas.drawText("⚽ $resolvedScorer", width / 2f, 1070f, scorerPaint)

            if (!story.minute.isNullOrBlank()) {
                val mPaint = Paint().apply {
                    color = intColor(0xFF4CAF50)
                    textSize = 44f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Minuto ${story.minute}'", width / 2f, 1160f, mPaint)
            }
        } else {
            val infoTitlePaint = Paint().apply {
                color = intColor(0xFFFFD700)
                textSize = 42f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val textHeader = if (story.eventType == StoryEventType.FINAL_PARTIDO) "GOLEADORES E INCIDENCIAS" else "INFORMACIÓN DEL ENCUENTRO"
            canvas.drawText(textHeader, width / 2f, 840f, infoTitlePaint)

            val detailPaint = Paint().apply {
                color = intColor(0xFFE2E8F0)
                textSize = 34f
                textAlign = Paint.Align.CENTER
            }

            if (match.scorers.isNotEmpty()) {
                var yScorer = 930f
                match.scorers.take(7).forEach { sc ->
                    canvas.drawText("⚽ $sc", width / 2f, yScorer, detailPaint)
                    yScorer += 60f
                }
            } else {
                canvas.drawText("🏆 Partido Oficial", width / 2f, 980f, detailPaint)
                canvas.drawText("🏟️ Sede / Estadio Oficial", width / 2f, 1060f, detailPaint)
                canvas.drawText("⭐ Pronosticá gratis en el Prode", width / 2f, 1140f, detailPaint)
            }
        }

        val footerPaint = Paint().apply {
            color = intColor(0xFFFFD700)
            textSize = 32f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("📲 SEGUÍ EL PARTIDO EN VIVO & JUGÁ AL PRODE", width / 2f, 1690f, footerPaint)

        val linkPaint = Paint().apply {
            color = intColor(0xFF90CAF9)
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Descargala gratis en: ellocodelpedal.duckdns.org", width / 2f, 1750f, linkPaint)

        val file = File(context.cacheDir, "story_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun fetchBitmap(context: Context, url: String?): Bitmap? {
    if (url.isNullOrBlank()) return null
    return withContext(Dispatchers.IO) {
        try {
            val loader = context.imageLoader
            val req = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = loader.execute(req)
            if (result is SuccessResult) {
                (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

private fun intColor(colorLong: Long): Int = colorLong.toInt()
