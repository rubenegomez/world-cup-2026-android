package com.example.worldcup2026

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.worldcup2026.ui.theme.WorldCup2026Theme
import com.example.worldcup2026.ui.MainScreen
import com.example.worldcup2026.data.RemoteConfigManager
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

import android.os.Build
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.worldcup2026.data.util.NotificationHelper

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Si el permiso es concedido o no, ya tenemos el canal registrado
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalamos el Splash nativo antes de super.onCreate
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Inicializamos canal de notificaciones
        NotificationHelper.createNotificationChannel(this)
        
        // Solicitar permisos en Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Inicializamos Remote Config para IDs dinámicos
        RemoteConfigManager.init()
        
        // Firebase Cloud Messaging: Suscribirse a goles/eventos
        FirebaseMessaging.getInstance().subscribeToTopic("live_matches_updates")
            .addOnCompleteListener { task ->
                var msg = "Subscribed to live_matches_updates"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d("FCM", msg)
            }
        
        FirebaseMessaging.getInstance().subscribeToTopic("upcoming_matches_30m")
            .addOnCompleteListener { task ->
                Log.d("FCM", "Subscribed to upcoming_matches_30m: ${task.isSuccessful}")
            }
            
        val navMatchId = intent?.getStringExtra("nav_match_id")
        val dataUri = intent?.data
        val joinCodeFromUri = dataUri?.getQueryParameter("code") ?: dataUri?.lastPathSegment?.takeIf { it != "join" }
        
        setContent {
            WorldCup2026Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(initialMatchId = navMatchId, initialJoinCode = joinCodeFromUri)
                }
            }
        }
    }
}
