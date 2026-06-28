package com.example.worldcup2026.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.worldcup2026.R

object NotificationHelper {
    private const val CHANNEL_GENERAL_ID = "world_cup_2026_notifications"
    private const val CHANNEL_GOALS_ID = "world_cup_2026_goals"
    private const val CHANNEL_INCIDENTS_ID = "world_cup_2026_incidents"
    private const val NOTIFICATION_ID = 2026

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // 1. Canal General (Whistle / Countdown)
            val soundGeneralUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + 
                "://" + context.packageName + "/" + R.raw.world_cup_whistle
            )
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL_ID,
                "Mundial 2026 General",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones generales y cuenta regresiva"
                setSound(soundGeneralUri, attributes)
                enableVibration(true)
            }
            manager.createNotificationChannel(generalChannel)

            // 2. Canal de Goles (Goolll)
            val soundGoalUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + 
                "://" + context.packageName + "/" + R.raw.gooolll
            )
            val goalChannel = NotificationChannel(
                CHANNEL_GOALS_ID,
                "Mundial 2026 Goles",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de goles en tiempo real"
                setSound(soundGoalUri, attributes)
                enableVibration(true)
            }
            manager.createNotificationChannel(goalChannel)

            // 3. Canal de Incidentes (Silbato)
            val soundIncidentUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + 
                "://" + context.packageName + "/" + R.raw.silbato
            )
            val incidentChannel = NotificationChannel(
                CHANNEL_INCIDENTS_ID,
                "Mundial 2026 Incidentes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de tarjetas rojas, fin de partido y eventos"
                setSound(soundIncidentUri, attributes)
                enableVibration(true)
            }
            manager.createNotificationChannel(incidentChannel)
        }
    }

    fun showStartNotification(context: Context) {
        val soundUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + 
            "://" + context.packageName + "/" + R.raw.world_cup_whistle
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_GENERAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚽ ¡EL MUNDIAL HA COMENZADO! ⚽")
            .setContentText("El pitazo inicial ha sonado en México. ¡Que ruede el balón!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    fun showMatchIncidentNotification(context: Context, title: String, message: String, isGoal: Boolean = false) {
        val channelId = if (isGoal) CHANNEL_GOALS_ID else CHANNEL_INCIDENTS_ID
        val soundRes = if (isGoal) R.raw.gooolll else R.raw.silbato
        
        val soundUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + 
            "://" + context.packageName + "/" + soundRes
        )

        val uniqueNotificationId = (System.currentTimeMillis() % 100000).toInt()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(uniqueNotificationId, builder.build())
    }
}
