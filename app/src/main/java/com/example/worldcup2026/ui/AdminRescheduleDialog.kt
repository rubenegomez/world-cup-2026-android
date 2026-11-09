package com.example.worldcup2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.worldcup2026.data.api.AuthManager
import com.example.worldcup2026.data.api.NetworkModule
import com.example.worldcup2026.data.api.RescheduleRoundRequest
import com.example.worldcup2026.data.api.RescheduleMatchRequest
import com.example.worldcup2026.data.api.RoundSummaryItem
import com.example.worldcup2026.data.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRescheduleDialog(
    initialTournamentId: Int = 5, // Por defecto Liga Profesional
    initialMatchday: Int = 16,     // Por defecto Fecha 16
    onDismiss: () -> Unit,
    onSuccess: (updatedCount: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isIndividualMode by remember { mutableStateOf(false) } // false = Lote / toda la fecha, true = Un partido
    var selectedTournamentId by remember { mutableStateOf(initialTournamentId) }
    var selectedMatchday by remember { mutableStateOf(initialMatchday) }
    var daysOffset by remember { mutableStateOf(-1) } // Por defecto -1 día (adelantar)
    var customDate by remember { mutableStateOf("") }
    var isOverrideChecked by remember { mutableStateOf(true) }
    
    var isLoadingSummary by remember { mutableStateOf(false) }
    var isLoadingMatches by remember { mutableStateOf(false) }
    var isExecuting by remember { mutableStateOf(false) }
    var availableRounds by remember { mutableStateOf<List<RoundSummaryItem>>(emptyList()) }
    var matchesInRound by remember { mutableStateOf<List<Match>>(emptyList()) }
    var selectedMatchId by remember { mutableStateOf<Int?>(null) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorMessage by remember { mutableStateOf(false) }

    val adminEmail = remember { AuthManager.ADMIN_EMAIL }

    // Torneos principales para reprogramación
    val tournamentOptions = remember {
        listOf(
            5 to "Liga Profesional",
            7 to "Primera Nacional",
            8 to "Primera B Metro",
            10 to "Primera C",
            6 to "Copa Argentina",
            3 to "Libertadores",
            4 to "Sudamericana"
        )
    }

    fun loadRounds(tId: Int) {
        isLoadingSummary = true
        coroutineScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    NetworkModule.apiService.getRoundsSummary(tId, adminEmail)
                }
                availableRounds = res.rounds
                if (res.rounds.any { it.matchday == selectedMatchday }) {
                    // Mantener
                } else if (res.rounds.isNotEmpty()) {
                    selectedMatchday = res.rounds.last().matchday
                }
            } catch (e: Exception) {
                // Silencioso o fallback
            } finally {
                isLoadingSummary = false
            }
        }
    }

    fun loadMatches(tId: Int, mDay: Int) {
        isLoadingMatches = true
        coroutineScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    NetworkModule.apiService.getMatches(tId)
                }
                matchesInRound = list.filter { it.matchday == mDay }
                if (matchesInRound.isNotEmpty() && (selectedMatchId == null || !matchesInRound.any { it.id == selectedMatchId })) {
                    selectedMatchId = matchesInRound.first().id
                }
            } catch (e: Exception) {
                matchesInRound = emptyList()
            } finally {
                isLoadingMatches = false
            }
        }
    }

    LaunchedEffect(selectedTournamentId) {
        loadRounds(selectedTournamentId)
        loadMatches(selectedTournamentId, selectedMatchday)
    }

    LaunchedEffect(selectedMatchday) {
        loadMatches(selectedTournamentId, selectedMatchday)
    }

    val currentRoundInfo = remember(availableRounds, selectedMatchday) {
        availableRounds.find { it.matchday == selectedMatchday }
    }

    val selectedMatch = remember(matchesInRound, selectedMatchId) {
        matchesInRound.find { it.id == selectedMatchId }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("REPROGRAMAR CALENDARIO", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                            Text("Adelantar o atrasar partidos y fechas", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.15f))

                // Mensajes de estado
                if (statusMessage != null) {
                    Surface(
                        color = if (isErrorMessage) Color(0xFFD32F2F).copy(alpha = 0.2f) else Color(0xFF00E676).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isErrorMessage) Color(0xFFE57373) else Color(0xFF00E676)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            fontSize = 12.sp,
                            color = if (isErrorMessage) Color(0xFFFFCDD2) else Color(0xFFB9F6CA),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Selector de Modo (LOTE vs INDIVIDUAL)
                Text("MODO DE OPERACIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (!isIndividualMode) Color(0xFF00E676) else Color(0xFF1E2638),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isIndividualMode = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📅 TODA LA FECHA (Lote)",
                                color = if (!isIndividualMode) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        color = if (isIndividualMode) Color(0xFF00E676) else Color(0xFF1E2638),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isIndividualMode = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⚽ UN SOLO PARTIDO",
                                color = if (isIndividualMode) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // 1. Selector de Torneo
                Text("1. TORNEO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    items(tournamentOptions) { (tId, tLabel) ->
                        val isSelected = tId == selectedTournamentId
                        Surface(
                            color = if (isSelected) Color(0xFF00E676) else Color(0xFF1E2638),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable { selectedTournamentId = tId }
                        ) {
                            Text(
                                text = tLabel,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 2. Número de Fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2. FECHA / JORNADA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                    if (currentRoundInfo != null) {
                        Text(
                            "${currentRoundInfo.total_matches} partidos (${currentRoundInfo.min_date?.take(10) ?: ""})",
                            fontSize = 11.sp,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Selector rápido de número de fecha
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    val roundsList = if (availableRounds.isNotEmpty()) availableRounds.map { it.matchday } else (1..27).toList()
                    items(roundsList) { rNum ->
                        val isSelected = rNum == selectedMatchday
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (isSelected) Color(0xFF00E676) else Color(0xFF1E2638),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedMatchday = rNum },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$rNum",
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Si es modo individual, mostrar selector de partido
                if (isIndividualMode) {
                    Text("PARTIDO A REPROGRAMAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isLoadingMatches) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00E676), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cargando partidos de la fecha...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    } else if (matchesInRound.isEmpty()) {
                        Text("No se encontraron partidos para esta fecha.", fontSize = 12.sp, color = Color(0xFFFF8A80))
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            matchesInRound.forEach { match ->
                                val isSelected = match.id == selectedMatchId
                                Surface(
                                    color = if (isSelected) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF1E2638),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF00E676) else Color.White.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedMatchId = match.id }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "${match.homeTeam.name} vs ${match.awayTeam.name}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color(0xFF00E676) else Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                "Horario: ${match.date ?: "Sin fecha"} (${match.status})",
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                        if (isSelected) {
                                            Text("SELECCIONADO", fontWeight = FontWeight.Black, fontSize = 10.sp, color = Color(0xFF00E676))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Desplazamiento (Días) - ADELANTAR Y ATRASAR
                Text("3. DESPLAZAMIENTO (ADELANTAR O ATRASAR)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                Spacer(modifier = Modifier.height(4.dp))

                // Fila Adelantar (hacia el pasado / restar días)
                Text("◀ ADELANTAR (Mover al pasado / restarle días):", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF40C4FF))
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        -1 to "-1 día",
                        -2 to "-2 días",
                        -3 to "-3 días",
                        -7 to "-7 días (1 sem)"
                    ).forEach { (d, label) ->
                        val isSelected = daysOffset == d && customDate.isBlank()
                        Surface(
                            color = if (isSelected) Color(0xFF00B0FF) else Color(0xFF1E2638),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    daysOffset = d
                                    customDate = ""
                                }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }

                // Fila Atrasar (hacia el futuro / sumar días)
                Text("▶ ATRASAR (Mover al futuro / sumarle días):", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF69F0AE))
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        1 to "+1 día",
                        2 to "+2 días",
                        3 to "+3 días",
                        7 to "+7 días (1 sem)"
                    ).forEach { (d, label) ->
                        val isSelected = daysOffset == d && customDate.isBlank()
                        Surface(
                            color = if (isSelected) Color(0xFF00E676) else Color(0xFF1E2638),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    daysOffset = d
                                    customDate = ""
                                }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }

                // Campo opcional de fecha específica
                OutlinedTextField(
                    value = customDate,
                    onValueChange = { customDate = it },
                    label = { Text("O ingresar nueva fecha base exacta (YYYY-MM-DD)", fontSize = 11.sp) },
                    placeholder = { Text("Ej: 2026-11-07", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Protección manual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isOverrideChecked,
                        onCheckedChange = { isOverrideChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Blindar con protección manual (evita que el scraper lo revierta)",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de Acción Principal
                Button(
                    onClick = {
                        isExecuting = true
                        statusMessage = null
                        coroutineScope.launch {
                            try {
                                if (isIndividualMode) {
                                    if (selectedMatchId == null) {
                                        statusMessage = "❌ Por favor selecciona un partido para reprogramar."
                                        isErrorMessage = true
                                        isExecuting = false
                                        return@launch
                                    }
                                    val req = RescheduleMatchRequest(
                                        match_id = selectedMatchId!!,
                                        days_offset = if (customDate.isNotBlank()) 0 else daysOffset,
                                        new_date = if (customDate.isNotBlank()) customDate.trim() else null,
                                        is_manual_override = isOverrideChecked
                                    )
                                    val res = withContext(Dispatchers.IO) {
                                        NetworkModule.apiService.rescheduleMatchAdmin(adminEmail, req)
                                    }
                                    if (res.isSuccessful && res.body() != null) {
                                        val mData = res.body()!!
                                        statusMessage = "✅ ¡Partido reprogramado con éxito a ${mData.new_date}!"
                                        isErrorMessage = false
                                        loadMatches(selectedTournamentId, selectedMatchday)
                                        onSuccess(1)
                                    } else {
                                        statusMessage = "❌ Error en el servidor: código ${res.code()}"
                                        isErrorMessage = true
                                    }
                                } else {
                                    val req = RescheduleRoundRequest(
                                        tournament_id = selectedTournamentId,
                                        matchday = selectedMatchday,
                                        days_offset = if (customDate.isNotBlank()) 0 else daysOffset,
                                        new_date = if (customDate.isNotBlank()) customDate.trim() else null,
                                        is_manual_override = isOverrideChecked
                                    )
                                    val res = withContext(Dispatchers.IO) {
                                        NetworkModule.apiService.rescheduleRoundAdmin(adminEmail, req)
                                    }
                                    if (res.isSuccessful && res.body() != null) {
                                        val count = res.body()!!.updated_count
                                        val actionLabel = if (daysOffset < 0) "adelantada" else "reprogramada"
                                        statusMessage = "✅ ¡Fecha $selectedMatchday $actionLabel exitosamente! ($count partidos actualizados)"
                                        isErrorMessage = false
                                        loadMatches(selectedTournamentId, selectedMatchday)
                                        onSuccess(count)
                                    } else {
                                        statusMessage = "❌ Error en el servidor: código ${res.code()}"
                                        isErrorMessage = true
                                    }
                                }
                            } catch (e: Exception) {
                                statusMessage = "❌ Error: ${e.message}"
                                isErrorMessage = true
                            } finally {
                                isExecuting = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (daysOffset < 0 && customDate.isBlank()) Color(0xFF00B0FF) else Color(0xFF00E676)
                    ),
                    enabled = !isExecuting
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("APLICANDO CAMBIOS...", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Black)
                    } else {
                        val icon = if (daysOffset < 0) Icons.Default.FastRewind else Icons.Default.Schedule
                        Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val label = if (isIndividualMode) {
                            val target = selectedMatch?.let { "${it.homeTeam.name.take(10)} vs ${it.awayTeam.name.take(10)}" } ?: "PARTIDO"
                            if (daysOffset < 0) "ADELANTAR $target ($daysOffset DÍAS)"
                            else "REPROGRAMAR $target (+$daysOffset DÍAS)"
                        } else {
                            if (daysOffset < 0) "ADELANTAR FECHA $selectedMatchday ($daysOffset DÍAS)"
                            else "ATRASAR / MOVER FECHA $selectedMatchday (+$daysOffset DÍAS)"
                        }
                        Text(
                            text = label,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
