package com.example.worldcup2026.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.worldcup2026.data.api.ApiTournament
import com.example.worldcup2026.data.model.MasterTeamCatalog

data class TournamentOption(
    val id: Int,
    val name: String,
    val category: String,
    val logoUrl: String? = null,
    val hasLive: Boolean = false
)

private data class CategoryHeader(
    val key: String,
    val title: String,
    val emoji: String
)

private val CATEGORIES_ORDER = listOf(
    CategoryHeader("FAVORITOS", "FAVORITOS", "⭐"),
    CategoryHeader("AFA", "AFA", "🇦🇷"),
    CategoryHeader("CONMEBOL", "CONMEBOL", "🌎"),
    CategoryHeader("FIFA", "FIFA", "🌍"),
    CategoryHeader("OTROS TORNEOS", "OTROS TORNEOS", "🌐")
)

@Composable
fun TournamentHeaderButton(
    selectedTournamentId: Int?,
    filterLiveOnly: Boolean = false,
    onOpenSelector: () -> Unit,
    onEditTournament: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val displayName = remember(selectedTournamentId, filterLiveOnly) {
        when {
            filterLiveOnly -> "🔴 Partidos En Vivo"
            selectedTournamentId == null || selectedTournamentId == 0 -> "🌐 Todos los Torneos"
            else -> MasterTeamCatalog.getTournamentDisplayName(context, selectedTournamentId)
        }
    }

    val displayLogo = remember(selectedTournamentId, filterLiveOnly) {
        if (!filterLiveOnly && selectedTournamentId != null && selectedTournamentId > 0) {
            MasterTeamCatalog.getTournamentLogo(context, selectedTournamentId)
        } else null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                com.example.worldcup2026.data.util.SoundManager.playTic()
                onOpenSelector()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (filterLiveOnly) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                } else if (!displayLogo.isNullOrBlank()) {
                    AsyncImage(
                        model = displayLogo,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                } else if (selectedTournamentId != null && selectedTournamentId > 0) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                } else {
                    Text("🌐", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onEditTournament != null && selectedTournamentId != null && selectedTournamentId > 0 && !filterLiveOnly) {
                    IconButton(
                        onClick = { onEditTournament(selectedTournamentId) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar Símbolo",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Cambiar Torneo",
                    tint = Color(0xFFFFD700)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TournamentSelectorModal(
    selectedTournamentId: Int?,
    filterLiveOnly: Boolean = false,
    includeAllOption: Boolean = false,
    includeLiveOption: Boolean = false,
    apiTournaments: List<ApiTournament> = emptyList(),
    liveTournamentsMap: Map<Int, Boolean> = emptyMap(),
    favoriteTournamentIds: Set<Int> = emptySet(),
    onToggleFavorite: ((Int) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSelectTournament: (Int) -> Unit,
    onSelectTodos: (() -> Unit)? = null,
    onSelectLiveOnly: (() -> Unit)? = null,
    onTournamentUpdated: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var editingTournamentId by remember { mutableStateOf<Int?>(null) }
    var versionTrigger by remember { mutableIntStateOf(0) }

    // Estados colapsables por categoría (todas desplegadas por defecto)
    val expandedCategories = remember {
        mutableStateMapOf<String, Boolean>().apply {
            CATEGORIES_ORDER.forEach { cat -> put(cat.key, true) }
        }
    }

    if (editingTournamentId != null) {
        val tId = editingTournamentId!!
        val initialName = MasterTeamCatalog.getTournamentDisplayName(context, tId)
        val initialLogo = MasterTeamCatalog.getTournamentLogo(context, tId)
        AdminEditTournamentDialog(
            tournamentId = tId,
            initialName = initialName,
            initialLogoUrl = initialLogo,
            onDismiss = { editingTournamentId = null },
            onSuccess = { _, _ ->
                editingTournamentId = null
                versionTrigger++
                onTournamentUpdated?.invoke()
            }
        )
    }

    val allTournamentOptions = remember(apiTournaments, liveTournamentsMap, versionTrigger) {
        MasterTeamCatalog.MASTER_TOURNAMENTS.map { masterT ->
            val customName = MasterTeamCatalog.getTournamentDisplayName(context, masterT.id)
            val customLogo = MasterTeamCatalog.getTournamentLogo(context, masterT.id) ?: masterT.logoUrl
            val hasLive = liveTournamentsMap[masterT.id] == true
            TournamentOption(
                id = masterT.id,
                name = if (customName.isNotBlank()) customName else masterT.displayName,
                category = masterT.category,
                logoUrl = customLogo,
                hasLive = hasLive
            )
        }
    }

    val groupedTournaments = remember(allTournamentOptions, favoriteTournamentIds) {
        val base = allTournamentOptions.groupBy { it.category }.toMutableMap()
        base["FAVORITOS"] = allTournamentOptions.filter { it.id in favoriteTournamentIds }
        base
    }

    val anyLive = remember(liveTournamentsMap) {
        liveTournamentsMap.values.any { it }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Cabecera del diálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏆", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seleccionar Torneo",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. SECCIÓN SUPERIOR: "Todos los Torneos" y "En Vivo"
                    if (includeAllOption || includeLiveOption) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (includeAllOption) {
                                    val isTodosSelected = !filterLiveOnly && (selectedTournamentId == null || selectedTournamentId == 0)
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clickable {
                                                com.example.worldcup2026.data.util.SoundManager.playTic()
                                                onSelectTodos?.invoke()
                                                onDismiss()
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isTodosSelected) Color(0xFFFFD700).copy(alpha = 0.25f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        ),
                                        border = BorderStroke(
                                            width = if (isTodosSelected) 1.5.dp else 0.8.dp,
                                            color = if (isTodosSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("🌐", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Todos los Torneos",
                                                fontSize = 13.sp,
                                                fontWeight = if (isTodosSelected) FontWeight.Black else FontWeight.Bold,
                                                color = if (isTodosSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                if (includeLiveOption) {
                                    Card(
                                        modifier = Modifier
                                            .weight(if (includeAllOption) 0.8f else 1f)
                                            .height(52.dp)
                                            .clickable {
                                                com.example.worldcup2026.data.util.SoundManager.playTic()
                                                onSelectLiveOnly?.invoke()
                                                onDismiss()
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (filterLiveOnly) Color(0xFFE53935).copy(alpha = 0.25f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        ),
                                        border = BorderStroke(
                                            width = if (filterLiveOnly) 1.5.dp else 0.8.dp,
                                            color = if (filterLiveOnly) Color(0xFFE53935) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(if (anyLive) Color(0xFFE53935) else Color.Gray)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "En Vivo",
                                                fontSize = 13.sp,
                                                fontWeight = if (filterLiveOnly) FontWeight.Black else FontWeight.Bold,
                                                color = if (filterLiveOnly) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. CATEGORÍAS COLAPSABLES/DESPLEGABLES
                    CATEGORIES_ORDER.forEach { categoryDef ->
                        val tourneys = groupedTournaments[categoryDef.key] ?: emptyList()
                        val isFavoritosCategory = categoryDef.key == "FAVORITOS"
                        if (tourneys.isNotEmpty() || isFavoritosCategory) {
                            val isExpanded = expandedCategories[categoryDef.key] ?: true

                            // Encabezado de Categoría (Acordeón)
                            item(key = "header_" + categoryDef.key) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            com.example.worldcup2026.data.util.SoundManager.playTic()
                                            expandedCategories[categoryDef.key] = !isExpanded
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isFavoritosCategory) Color(0xFFFFD700).copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isFavoritosCategory) Color(0xFFFFD700).copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(categoryDef.emoji, fontSize = 17.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = categoryDef.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isFavoritosCategory) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${tourneys.size}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFFFD700)
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) "Colapsar" else "Desplegar",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            if (isExpanded) {
                                if (isFavoritosCategory && tourneys.isEmpty()) {
                                    item(key = "empty_favoritos") {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("⭐", fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "Tocá la estrella en cualquier torneo para agregarlo a tus Favoritos y tenerlo siempre arriba.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.75f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    val chunks = tourneys.chunked(3)
                                    chunks.forEachIndexed { chunkIndex, rowItems ->
                                        item(key = "row_" + categoryDef.key + "_" + chunkIndex) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                for (item in rowItems) {
                                                    val isSelected = !filterLiveOnly && selectedTournamentId == item.id
                                                    val isFav = item.id in favoriteTournamentIds
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .aspectRatio(1f)
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .background(
                                                                if (isSelected) Color(0xFFFFD700).copy(alpha = 0.22f)
                                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                                            )
                                                            .border(
                                                                width = if (isSelected) 1.6.dp else 0.5.dp,
                                                                color = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                                                shape = RoundedCornerShape(14.dp)
                                                            )
                                                            .combinedClickable(
                                                                onClick = {
                                                                    com.example.worldcup2026.data.util.SoundManager.playTic()
                                                                    onSelectTournament(item.id)
                                                                    onDismiss()
                                                                },
                                                                onLongClick = {
                                                                    editingTournamentId = item.id
                                                                }
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        // Botón de Favorito (Estrella)
                                                        IconButton(
                                                            onClick = {
                                                                com.example.worldcup2026.data.util.SoundManager.playTic()
                                                                onToggleFavorite?.invoke(item.id)
                                                            },
                                                            modifier = Modifier
                                                                .size(26.dp)
                                                                .align(Alignment.TopEnd)
                                                                .padding(1.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                                                contentDescription = if (isFav) "Quitar de favoritos" else "Agregar a favoritos",
                                                                tint = if (isFav) Color(0xFFFFD700) else Color.White.copy(alpha = 0.35f),
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                        }

                                                        // Punto rojo si tiene partidos en vivo
                                                        if (item.hasLive) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(9.dp)
                                                                    .align(Alignment.TopStart)
                                                                    .padding(start = 6.dp, top = 6.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFFE53935))
                                                                    .border(1.dp, Color.White, CircleShape)
                                                            )
                                                        }

                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center,
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(6.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier.size(36.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                if (!item.logoUrl.isNullOrBlank()) {
                                                                    AsyncImage(
                                                                        model = item.logoUrl,
                                                                        contentDescription = item.name,
                                                                        modifier = Modifier.size(32.dp)
                                                                    )
                                                                } else {
                                                                    Icon(
                                                                        Icons.Default.EmojiEvents,
                                                                        contentDescription = null,
                                                                        tint = Color(0xFFFFD700),
                                                                        modifier = Modifier.size(28.dp)
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.height(4.dp))

                                                            val cleanName = remember(item.name) {
                                                                item.name
                                                                    .replace("Copa Conmebol ", "")
                                                                    .replace("Torneo ", "")
                                                                    .trim()
                                                            }

                                                            Text(
                                                                text = cleanName,
                                                                fontSize = 10.sp,
                                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                                                color = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface,
                                                                textAlign = TextAlign.Center,
                                                                maxLines = 2,
                                                                overflow = TextOverflow.Ellipsis,
                                                                lineHeight = 12.sp
                                                            )
                                                        }
                                                    }
                                                }

                                                // Rellenar celdas vacías si la fila tiene menos de 3
                                                for (i in 0 until (3 - rowItems.size)) {
                                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
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
        }
    }
}