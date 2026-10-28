package com.example.worldcup2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.worldcup2026.data.model.MasterTeamCatalog

@Composable
fun AdminEditTournamentDialog(
    tournamentId: Int,
    initialName: String,
    initialLogoUrl: String?,
    onDismiss: () -> Unit,
    onSuccess: (newName: String, newLogoUrl: String?) -> Unit
) {
    var tournamentName by remember { mutableStateOf(initialName) }
    var logoUrl by remember { mutableStateOf(initialLogoUrl ?: "") }
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (originalBitmap != null) {
                    val maxDim = 256
                    val ratio = Math.min(maxDim.toFloat() / originalBitmap.width, maxDim.toFloat() / originalBitmap.height)
                    val targetW = if (ratio < 1.0f) (originalBitmap.width * ratio).toInt() else originalBitmap.width
                    val targetH = if (ratio < 1.0f) (originalBitmap.height * ratio).toInt() else originalBitmap.height
                    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)

                    val outputStream = java.io.ByteArrayOutputStream()
                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 95, outputStream)
                    val bytes = outputStream.toByteArray()
                    selectedImageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    android.widget.Toast.makeText(context, "Símbolo cargado listo para guardar", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error procesando imagen: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("🏆 Editar Símbolo y Nombre del Torneo", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Previsualización de logo / símbolo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Símbolo seleccionado",
                            modifier = Modifier.size(56.dp)
                        )
                    } else if (logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = "Símbolo",
                            modifier = Modifier.size(56.dp)
                        )
                    } else {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700).copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
                    }
                }

                // Botón para elegir archivo de imagen
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (selectedImageUri != null) "CAMBIAR SÍMBOLO SELECCIONADO" else "SELECCIONAR SÍMBOLO DEL TELÉFONO", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = tournamentName,
                    onValueChange = { tournamentName = it },
                    label = { Text("Nombre / Título del Torneo", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("URL del Símbolo Oficial (opcional)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isUploading = true
                    coroutineScope.launch {
                        try {
                            val finalName = tournamentName.trim()
                            val finalLogo = when {
                                selectedImageUri != null -> selectedImageUri.toString()
                                logoUrl.isNotBlank() -> logoUrl.trim()
                                else -> initialLogoUrl
                            }
                            
                            // Guardado local persistente inmediato
                            MasterTeamCatalog.saveCustomTournament(context, tournamentId, finalName, finalLogo)

                            // Sincronización con el backend si hay email de administrador
                            val email = com.example.worldcup2026.data.api.AuthManager.getCurrentUserEmail() ?: ""
                            if (email.isNotBlank()) {
                                val req = com.example.worldcup2026.data.api.AdminTournamentEditRequest(
                                    tournament_id = tournamentId,
                                    name = finalName,
                                    logo_url = if (logoUrl.isNotBlank()) logoUrl.trim() else null,
                                    image_base64 = selectedImageBase64,
                                    file_extension = ".png"
                                )
                                try {
                                    com.example.worldcup2026.data.api.NetworkModule.apiService.editTournamentAdmin(email, req)
                                } catch (e: Exception) {
                                    // Continúa con guardado local aunque el backend no responda
                                }
                            }

                            android.widget.Toast.makeText(context, "Símbolo y nombre del torneo actualizados", android.widget.Toast.LENGTH_SHORT).show()
                            onSuccess(finalName, finalLogo)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Error: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
                },
                enabled = !isUploading && tournamentName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isUploading) "GUARDANDO..." else "GUARDAR SÍMBOLO/NOMBRE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
