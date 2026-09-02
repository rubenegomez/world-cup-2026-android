package com.example.worldcup2026.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.worldcup2026.MainActivity
import com.example.worldcup2026.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import kotlinx.coroutines.launch
import com.example.worldcup2026.data.util.NotificationHelper

class WorldCupMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "From: ")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            
            // Validar que la notificación esté destinada a esta aplicación y no a Fondos / El Loco del Pedal
            val targetApp = remoteMessage.data["app"] ?: remoteMessage.data["platform"] ?: remoteMessage.data["target"]
            val targetPkg = remoteMessage.data["package"] ?: remoteMessage.data["packageName"]
            if (!targetPkg.isNullOrBlank() && !targetPkg.equals(packageName, ignoreCase = true)) {
                return
            }
            if (!targetApp.isNullOrBlank() && (targetApp.contains("fondos", ignoreCase = true) || targetApp.contains("locodelpedal", ignoreCase = true) || targetApp.contains("wallpaper", ignoreCase = true))) {
                return
            }

            val matchId = remoteMessage.data["matchId"]
            val eventType = remoteMessage.data["eventType"] // "goal", "start", "end", "upcoming_30m"
            val homeTeam = remoteMessage.data["homeTeam"] ?: "Local"
            val awayTeam = remoteMessage.data["awayTeam"] ?: "Visitante"
            val homeScore = remoteMessage.data["homeScore"]
            val awayScore = remoteMessage.data["awayScore"]

            val msgType = remoteMessage.data["type"]
            val downloadUrl = remoteMessage.data["downloadUrl"]
            if (msgType == "app_update" || downloadUrl != null && remoteMessage.data.containsKey("versionCode")) {
                val updateTitle = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "🏟️ ¡Nueva Versión Disponible!"
                val updateBody = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "Toca aquí para actualizar la aplicación."
                
                // Descartar si el título o cuerpo hace referencia a fondos u otra app
                if (updateTitle.contains("fondo", ignoreCase = true) || updateBody.contains("fondo", ignoreCase = true) || 
                    (downloadUrl != null && downloadUrl.contains("Fondos", ignoreCase = true))) {
                    return
                }

                sendUpdateNotification(updateTitle, updateBody, downloadUrl ?: "https://ellocodelpedal.duckdns.org/download/ArenaProde.apk")
                return
            }

            if (eventType == "upcoming_30m" && matchId != null) {
                // Check local Prode
                val mId = matchId.toIntOrNull() ?: return
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val db = com.example.worldcup2026.data.local.WorldCupDatabase.getDatabase(applicationContext)
                    val match = db.matchDao().getMatchById(mId)
                    val hasPredicted = match != null && (match.predictedHomeScore != null || !match.predictedWinner.isNullOrBlank())
                    
                    val title: String
                    val body: String
                    if (hasPredicted && match != null) {
                        title = "⚽ ¡En 30 minutos empieza!"
                        val predStr = if (match.predictedHomeScore != null && match.predictedAwayScore != null) {
                            " Tu pronóstico: ${match.predictedHomeScore} - ${match.predictedAwayScore}."
                        } else if (!match.predictedWinner.isNullOrBlank()) {
                            " Tu pronóstico: ${match.predictedWinner}."
                        } else ""
                        body = "El partido $homeTeam vs $awayTeam empieza en 30 minutos.$predStr"
                    } else {
                        title = "📝 Recordatorio de Prode"
                        body = "Recuerda hacer tu Prode para $homeTeam vs $awayTeam que empieza en 30 minutos."
                    }
                    sendNotification(title, body, matchId)
                }
                return // Do not process standard live events
            }

            val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "El Loco del Prode"
            val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "${homeTeam} vs ${awayTeam}"

            val prefs = getSharedPreferences("world_cup_prefs", Context.MODE_PRIVATE)
            val notifScope = prefs.getString("notif_scope", "ALL") ?: "ALL"

            // 1. Filtrado por tipo de evento (estrictamente los tipos configurados)
            val eventAllowed = when (eventType) {
                "goal" -> prefs.getBoolean("notif_goals", true)
                "start" -> prefs.getBoolean("notif_start", true)
                "end" -> prefs.getBoolean("notif_end", true)
                "yellow_card", "yellow" -> prefs.getBoolean("notif_yellow", true)
                "red_card", "red" -> prefs.getBoolean("notif_red", true)
                "sub", "substitution" -> prefs.getBoolean("notif_subs", true)
                "penalty" -> prefs.getBoolean("notif_penalties", true)
                "extra_time" -> prefs.getBoolean("notif_extra_time", true)
                "shootout" -> prefs.getBoolean("notif_shootout", true)
                else -> false // Ignorar cualquier otro evento no configurado
            }

            // 2. Filtrado por alcance (Torneos / Equipos Favoritos)
            val tournamentId = remoteMessage.data["tournamentId"]?.toIntOrNull() ?: remoteMessage.data["tournament_id"]?.toIntOrNull() ?: 0
            val favTournaments = prefs.getStringSet("favorite_tournament_ids", emptySet()) ?: emptySet()
            val favTeams = prefs.getStringSet("favorite_team_names", emptySet()) ?: emptySet()

            val isTournamentFav = tournamentId != 0 && favTournaments.contains(tournamentId.toString())
            val isTeamFav = (homeTeam.isNotBlank() && favTeams.any { homeTeam.contains(it, ignoreCase = true) }) ||
                            (awayTeam.isNotBlank() && favTeams.any { awayTeam.contains(it, ignoreCase = true) })

            val scopeAllowed = when (notifScope) {
                "FAV_TOURNAMENTS" -> isTournamentFav
                "FAV_TEAMS" -> isTeamFav
                "FAV_BOTH" -> isTournamentFav || isTeamFav
                else -> true // "ALL"
            }

            if (eventAllowed && scopeAllowed) {
                sendNotification(title, body, matchId, eventType)
                
                // Broadcast intent to show popup if app is in foreground
                val intent = Intent("com.example.worldcup2026.MATCH_EVENT").apply {
                    setPackage(packageName)
                    putExtra("match_id", matchId?.toIntOrNull() ?: -1)
                    putExtra("eventType", eventType)
                    putExtra("homeTeam", homeTeam)
                    putExtra("awayTeam", awayTeam)
                    putExtra("homeScore", homeScore)
                    putExtra("awayScore", awayScore)
                }
                sendBroadcast(intent)
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
    }

    private fun sendNotification(title: String, messageBody: String, matchId: String?, eventType: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (matchId != null) {
                putExtra("nav_match_id", matchId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, (System.currentTimeMillis() % 10000).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (eventType) {
            "goal" -> "world_cup_2026_goals_v4"
            "start", "end", "card" -> "world_cup_2026_incidents_v4"
            else -> "world_cup_2026_notifications_v4"
        }

        val soundRes = when (eventType) {
            "goal" -> R.raw.gooolll
            else -> R.raw.silbato
        }

        val soundUri = android.net.Uri.parse(
            android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + 
            "://" + packageName + "/" + soundRes
        )

        // Ensure notification channels exist with custom sounds
        NotificationHelper.createNotificationChannel(this)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Unificar ID por partido para que se actualice la notificación del mismo partido en vez de apilarse
        val notifId = matchId?.toIntOrNull() ?: (matchId?.hashCode() ?: 10001)
        notificationManager.notify(notifId, notificationBuilder.build())
    }

    private fun sendUpdateNotification(title: String, messageBody: String, downloadUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(downloadUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, (System.currentTimeMillis() % 10000).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        NotificationHelper.createNotificationChannel(this)

        val notificationBuilder = NotificationCompat.Builder(this, "world_cup_2026_notifications_v4")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(99999, notificationBuilder.build())
    }
}
