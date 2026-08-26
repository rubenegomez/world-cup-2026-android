package com.example.worldcup2026.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

fun openDownloadUrlInChromeOrFallback(context: Context, urlStr: String) {
    val uri = Uri.parse(urlStr)
    
    // 1. Forzar apertura en Google Chrome obligatoriamente si está instalado
    val chromeIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.android.chrome")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    
    try {
        context.startActivity(chromeIntent)
        Toast.makeText(context, "🌐 Abriendo descarga en Google Chrome...", Toast.LENGTH_SHORT).show()
        return
    } catch (e: Exception) {
        // Chrome no está instalado o deshabilitado
    }

    // 2. Si Chrome no está, intentar usar el Gestor de Descargas nativo (DownloadManager)
    try {
        val request = DownloadManager.Request(uri).apply {
            setTitle("Arena Prode APK")
            setDescription("Descargando actualización...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ArenaProde.apk")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "📥 Descargando en la barra de notificaciones...", Toast.LENGTH_LONG).show()
        return
    } catch (e: Exception) {
        // Error en DownloadManager
    }

    // 3. Fallback genérico a cualquier navegador web predeterminado
    try {
        val genericIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(genericIntent)
    } catch (ex: Exception) {
        Toast.makeText(context, "Error al abrir el navegador: ${ex.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun UpdateAvailableDialog(
    updateInfo: WorldCupViewModel.AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = {
            if (!updateInfo.isMandatory) onDismiss()
        },
        properties = DialogProperties(dismissOnBackPress = !updateInfo.isMandatory, dismissOnClickOutside = !updateInfo.isMandatory)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2230)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚀",
                    fontSize = 44.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "¡NUEVA VERSIÓN DISPONIBLE!",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Versión ${updateInfo.versionName} (Build ${updateInfo.versionCode})",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!updateInfo.releaseNotes.isNullOrBlank()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Novedades:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = updateInfo.releaseNotes,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Button(
                    onClick = {
                        openDownloadUrlInChromeOrFallback(context, updateInfo.downloadUrl)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text(
                        text = "⚡ ACTUALIZAR AHORA (CHROME)",
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }

                if (!updateInfo.isMandatory) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Recordarme más tarde",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
