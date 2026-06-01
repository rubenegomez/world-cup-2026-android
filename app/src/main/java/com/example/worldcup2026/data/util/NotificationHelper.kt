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
    private const val CHANNEL_ID = "world_cup_2026_notifications"
    private const val CHANNEL_NAME = "Mundial 2026 Countdown"
    private const val CHANNEL_DESC = "Notificaciones para el inicio del Mundial 2026"
    private const val NOTIFICATION_ID = 2026

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + 
                "://" + context.packageName + "/" + R.raw.world_cup_whistle
            )
            
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                setSound(soundUri, attributes)
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showStartNotification(context: Context) {
        val soundUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + 
            "://" + context.packageName + "/" + R.raw.world_cup_whistle
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback simple para icono
            .setContentTitle("⚽ ¡EL MUNDIAL HA COMENZADO! ⚽")
            .setContentText("El pitazo inicial ha sonado en México. ¡Que ruede el balón!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }
}
