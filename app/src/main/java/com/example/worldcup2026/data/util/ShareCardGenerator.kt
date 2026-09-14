package com.example.worldcup2026.data.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.worldcup2026.data.local.LeagueEntity
import java.io.File
import java.io.FileOutputStream

object ShareCardGenerator {

    fun generateLeagueCard(context: Context, league: LeagueEntity, tournamentDesc: String = ""): Uri? {
        try {
            val width = 1080
            val height = 1200
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Fondo degradado profesional
            val bgPaint = Paint().apply {
                isAntiAlias = true
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#0A192F"),
                        Color.parseColor("#020C1B"),
                        Color.parseColor("#052A4A"),
                        Color.parseColor("#021526")
                    ),
                    floatArrayOf(0f, 0.4f, 0.8f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // 2. Líneas deportivas decorativas de cancha
            val linePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#1E3A8A")
                alpha = 40
                style = Paint.Style.STROKE
                strokeWidth = 6f
            }
            canvas.drawCircle(width / 2f, height / 2f, 320f, linePaint)
            canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, linePaint)
            canvas.drawRect(60f, 60f, width - 60f, height - 60f, linePaint)

            // 3. Encabezado de la App
            val titlePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#00E676") // Verde Neón
                textSize = 52f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                setShadowLayer(16f, 0f, 4f, Color.parseColor("#00E676"))
            }
            canvas.drawText("🏟️ ARENA PRODE ⚽", width / 2f, 140f, titlePaint)

            val subtitlePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#94A3B8")
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("¡Te invitaron a participar de una Liga de Prode!", width / 2f, 210f, subtitlePaint)

            // 4. Tarjeta central de la Liga
            val cardRect = RectF(100f, 270f, width - 100f, 540f)
            val cardPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#132F4C")
                style = Paint.Style.FILL
            }
            val cardBorder = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#38BDF8")
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)
            canvas.drawRoundRect(cardRect, 32f, 32f, cardBorder)

            val leagueLabelPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#38BDF8")
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("NOMBRE DE LA LIGA", width / 2f, 330f, leagueLabelPaint)

            val leagueNamePaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 54f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val displayLeagueName = if (league.name.length > 25) league.name.take(22) + "..." else league.name
            canvas.drawText(displayLeagueName, width / 2f, 410f, leagueNamePaint)

            val descText = if (tournamentDesc.isNotBlank()) tournamentDesc else "Torneo Oficial"
            val leagueDescPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#CBD5E1")
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(descText, width / 2f, 480f, leagueDescPaint)

            // 5. Caja Dorada de CÓDIGO DE INVITACIÓN
            val codeBoxRect = RectF(120f, 590f, width - 120f, 850f)
            val codeBoxPaint = Paint().apply {
                isAntiAlias = true
                shader = LinearGradient(
                    120f, 590f, width - 120f, 850f,
                    intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#FFA000"), Color.parseColor("#FF8F00")),
                    null,
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(codeBoxRect, 28f, 28f, codeBoxPaint)

            val codeLabelPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#3E2723")
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("CÓDIGO DE INVITACIÓN", width / 2f, 660f, codeLabelPaint)

            val codeValPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#000000")
                textSize = 80f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.15f
            }
            canvas.drawText(league.code, width / 2f, 760f, codeValPaint)

            val codeSubPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#3E2723")
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Toca 'Unirse a Liga' en la app y escribe este código", width / 2f, 815f, codeSubPaint)

            // 6. Premio (si aplica)
            var nextY = 930f
            if (!league.customPrize.isNullOrBlank()) {
                val prizePaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#FFD54F")
                    textSize = 34f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🎁 Premio en juego: ${league.customPrize}", width / 2f, nextY, prizePaint)
                nextY += 70f
            }

            // 7. Pie de Tarjeta / Descarga
            val footerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#94A3B8")
                textSize = 26f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Descargá la app en: ellocodelpedal.duckdns.org", width / 2f, height - 90f, footerPaint)

            val brandPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#64748B")
                textSize = 22f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Desarrollado por El Loco del Pedal • Prode 2026", width / 2f, height - 50f, brandPaint)

            // Guardar imagen en caché
            val imagesDir = File(context.cacheDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val file = File(imagesDir, "liga_${league.code}.png")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun shareLeagueInvite(context: Context, league: LeagueEntity, tournamentDesc: String = "") {
        val imageUri = generateLeagueCard(context, league, tournamentDesc)
        
        val shareText = buildString {
            append("🏆 *¡SUMATE A MI LIGA DE PRODE!* ⚽\n\n")
            append("🏟️ *Liga:* ${league.name}\n")
            if (tournamentDesc.isNotBlank()) {
                append("📋 *Formato:* $tournamentDesc\n")
            }
            if (!league.customPrize.isNullOrBlank()) {
                append("🎁 *Premio:* ${league.customPrize}\n")
            }
            append("\n🔑 *Código de Invitación:* `${league.code}`\n\n")
            append("📲 Descargá la app o sumate directamente desde el enlace:\n")
            append("https://ellocodelpedal.duckdns.org/join?code=${league.code}")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            if (imageUri != null) {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        val chooser = Intent.createChooser(shareIntent, "Compartir Liga de Prode")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
