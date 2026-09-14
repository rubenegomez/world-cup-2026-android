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
        
        // Actualizar proveedores de seguridad TLS para compatibilidad con Android 7-9 (J7 Neo / A5 Pro)
        try {
            com.google.android.gms.security.ProviderInstaller.installIfNeededAsync(this, object : com.google.android.gms.security.ProviderInstaller.ProviderInstallListener {
                override fun onProviderInstalled() {
                    android.util.Log.d("WorldCupApp", "TLS Security Provider instalado con éxito")
                }
                override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: android.content.Intent?) {
                    android.util.Log.w("WorldCupApp", "No se pudo actualizar TLS Security Provider: $errorCode")
                }
            })
        } catch (e: Throwable) {
            e.printStackTrace()
        }

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
