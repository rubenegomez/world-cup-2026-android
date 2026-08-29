package com.example.worldcup2026

import android.app.Application
import androidx.work.*
import com.example.worldcup2026.data.sync.SyncWorker
import com.example.worldcup2026.data.util.NotificationHelper
import com.example.worldcup2026.data.util.SoundManager
import com.google.android.gms.ads.MobileAds
import com.example.worldcup2026.data.util.AnalyticsManager
import java.util.concurrent.TimeUnit

class WorldCupApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        com.example.worldcup2026.ui.UnityAdsManager.init(this)
        AnalyticsManager.initialize(this)
        SoundManager.init(this)
        
        // Inicializar canales de notificaciones
        NotificationHelper.createNotificationChannel(this)
        
        // Configurar sincronización de fondo
        setupBackgroundSync()
    }

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WorldCupBackgroundSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
