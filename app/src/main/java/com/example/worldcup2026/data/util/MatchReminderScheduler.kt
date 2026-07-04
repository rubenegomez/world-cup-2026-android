package com.example.worldcup2026.data.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.worldcup2026.data.model.Match
import java.text.SimpleDateFormat
import java.util.*

object MatchReminderScheduler {

    fun scheduleRemindersForMatches(context: Context, matches: List<Match>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentTime = System.currentTimeMillis()

        matches.forEach { match ->
            // Solo agendar para partidos programados
            if (match.status != "Scheduled") return@forEach

            try {
                val matchDate = sdf.parse(match.date) ?: return@forEach
                val matchTimeMs = matchDate.time

                // 1. Alarma de recordatorio (30 minutos antes)
                val reminderTimeMs = matchTimeMs - (30 * 60 * 1000)
                if (reminderTimeMs > currentTime) {
                    val intent = Intent(context, MatchReminderReceiver::class.java).apply {
                        putExtra("match_id", match.id)
                        putExtra("home_team", match.homeTeam?.name)
                        putExtra("away_team", match.awayTeam?.name)
                        putExtra("is_reminder", true)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        match.id * 10 + 1,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMs,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMs,
                            pendingIntent
                        )
                    }
                }

                // 2. Alarma de comienzo (Exactamente a la hora de inicio)
                if (matchTimeMs > currentTime) {
                    val intent = Intent(context, MatchReminderReceiver::class.java).apply {
                        putExtra("match_id", match.id)
                        putExtra("home_team", match.homeTeam?.name)
                        putExtra("away_team", match.awayTeam?.name)
                        putExtra("is_reminder", false)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        match.id * 10 + 2,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            matchTimeMs,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            matchTimeMs,
                            pendingIntent
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
