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

fun downloadAndInstallApk(context: Context, urlStr: String) {
    val cleanUrl = if (urlStr.contains("?")) "$urlStr&t=${System.currentTimeMillis()}" else "$urlStr?t=${System.currentTimeMillis()}"
    val uri = Uri.parse(cleanUrl)

    try {
        val fileName = "ArenaProde_update.apk"
        val destinationFile = java.io.File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        val request = DownloadManager.Request(uri).apply {
            setTitle("Arena Prode - Actualización")
            setDescription("Descargando nueva versión...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationUri(Uri.fromFile(destinationFile))
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        Toast.makeText(context, "📥 Descargando actualización...", Toast.LENGTH_SHORT).show()

        // Receptor para instalar inmediatamente al completarse
        val onComplete = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    try {
                        ctxt.unregisterReceiver(this)
                    } catch (e: Exception) {}

                    try {
                        if (destinationFile.exists()) {
                            val apkUri = androidx.core.content.FileProvider.getUriForFile(
                                ctxt,
                                "${ctxt.packageName}.fileprovider",
                                destinationFile
                            )
                            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(apkUri, "application/vnd.android.package-archive")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            ctxt.startActivity(installIntent)
                        }
                    } catch (ex: Exception) {
                        Toast.makeText(ctxt, "Error al abrir instalador: ${ex.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        val filter = android.content.IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(onComplete, filter)
        }

    } catch (e: Exception) {
        // Fallback al navegador si DownloadManager falla
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, "Error: ${ex.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

fun openDownloadUrlInChromeOrFallback(context: Context, urlStr: String) {
    downloadAndInstallApk(context, urlStr)
}

@Composable
fun ChangelogDialog(
    versionName: String,
    versionCode: Int,
    releaseNotes: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
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
                Text(text = "🎉", fontSize = 44.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "¡NOVEDADES DE LA VERSIÓN!",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Versión $versionName (Build $versionCode)",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Qué hay de nuevo:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = releaseNotes.ifBlank { "Mejoras de rendimiento, estabilidad y actualización de torneos." },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text(
                        text = "¡GENIAL, A JUGAR!",
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
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
                        text = "⚡ ACTUALIZAR AHORA",
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
