package com.example.worldcup2026.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen() {
    val libraries = listOf(
        LibraryInfo("Jetpack Compose", "Google", "Apache 2.0"),
        LibraryInfo("Room Database", "Google", "Apache 2.0"),
        LibraryInfo("Retrofit & Gson", "Square", "Apache 2.0"),
        LibraryInfo("Coil Image Loader", "Coil-kt", "Apache 2.0"),
        LibraryInfo("Material Design 3", "Google", "Apache 2.0")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Cabecera Premium
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = com.example.worldcup2026.R.drawable.app_icon_main),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("WORLD CUP 2026", fontWeight = FontWeight.Black, fontSize = 26.sp, letterSpacing = 2.sp)
                    Text("Centro de Resultados de Alto Rendimiento", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "VERSIÓN 1.2.0 (BUILD 2026)", 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Sección de Autores
        item {
            AboutSectionHeader("EQUIPO DE DESARROLLO", Icons.Default.Person)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AuthorItem("Director de Proyecto", "Rubén")
                    Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    AuthorItem("Arquitectura e IA", "Antigravity")
                }
            }
        }

        // Recursos y Datos
        item {
            AboutSectionHeader("RECURSOS Y DATOS", Icons.Default.Build)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ResourceItem("Banderas Oficiales", "FlagCDN Public API")
                    ResourceItem("Tipografía", "Google Fonts (Inter/Outfit)")
                    ResourceItem("Iconografía", "Material Symbols")
                }
            }
        }

        // Soporte y Contacto
        item {
            AboutSectionHeader("SOPORTE Y CONTACTO", Icons.Default.Email)
            val context = androidx.compose.ui.platform.LocalContext.current
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextButton(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:rubeng69@hotmail.com")
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Soporte World Cup 2026")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contactar Soporte (rubeng69@hotmail.com)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Sección Legal
        item {
            AboutSectionHeader("LEGAL", Icons.Default.Lock)
            val context = androidx.compose.ui.platform.LocalContext.current
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    LegalButton(
                        label = "Política de Privacidad",
                        url = "https://ellocodelpedal.duckdns.org/privacidad-wc.html",
                        context = context
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    LegalButton(
                        label = "Términos y Condiciones",
                        url = "https://ellocodelpedal.duckdns.org/terminos-wc.html",
                        context = context
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    LegalButton(
                        label = "Visitar Developer Studio",
                        url = "https://ellocodelpedal.duckdns.org/index.html",
                        context = context
                    )
                }
            }
        }

        // Licencias de Software
        item {
            AboutSectionHeader("LICENCIAS DE SOFTWARE", Icons.Default.Info)
        }

        items(libraries) { lib ->
            LibraryItem(lib)
        }

        // Pie de página Legal
        item {
            Text(
                text = "© 2026 Rubén & Antigravity. Todos los derechos reservados.\n\nEsta aplicación es una herramienta independiente de seguimiento de resultados y no tiene vínculo oficial con la FIFA ni ninguna federación nacional. Los nombres y marcas se utilizan bajo el principio de uso informativo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun AboutSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun AuthorItem(role: String, name: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ResourceItem(label: String, source: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun LibraryItem(lib: LibraryInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(lib.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(lib.author, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Surface(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                Text(lib.license, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun LegalButton(label: String, url: String, context: android.content.Context) {
    TextButton(
        onClick = {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

data class LibraryInfo(val name: String, val author: String, val license: String)
