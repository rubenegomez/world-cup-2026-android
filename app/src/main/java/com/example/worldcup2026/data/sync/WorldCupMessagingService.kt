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
            Log.d("FCM", "Message data payload: ")
            
            val matchId = remoteMessage.data["matchId"]
            val eventType = remoteMessage.data["eventType"] // "goal", "start", "end", "upcoming_30m"
            val homeTeam = remoteMessage.data["homeTeam"] ?: "Local"
            val awayTeam = remoteMessage.data["awayTeam"] ?: "Visitante"
            val homeScore = remoteMessage.data["homeScore"]
            val awayScore = remoteMessage.data["awayScore"]

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

            val title = remoteMessage.notification?.title ?: "World Cup Update"
            val body = remoteMessage.notification?.body ?: "${homeTeam} vs ${awayTeam}"

            val prefs = getSharedPreferences("world_cup_prefs", Context.MODE_PRIVATE)
            val receiveAll = prefs.getBoolean("notif_all", true)
            val receiveGoals = prefs.getBoolean("notif_goals", true)
            val receiveStartEnd = prefs.getBoolean("notif_start_end", true)

            var shouldShow = receiveAll
            if (!shouldShow) {
                if (eventType == "goal" && receiveGoals) shouldShow = true
                if ((eventType == "start" || eventType == "end") && receiveStartEnd) shouldShow = true
            }

            if (shouldShow) {
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
            "goal" -> "world_cup_2026_goals"
            "start", "end", "card" -> "world_cup_2026_incidents"
            else -> "world_cup_2026_notifications"
        }

        val soundRes = when (eventType) {
            "goal" -> R.raw.gooolll
            "start", "end", "card" -> R.raw.silbato
            else -> R.raw.world_cup_whistle
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
        val notifId = ((matchId?.hashCode() ?: 0) * 31) + (eventType?.hashCode() ?: 0)
        notificationManager.notify(notifId, notificationBuilder.build())
    }
}
