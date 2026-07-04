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
        val isReminder = intent.getBooleanExtra("is_reminder", true) // true = 30min recordatorio, false = al arrancar

        if (matchId == -1) return

        val database = WorldCupDatabase.getDatabase(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            val matchEntity = database.matchDao().getMatchById(matchId)
            val hasPrediction = matchEntity != null && 
                    (matchEntity.predictedHomeScore != null || matchEntity.predictedWinner != null)

            val title: String
            val text: String
            val soundRes: Int

            if (isReminder) {
                // Alarma 30 minutos antes del partido
                if (hasPrediction) {
                    title = "⚽ ¡Todo Listo! ⚽"
                    text = "Tu pronóstico ya está registrado. En 30 minutos arranca: $homeTeamName vs $awayTeamName."
                    soundRes = com.example.worldcup2026.R.raw.silbato
                } else {
                    title = "⚠️ ¡COMPLETÁ TU PRODE! ⚠️"
                    text = "En 30 minutos arranca $homeTeamName vs $awayTeamName y aún no cargaste tu pronóstico."
                    soundRes = com.example.worldcup2026.R.raw.world_cup_whistle
                }
            } else {
                // Alarma al arrancar el partido
                title = "⏱️ ¡Pitazo Inicial! ⏱️"
                text = "Comienza el partido entre $homeTeamName y $awayTeamName. ¡Que ruede el balón!"
                soundRes = com.example.worldcup2026.R.raw.world_cup_whistle
            }

            // Enviar notificación
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val soundUri = Uri.parse(
                android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + 
                "://" + context.packageName + "/" + soundRes
            )

            val builder = NotificationCompat.Builder(context, "world_cup_2026_notifications")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setAutoCancel(true)

            manager.notify(matchId * 10 + (if (isReminder) 1 else 2), builder.build())
        }
    }
}
