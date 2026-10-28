package com.example.worldcup2026.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
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

data class GridTournamentItem(
    val id: Int,
    val name: String,
    val logoUrl: String? = null,
    val isTodos: Boolean = false,
    val isLive: Boolean = false,
    val hasLive: Boolean = false,
    val isActive: Boolean = false
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
    onDismiss: () -> Unit,
    onSelectTournament: (Int) -> Unit,
    onSelectTodos: (() -> Unit)? = null,
    onSelectLiveOnly: (() -> Unit)? = null,
    onTournamentUpdated: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var editingTournamentId by remember { mutableStateOf<Int?>(null) }
    var versionTrigger by remember { mutableIntStateOf(0) }

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

    val gridItems = remember(apiTournaments, liveTournamentsMap, versionTrigger) {
        val result = mutableListOf<GridTournamentItem>()

        if (includeAllOption) {
            result.add(GridTournamentItem(id = 0, name = "Todos", isTodos = true))
        }
        if (includeLiveOption) {
            val anyLive = liveTournamentsMap.values.any { it }
            result.add(GridTournamentItem(id = -1, name = "En Vivo", isLive = true, hasLive = anyLive))
        }

        val registeredIds = mutableSetOf<Int>()
        for (apiT in apiTournaments) {
            registeredIds.add(apiT.id)
            val customName = MasterTeamCatalog.getTournamentDisplayName(context, apiT.id)
            val customLogo = MasterTeamCatalog.getTournamentLogo(context, apiT.id) ?: apiT.logo_url
            val isActive = MasterTeamCatalog.ACTIVE_TOURNAMENT_IDS.contains(apiT.id)
            val hasLive = liveTournamentsMap[apiT.id] == true
            result.add(
                GridTournamentItem(
                    id = apiT.id,
                    name = if (customName.isNotBlank()) customName else apiT.name,
                    logoUrl = customLogo,
                    hasLive = hasLive,
                    isActive = isActive
                )
            )
        }

        for (masterT in MasterTeamCatalog.MASTER_TOURNAMENTS) {
            if (!registeredIds.contains(masterT.id)) {
                val customName = MasterTeamCatalog.getTournamentDisplayName(context, masterT.id)
                val customLogo = MasterTeamCatalog.getTournamentLogo(context, masterT.id) ?: masterT.logoUrl
                val hasLive = liveTournamentsMap[masterT.id] == true
                result.add(
                    GridTournamentItem(
                        id = masterT.id,
                        name = if (customName.isNotBlank()) customName else masterT.displayName,
                        logoUrl = customLogo,
                        hasLive = hasLive,
                        isActive = masterT.isActive
                    )
                )
            }
        }

        val special = result.filter { it.isTodos || it.isLive }
        val active = result.filter { !it.isTodos && !it.isLive && it.isActive }
        val inactive = result.filter { !it.isTodos && !it.isLive && !it.isActive }
        special + active + inactive
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏆 Seleccionar Torneo",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(gridItems, key = { if (it.isTodos) "todos" else if (it.isLive) "live" else "t_${it.id}" }) { item ->
                        val isSelected = when {
                            item.isTodos -> !filterLiveOnly && (selectedTournamentId == null || selectedTournamentId == 0)
                            item.isLive -> filterLiveOnly
                            else -> !filterLiveOnly && selectedTournamentId == item.id
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) Color(0xFFFFD700).copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .combinedClickable(
                                    onClick = {
                                        com.example.worldcup2026.data.util.SoundManager.playTic()
                                        when {
                                            item.isTodos -> onSelectTodos?.invoke()
                                            item.isLive -> onSelectLiveOnly?.invoke()
                                            else -> onSelectTournament(item.id)
                                        }
                                        onDismiss()
                                    },
                                    onLongClick = {
                                        if (!item.isTodos && !item.isLive) {
                                            editingTournamentId = item.id
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
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
                                    when {
                                        item.isTodos -> Text("🌐", fontSize = 24.sp)
                                        item.isLive -> Text("🔴", fontSize = 22.sp)
                                        !item.logoUrl.isNullOrBlank() -> {
                                            AsyncImage(
                                                model = item.logoUrl,
                                                contentDescription = item.name,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    // Indicador de partidos en vivo en la esquina superior
                                    if (item.hasLive && !item.isLive) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .align(Alignment.TopEnd)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE53935))
                                                .border(1.dp, Color.White, CircleShape)
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
                }
            }
        }
    }
}
