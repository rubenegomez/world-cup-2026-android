package com.example.worldcup2026.data.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.example.worldcup2026.data.local.WorldCupDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MatchReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val matchId = intent.getIntExtra("match_id", -1)
        val homeTeamName = intent.getStringExtra("home_team") ?: "Local"
        val awayTeamName = intent.getStringExtra("away_team") ?: "Visitante"
        val reminderType = intent.getStringExtra("reminder_type") ?: if (intent.getBooleanExtra("is_reminder", true)) "match_30m" else "start"
        val matchTimeMs = intent.getLongExtra("match_time", 0L)

        if (matchId == -1) return

        val database = WorldCupDatabase.getDatabase(context)
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val matchEntity = database.matchDao().getMatchById(matchId)
                val hasPrediction = matchEntity != null && 
                        (matchEntity.predictedHomeScore != null || !matchEntity.predictedWinner.isNullOrBlank())

                val currentTime = System.currentTimeMillis()

                val title: String
                val text: String
                val soundRes: Int
                val notifIdSuffix: Int

                if (matchTimeMs > 0L && currentTime >= matchTimeMs || reminderType == "start") {
                    title = "⚽ ¡Pitazo Inicial! ⚽"
                    text = "Comienza el partido entre $homeTeamName y $awayTeamName. ¡Que ruede el balón!"
                    soundRes = com.example.worldcup2026.R.raw.world_cup_whistle
                    notifIdSuffix = 2
                } else if (reminderType == "prode_35m") {
                    // Si ya hizo el prode, SE ANULA la notificación por completo
                    if (hasPrediction) {
                        return@launch
                    }
                    title = "⚠️ ¡COMPLETÁ TU PRODE! ⚠️"
                    text = "En 35 minutos arranca $homeTeamName vs $awayTeamName y aún no cargaste tu pronóstico."
                    soundRes = com.example.worldcup2026.R.raw.world_cup_whistle
                    notifIdSuffix = 3
                } else {
                    // reminderType == "match_30m"
                    title = "⏱ ¡Faltan 30 minutos! ⏱"
                    text = if (hasPrediction) {
                        "Faltan 30 minutos para el inicio de $homeTeamName vs $awayTeamName. ¡Tu pronóstico ya está registrado!"
                    } else {
                        "Faltan 30 minutos para el inicio del partido: $homeTeamName vs $awayTeamName."
                    }
                    soundRes = com.example.worldcup2026.R.raw.silbato
                    notifIdSuffix = 1
                }

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val soundUri = Uri.parse(
                    android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + 
                    "://" + context.packageName + "/" + soundRes
                )

                val builder = NotificationCompat.Builder(context, "world_cup_2026_notifications_v4")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(soundUri)
                    .setAutoCancel(true)

                manager.notify(matchId * 10 + notifIdSuffix, builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

