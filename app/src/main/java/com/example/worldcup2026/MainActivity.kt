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
        
        setContent {
            WorldCup2026Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}
