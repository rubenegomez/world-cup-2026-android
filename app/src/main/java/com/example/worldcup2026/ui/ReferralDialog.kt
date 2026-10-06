package com.example.worldcup2026.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ReferralDialog(
    userId: String,
    userName: String,
    viewModel: ProdeViewModel? = null,
    worldCupViewModel: WorldCupViewModel? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputCode by remember { mutableStateOf("") }
    var isSubmittingCode by remember { mutableStateOf(false) }

    val referralCode = userId.ifEmpty { "prode" }
    val referralUrl = "https://ellocodelpedal.duckdns.org/download/ArenaProde.apk?ref=$referralCode"
    val invitationText = "⚽ ¡Hola! Te invito a jugar a Arena Prode y Torneos conmigo.\nDescargá la app directamente desde mi enlace para recibir +6 Horas Sin Anuncios (¡y yo recibiré +12 Horas!):\n👉 $referralUrl"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141924))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎁", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REFERIR Y BONOS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explicación
                Surface(
                    color = Color.White.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "⚡ BENEFICIOS SIN PUBLICIDAD:",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color(0xFFFFC107)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• 🎁 +12 Horas para vos por cada amigo que descargue e inicie sesión con tu enlace.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "• 🤝 +6 Horas para tu amigo al ingresar mediante tu invitación.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "• 🌟 +24 Horas de Bono de Bienvenida único al iniciar sesión por primera vez.",
                            fontSize = 11.sp,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enlace de referido
                Text("Tu enlace de invitación:", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = referralUrl,
                            fontSize = 11.sp,
                            color = Color(0xFF64B5F6),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(referralUrl))
                                Toast.makeText(context, "¡Enlace copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Compartir invitación directamente:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // WhatsApp
                    ReferralChannelButton("WhatsApp", Color(0xFF25D366)) {
                        shareInvitation(context, invitationText, "com.whatsapp")
                    }

                    // Email / SMS
                    ReferralChannelButton("Email / SMS", Color(0xFFEA4335)) {
                        shareInvitation(context, invitationText, null)
                    }

                    // General
                    ReferralChannelButton("Más Apps", Color(0xFF2196F3)) {
                        shareInvitation(context, invitationText, null)
                    }
                }

                if (viewModel != null) {
                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("¿Un amigo te compartió un código?", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { inputCode = it },
                            placeholder = { Text("Código de referido", fontSize = 11.sp, color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                        Button(
                            onClick = {
                                if (inputCode.isNotBlank()) {
                                    isSubmittingCode = true
                                    viewModel.applyReferral(inputCode.trim()) { success, msg, bonusHours ->
                                        isSubmittingCode = false
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        if (success) {
                                            if (bonusHours > 0) {
                                                worldCupViewModel?.addAdFreeTime(bonusHours * 3600 * 1000L)
                                            }
                                            inputCode = ""
                                            onDismiss()
                                        }
                                    }
                                }
                            },
                            enabled = !isSubmittingCode && inputCode.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Canjear (+6h)", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferralChannelButton(
    name: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (name.contains("WhatsApp")) Icons.Default.Share else if (name.contains("Email")) Icons.Default.Email else Icons.Default.GroupAdd,
                contentDescription = name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, fontSize = 11.sp, color = Color.Gray)
    }
}

private fun shareInvitation(context: Context, text: String, packageName: String?) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        if (!packageName.isNullOrEmpty()) {
            setPackage(packageName)
        }
    }
    try {
        if (!packageName.isNullOrEmpty()) {
            context.startActivity(intent)
        } else {
            context.startActivity(Intent.createChooser(intent, "Invitar Amigo a Arena Prode"))
        }
    } catch (e: Exception) {
        context.startActivity(Intent.createChooser(intent, "Invitar Amigo a Arena Prode"))
    }
}
