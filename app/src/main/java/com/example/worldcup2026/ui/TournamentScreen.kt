package com.example.worldcup2026.ui

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class TournamentItem(
    val id: Int, 
    val name: String, 
    val type: String, 
    val active: Boolean = false
)

val internacionales = listOf(
    TournamentItem(1, "Campeonato Mundial", "Internacional", active = true),
    TournamentItem(2, "Torneos Regionales", "Internacional", active = false),
    TournamentItem(3, "Copa Libertadores", "Internacional", active = true),
    TournamentItem(4, "CONMEBOL Sudamericana", "Internacional", active = false)
)

val nacionales = listOf(
    TournamentItem(5, "Liga Profesional", "Nacional", active = true),
    TournamentItem(6, "Copa Argentina", "Nacional", active = true),
    TournamentItem(7, "Supercopa Argentina", "Nacional", active = false),
    TournamentItem(8, "Primera Nacional", "Nacional", active = true)
)

@Composable
fun TournamentScreen(viewModel: WorldCupViewModel, onTournamentSelected: (Int, String) -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("world_cup_prefs", Context.MODE_PRIVATE) }
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Internacionales", "Nacionales")
    
    // Estado de favoritos locales
    var favoriteTournaments by remember {
        mutableStateOf(
            sharedPrefs.getStringSet("favorite_tournaments", emptySet()) ?: emptySet()
        )
    }

    // Estado de Selección / Club favoritos
    var favCountry by remember { mutableStateOf(sharedPrefs.getString("favorite_country", "Ninguna") ?: "Ninguna") }
    var favClub by remember { mutableStateOf(sharedPrefs.getString("favorite_club", "Ninguno") ?: "Ninguno") }

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Pulsación infinita para el indicador "EN VIVO"
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val livePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )

    val liveStatuses by viewModel.liveTournaments
    
    // Detectar si hay vivos generales en cada pestaña
    val hasInternacionalesLive = remember(liveStatuses) {
        liveStatuses[1] == true || liveStatuses[3] == true
    }
    val hasNacionalesLive = remember(liveStatuses) {
        liveStatuses[5] == true
    }

    // Función para alternar favorito
    val toggleFavorite: (Int) -> Unit = { id ->
        val currentSet = favoriteTournaments.toMutableSet()
        val idStr = id.toString()
        if (currentSet.contains(idStr)) {
            currentSet.remove(idStr)
        } else {
            currentSet.add(idStr)
        }
        sharedPrefs.edit().putStringSet("favorite_tournaments", currentSet).apply()
        favoriteTournaments = currentSet
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp)
    ) {
        // Encabezado principal: Título y Perfil
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(40.dp))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ARENA PRODE",
                    color = Color(0xFFFFF3CD),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Y TORNEOS",
                    color = Color(0xFFD4AF37),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
            
            // Icono de perfil que abre Ajustes (Muestra foto real de Google/Firebase si existe)
            val currentUser = remember { FirebaseAuth.getInstance().currentUser }
            val photoUrl = currentUser?.photoUrl?.toString()

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5B842))
                    .border(1.5.dp, Color(0xFFFFF0B3), CircleShape)
                    .clickable { showSettingsDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (!photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = Color(0xFF4E360F),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Pestañas Estilizadas Doradas
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFFE5B842),
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val hasLive = if (index == 0) hasInternacionalesLive else hasNacionalesLive
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title.uppercase(), 
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                            if (hasLive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50).copy(alpha = livePulseAlpha))
                                )
                            }
                        }
                    },
                    selectedContentColor = Color(0xFFE5B842),
                    unselectedContentColor = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // Obtener lista y ordenar prioritariamente por favorito
        val baseList = if (selectedTabIndex == 0) internacionales else nacionales
        val tournamentsToList = remember(baseList, favoriteTournaments) {
            baseList.sortedWith(
                compareByDescending<TournamentItem> { favoriteTournaments.contains(it.id.toString()) }
                .thenBy { it.id }
            )
        }

        // Contenedor principal de la lista de botones
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tournamentsToList, key = { it.id }) { tournament ->
                    val icon = when (tournament.id) {
                        1 -> Icons.Default.EmojiEvents
                        2 -> Icons.Default.Public
                        3 -> Icons.Default.EmojiEvents
                        4 -> Icons.Default.Public
                        5 -> Icons.Default.Leaderboard
                        6 -> Icons.Default.EmojiEvents
                        7 -> Icons.Default.EmojiEvents
                        else -> Icons.Default.Book
                    }
                    val isLive = liveStatuses[tournament.id] == true
                    val isFav = favoriteTournaments.contains(tournament.id.toString())

                    GoldButton(
                        text = tournament.name,
                        icon = icon,
                        active = tournament.active,
                        isLive = isLive,
                        isFavorite = isFav,
                        liveAlpha = livePulseAlpha,
                        onFavToggle = { toggleFavorite(tournament.id) },
                        onClick = { onTournamentSelected(tournament.id, tournament.name) }
                    )
                }
            }
        }
        
        // Ajustes en la parte inferior izquierda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .border(1.2.dp, Color(0xFFE5B842), CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = Color(0xFFE5B842),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // Modal de Configuración Favoritos
    if (showSettingsDialog) {
        FavoritesSettingsDialog(
            favCountry = favCountry,
            favClub = favClub,
            onSave = { country, club ->
                sharedPrefs.edit()
                    .putString("favorite_country", country)
                    .putString("favorite_club", club)
                    .apply()
                favCountry = country
                favClub = club
                showSettingsDialog = false
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun GoldButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    isLive: Boolean,
    isFavorite: Boolean,
    liveAlpha: Float,
    onFavToggle: () -> Unit,
    onClick: () -> Unit
) {
    val alpha = if (active) 1f else 0.45f
    
    val goldBg = Brush.verticalGradient(
        colors = if (active) {
            listOf(
                Color(0xFFFFF2C2),
                Color(0xFFE5B842),
                Color(0xFF9E7815)
            )
        } else {
            listOf(
                Color(0xFFBDB29A),
                Color(0xFF8D7F67),
                Color(0xFF5A503F)
            )
        }
    )
    
    val goldBorder = Brush.verticalGradient(
        colors = if (active) {
            listOf(Color(0xFFFFFDF5), Color(0xFF7A5807))
        } else {
            listOf(Color(0xFFD7CCC8), Color(0xFF3E2723))
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(
                elevation = if (active) 8.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                clip = false
            )
            .background(goldBg, shape = RoundedCornerShape(18.dp))
            .border(
                width = 2.dp,
                brush = goldBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .then(if (active) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) Color(0xFF4E360F) else Color(0xFF2C2216).copy(alpha = alpha),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = text.uppercase(),
                        color = if (active) Color(0xFF4E360F) else Color(0xFF2C2216).copy(alpha = alpha),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    if (active && isLive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50).copy(alpha = liveAlpha))
                        )
                    }
                }
                if (!active) {
                    Text(
                        text = "PRÓXIMAMENTE",
                        color = Color(0xFF2C2216).copy(alpha = 0.45f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Estrella para marcar favorito
            if (active) {
                IconButton(onClick = onFavToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFF6A4E00) else Color(0xFF4E360F).copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// Dialog de favoritos para Ajustes
@Composable
fun FavoritesSettingsDialog(
    favCountry: String,
    favClub: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCountry by remember { mutableStateOf(favCountry) }
    var selectedClub by remember { mutableStateOf(favClub) }

    // Listas básicas para seleccionar (Secciones)
    val countries = listOf("Ninguna", "Argentina", "Brasil", "Alemania", "Francia", "España", "Uruguay", "Colombia", "México", "Italia", "Inglaterra")
    val clubs = listOf("Ninguno", "Boca Juniors", "River Plate", "Racing Club", "Independiente", "San Lorenzo", "Estudiantes de La Plata", "Flamengo", "Palmeiras", "Corinthians", "Peñarol", "Nacional")

    var showCountryDropdown by remember { mutableStateOf(false) }
    var showClubDropdown by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF202020), RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFFE5B842), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AJUSTES DE FAVORITOS",
                        color = Color(0xFFFFF3CD),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Selección
                Text(
                    text = "SELECCIÓN FAVORITA (MUNDIAL)",
                    color = Color(0xFFD4AF37),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { showCountryDropdown = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = selectedCountry, color = Color.White, fontWeight = FontWeight.Bold)
                    DropdownMenu(
                        expanded = showCountryDropdown,
                        onDismissRequest = { showCountryDropdown = false },
                        modifier = Modifier.background(Color(0xFF2E2E2E))
                    ) {
                        countries.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name, color = Color.White) },
                                onClick = {
                                    selectedCountry = name
                                    showCountryDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Selector de Club
                Text(
                    text = "CLUB FAVORITO (LIGA/LIBERTADORES)",
                    color = Color(0xFFD4AF37),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { showClubDropdown = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = selectedClub, color = Color.White, fontWeight = FontWeight.Bold)
                    DropdownMenu(
                        expanded = showClubDropdown,
                        onDismissRequest = { showClubDropdown = false },
                        modifier = Modifier.background(Color(0xFF2E2E2E))
                    ) {
                        clubs.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name, color = Color.White) },
                                onClick = {
                                    selectedClub = name
                                    showClubDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Botón Guardar
                Button(
                    onClick = { onSave(selectedCountry, selectedClub) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5B842)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "GUARDAR FAVORITOS",
                        color = Color(0xFF4E360F),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
