package com.example.worldcup2026.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

private fun installApkFile(context: Context, destinationFile: File) {
    if (!destinationFile.exists()) return
    val apkUri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        destinationFile
    )
    val installIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(installIntent)
}

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
    var showWebUpdater by remember { mutableStateOf(false) }

    if (showWebUpdater) {
        InAppUpdateWebDialog(
            updateInfo = updateInfo,
            onClose = {
                showWebUpdater = false
                if (!updateInfo.isMandatory) {
                    onDismiss()
                }
            }
        )
        return
    }

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
            border = BorderStroke(1.5.dp, Color(0xFFFFD700))
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
                    onClick = { showWebUpdater = true },
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

@Composable
private fun InAppUpdateWebDialog(
    updateInfo: WorldCupViewModel.AppUpdateInfo,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isDownloading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var downloadedBytes by remember { mutableLongStateOf(0L) }
    var totalBytes by remember { mutableLongStateOf(0L) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }

    fun startApkDownload(rawUrl: String) {
        if (isDownloading) return
        val targetUrl = if (rawUrl.endsWith(".apk", ignoreCase = true) || rawUrl.contains("/download/ArenaProde", ignoreCase = true)) {
            if (rawUrl.endsWith("/download/ArenaProde", ignoreCase = true)) {
                "https://ellocodelpedal.duckdns.org/download/ArenaProde.apk"
            } else {
                rawUrl
            }
        } else {
            updateInfo.downloadUrl
        }
        val cleanUrl = if (targetUrl.contains("?")) "$targetUrl&t=${System.currentTimeMillis()}" else "$targetUrl?t=${System.currentTimeMillis()}"
        val destFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "ArenaProde_update.apk")

        isDownloading = true
        progress = 0f
        downloadedBytes = 0L
        totalBytes = 0L

        downloadJob = scope.launch(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                if (destFile.exists()) destFile.delete()
                val url = URL(cleanUrl)
                conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 20000
                    instanceFollowRedirects = true
                }
                val total = conn.contentLengthLong
                withContext(Dispatchers.Main) { totalBytes = if (total > 0) total else 0L }

                conn.inputStream.use { input ->
                    FileOutputStream(destFile).use { output ->
                        val buffer = ByteArray(16 * 1024)
                        var read: Int
                        var copied = 0L
                        var lastUiUpdate = 0L
                        while (input.read(buffer).also { read = it } != -1) {
                            if (!isActive) {
                                destFile.delete()
                                return@launch
                            }
                            output.write(buffer, 0, read)
                            copied += read
                            val now = System.currentTimeMillis()
                            if (now - lastUiUpdate >= 100L) {
                                lastUiUpdate = now
                                val p = if (total > 0) (copied.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
                                withContext(Dispatchers.Main) {
                                    downloadedBytes = copied
                                    progress = p
                                }
                            }
                        }
                        output.flush()
                    }
                }

                withContext(Dispatchers.Main) {
                    isDownloading = false
                    installApkFile(context, destFile)
                    onClose()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDownloading = false
                    if (isActive) {
                        Toast.makeText(context, "Error al descargar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } finally {
                conn?.disconnect()
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isDownloading && !updateInfo.isMandatory) onClose()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isDownloading && !updateInfo.isMandatory,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF090D16)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111827))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Arena Prode · Descarga Oficial",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Tocá Descargar APK para iniciar la actualización v${updateInfo.versionName}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                        if (!updateInfo.isMandatory || !isDownloading) {
                            IconButton(
                                onClick = {
                                    downloadJob?.cancel()
                                    isDownloading = false
                                    onClose()
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                            }
                        }
                    }

                    AndroidView(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                setDownloadListener { url, _, _, _, _ ->
                                    if (!url.isNullOrBlank()) {
                                        startApkDownload(url)
                                    }
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val clickedUrl = request?.url?.toString() ?: return false
                                        val isApkTarget = clickedUrl.endsWith(".apk", ignoreCase = true) ||
                                            (clickedUrl.contains("/download/ArenaProde", ignoreCase = true) &&
                                                !clickedUrl.endsWith(".msi", ignoreCase = true) &&
                                                !clickedUrl.endsWith(".msix", ignoreCase = true))
                                        if (isApkTarget) {
                                            startApkDownload(clickedUrl)
                                            return true
                                        }
                                        return false
                                    }
                                }
                                loadUrl("https://ellocodelpedal.duckdns.org/arena.html")
                            }
                        },
                        onRelease = { webView ->
                            webView.stopLoading()
                            webView.destroy()
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D121D))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AdmobBanner()
                    }
                }

                if (isDownloading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.68f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2230)),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Descargando archivo:\nArenaProde_v${updateInfo.versionName}.apk",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                if (totalBytes > 0L) {
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = Color(0xFF00E676),
                                        trackColor = Color.White.copy(alpha = 0.15f)
                                    )
                                    val dlMb = String.format(java.util.Locale.US, "%.1f", downloadedBytes / (1024f * 1024f))
                                    val totMb = String.format(java.util.Locale.US, "%.1f", totalBytes / (1024f * 1024f))
                                    val pct = (progress * 100).toInt()
                                    Text(
                                        text = "$dlMb MB / $totMb MB ($pct%)",
                                        color = Color(0xFF00E676),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = Color(0xFF00E676),
                                        trackColor = Color.White.copy(alpha = 0.15f)
                                    )
                                }

                                Text(
                                    text = "Por favor, espera...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            downloadJob?.cancel()
                                            isDownloading = false
                                        }
                                    ) {
                                        Text(
                                            text = "CANCELAR",
                                            color = Color(0xFFFF5252),
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
