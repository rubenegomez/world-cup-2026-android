package com.example.worldcup2026.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.worldcup2026.data.api.CountryCupTeamDto
import com.example.worldcup2026.data.api.CountryQualificationDto
import com.example.worldcup2026.data.api.CupSlotDto
import com.example.worldcup2026.data.api.DivisionMovementDto
import com.example.worldcup2026.data.api.MovementSlotDto
import com.example.worldcup2026.data.api.NetworkModule
import com.example.worldcup2026.data.api.QualificationSummaryDto

@Composable
fun QualificationSummaryScreen() {
    var summary by remember { mutableStateOf<QualificationSummaryDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshKey) {
        isLoading = true
        errorMessage = null
        try {
            summary = NetworkModule.apiService.getQualificationSummary()
        } catch (e: Exception) {
            errorMessage = "No se pudo cargar la clasificación. Verificá tu conexión."
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFFFFD700),
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "COPAS 2027",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = if (selectedTab == 0) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "ASCENSOS Y DESCENSOS",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = if (selectedTab == 1) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFFD700))
                }
            }
            errorMessage != null || summary == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "Sin datos disponibles",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { refreshKey++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reintentar", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            selectedTab == 0 -> {
                CupsQualificationContent(summary = summary!!, onRefresh = { refreshKey++ })
            }
            else -> {
                DivisionsMovementContent(summary = summary!!, onRefresh = { refreshKey++ })
            }
        }
    }
}

@Composable
private fun CupsQualificationContent(
    summary: QualificationSummaryDto,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CupSectionCard(
                title = "CONMEBOL LIBERTADORES ${summary.target_cups_year}",
                subtitle = "Cupos argentinos · Campeones + Tabla Anual con corrimiento automático",
                accentColor = Color(0xFF00E676),
                slots = summary.libertadores,
                onRefresh = onRefresh
            )
        }

        if (summary.liberated_spots.isNotEmpty() || summary.notes.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CUPOS LIBERADOS Y CORRIMIENTOS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFB300)
                            )
                        }
                        summary.liberated_spots.forEach { msg ->
                            Text(
                                text = "• $msg",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                        summary.notes.forEach { note ->
                            Text(
                                text = "• $note",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            CupSectionCard(
                title = "CONMEBOL SUDAMERICANA ${summary.target_cups_year}",
                subtitle = "6 plazas por Tabla Anual (descontando clasificados a Libertadores y descendidos)",
                accentColor = Color(0xFF00B0FF),
                slots = summary.sudamericana
            )
        }

        if (summary.libertadores_countries.isNotEmpty()) {
            item {
                CupCountriesCard(
                    title = "CLASIFICADOS POR PAÍS · LIBERTADORES ${summary.target_cups_year}",
                    subtitle = "Las 10 asociaciones de Sudamérica · Tocá un país para ver sus equipos",
                    accentColor = Color(0xFFFFD700),
                    countries = summary.libertadores_countries
                )
            }
        }

        if (summary.sudamericana_countries.isNotEmpty()) {
            item {
                CupCountriesCard(
                    title = "CLASIFICADOS POR PAÍS · SUDAMERICANA ${summary.target_cups_year}",
                    subtitle = "Las 10 asociaciones de Sudamérica · Incluye asegurados con opción a Libertadores",
                    accentColor = Color(0xFF00B0FF),
                    countries = summary.sudamericana_countries
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REGLAMENTO DE CLASIFICACIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700)
                        )
                    }
                    Text(
                        text = "• El campeón de Libertadores y el campeón de Sudamericana obtienen plaza directa (cupo extra CONMEBOL) a la fase de grupos de la Libertadores siguiente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• Si un campeón argentino ya ocupa zona de copas en la Tabla Anual, libera su cupo al siguiente equipo mejor ubicado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• Si el campeón de Copa Argentina ya está clasificado a Libertadores, el cupo pasa al subcampeón (o a Tabla Anual si ambos ya clasificaron).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
private fun CupSectionCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    slots: List<CupSlotDto>,
    onRefresh: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 10.sp
                        )
                    }
                }
                if (onRefresh != null) {
                    IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            slots.forEachIndexed { index, slot ->
                CupSlotRow(slot = slot, accentColor = accentColor)
                if (index < slots.size - 1) {
                    HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                }
            }
        }
    }
}

@Composable
private fun CupSlotRow(slot: CupSlotDto, accentColor: Color) {
    val (statusLabel, statusColor) = when (slot.status.uppercase()) {
        "CONFIRMED" -> "CONFIRMADO" to Color(0xFF00E676)
        "CONDITIONAL" -> "CONDICIONAL" to Color(0xFF00B0FF)
        else -> "A DEFINIR" to Color(0xFFFFB300)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (slot.logo_url.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(slot.logo_url)
                    .crossfade(true)
                    .build(),
                contentDescription = slot.team_name,
                modifier = Modifier.size(26.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${slot.slot_order}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = slot.team_name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (slot.is_extra_conmebol) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFD700).copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "CUPO EXTRA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
            Text(
                text = "${slot.source} · ${slot.stage}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
            if (slot.detail.isNotBlank()) {
                Text(
                    text = slot.detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = statusLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = statusColor
            )
        }
    }
}

@Composable
private fun DivisionsMovementContent(
    summary: QualificationSummaryDto,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PIRÁMIDE AFA · TEMPORADA ${summary.season}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700)
                )
                IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }

        summary.divisions.forEach { division ->
            item(key = division.id) {
                DivisionMovementCard(division = division)
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
private fun DivisionMovementCard(division: DivisionMovementDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = division.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            if (division.subtitle.isNotBlank()) {
                Text(
                    text = division.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp
                )
            }

            if (division.ascensos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ASCENSOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E676)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                division.ascensos.forEach { slot ->
                    MovementSlotRow(slot = slot, accentColor = Color(0xFF00E676))
                }
            }

            if (division.descensos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFFFF1744), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DESCENSOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF1744)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                division.descensos.forEach { slot ->
                    MovementSlotRow(slot = slot, accentColor = Color(0xFFFF1744))
                }
            }
        }
    }
}

@Composable
private fun MovementSlotRow(slot: MovementSlotDto, accentColor: Color) {
    val isConfirmed = slot.status.equals("CONFIRMED", ignoreCase = true)
    val badgeColor = if (isConfirmed) accentColor else Color(0xFFFFB300)
    val badgeText = if (isConfirmed) "CONFIRMADO" else "EN DISPUTA"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(accentColor.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (slot.logo_url.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(slot.logo_url)
                    .crossfade(true)
                    .build(),
                contentDescription = slot.team_name,
                modifier = Modifier.size(22.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = slot.team_name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = slot.label,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (slot.detail.isNotBlank()) {
                Text(
                    text = slot.detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .background(badgeColor.copy(alpha = 0.16f), RoundedCornerShape(5.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = badgeText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = badgeColor
            )
        }
    }
}

@Composable
private fun CupCountriesCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    countries: List<CountryQualificationDto>
) {
    var expandedCodes by remember { mutableStateOf(setOf<String>()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Public, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            countries.forEachIndexed { index, country ->
                val isExpanded = country.country_code in expandedCodes
                CountryCupItem(
                    country = country,
                    expanded = isExpanded,
                    onToggle = {
                        expandedCodes = if (isExpanded) {
                            expandedCodes - country.country_code
                        } else {
                            expandedCodes + country.country_code
                        }
                    }
                )
                if (index < countries.size - 1) {
                    HorizontalDivider(
                        thickness = 0.3.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryCupItem(
    country: CountryQualificationDto,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val conditionalCount = country.teams.count { it.status.equals("CONDITIONAL", ignoreCase = true) }
    val isComplete = country.confirmed_count >= country.total_slots
    val pillColor = when {
        isComplete -> Color(0xFF00E676)
        country.confirmed_count == 0 && conditionalCount > 0 -> Color(0xFF00B0FF)
        else -> Color(0xFFFFB300)
    }
    val pillText = when {
        isComplete -> "${country.confirmed_count}/${country.total_slots} ✓"
        country.confirmed_count == 0 && conditionalCount > 0 -> "$conditionalCount/${country.total_slots}*"
        else -> "${country.confirmed_count}/${country.total_slots}"
    }
    val subtitleText = when {
        isComplete -> "${country.confirmed_count} confirmados de ${country.total_slots} cupos · Completo"
        country.confirmed_count > 0 && conditionalCount > 0 ->
            "${country.confirmed_count} confirmados · $conditionalCount con opción a Libertadores (${country.total_slots} cupos)"
        country.confirmed_count == 0 && conditionalCount > 0 ->
            "$conditionalCount asegurados con opción a Libertadores (${country.total_slots} cupos)"
        else -> "${country.confirmed_count} confirmados de ${country.total_slots} cupos"
    }
    val previewTeamsWithLogos = country.teams.filter {
        (it.status.equals("CONFIRMED", ignoreCase = true) ||
            it.status.equals("CONDITIONAL", ignoreCase = true)) &&
            it.logo_url.isNotBlank()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (country.flag_url.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(country.flag_url)
                        .crossfade(true)
                        .build(),
                    contentDescription = country.country_name,
                    modifier = Modifier.size(width = 24.dp, height = 17.dp).clip(RoundedCornerShape(3.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.country_name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (isComplete) Color(0xFF00E676) else MaterialTheme.colorScheme.outline
                )
            }

            if (!expanded && previewTeamsWithLogos.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    previewTeamsWithLogos.take(4).forEach { team ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(team.logo_url)
                                .crossfade(true)
                                .build(),
                            contentDescription = team.team_name,
                            modifier = Modifier.size(18.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier
                    .background(pillColor.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = pillText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = pillColor
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(6.dp))
            if (country.note.isNotBlank()) {
                Text(
                    text = "• ${country.note}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00B0FF),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            country.teams.forEach { team ->
                CountryCupTeamRow(team = team)
            }
        }
    }
}

@Composable
private fun CountryCupTeamRow(team: CountryCupTeamDto) {
    val (badgeLabel, badgeColor) = when (team.status.uppercase()) {
        "CONFIRMED" -> "CONFIRMADO" to Color(0xFF00E676)
        "CONDITIONAL" -> "OPCIÓN LIB." to Color(0xFF00B0FF)
        else -> "A DEFINIR" to Color(0xFFFFB300)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (team.logo_url.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(team.logo_url)
                    .crossfade(true)
                    .build(),
                contentDescription = team.team_name,
                modifier = Modifier.size(22.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = badgeColor, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = team.team_name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val routeText = buildString {
                append(team.source)
                if (team.stage.isNotBlank()) {
                    append(" · ")
                    append(team.stage)
                }
            }
            Text(
                text = routeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(5.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = badgeLabel,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = badgeColor
            )
        }
    }
}
