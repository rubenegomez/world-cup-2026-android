package com.example.worldcup2026.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.worldcup2026.data.model.Match
import java.time.LocalDate
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

enum class CalendarViewMode {
    MONTH, WEEK, DAY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: WorldCupViewModel,
    matches: List<Match>,
    onNavigateToMatches: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("world_cup_prefs", Context.MODE_PRIVATE) }
    var viewMode by remember { 
        val defaultView = sharedPrefs.getString("default_calendar_view", "MONTH") ?: "MONTH"
        mutableStateOf(CalendarViewMode.valueOf(defaultView))
    }

    // Obtenemos partidos para la fecha seleccionada
    val matchesForSelectedDate = remember(matches, selectedDate) {
        val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        matches.filter { it.date == dateStr }.sortedBy { it.clock ?: "00:00" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Cabecera del Calendario
        CalendarHeader(
            selectedDate = selectedDate,
            viewMode = viewMode,
            onViewModeChanged = { viewMode = it },
            onDateSelected = { 
                selectedDate = it 
                onNavigateToMatches(it)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Vistas de Selector
        AnimatedContent(targetState = viewMode, label = "CalendarView", modifier = Modifier.weight(1f)) { mode ->
            when (mode) {
                CalendarViewMode.DAY -> DaySelector(
                    selectedDate = selectedDate,
                    onDateChange = { selectedDate = it },
                    onNavigateToMatches = { onNavigateToMatches(selectedDate) }
                )
                CalendarViewMode.WEEK -> WeekSelector(
                    selectedDate = selectedDate,
                    globalMatches = matches,
                    onDateChange = { 
                        selectedDate = it
                        onNavigateToMatches(it)
                    }
                )
                CalendarViewMode.MONTH -> MonthSelector(
                    selectedDate = selectedDate,
                    globalMatches = matches,
                    onDateChange = { 
                        selectedDate = it
                        onNavigateToMatches(it)
                    }
                )
            }
        }
    }
}

@Composable
fun CalendarHeader(
    selectedDate: LocalDate,
    viewMode: CalendarViewMode,
    onViewModeChanged: (CalendarViewMode) -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val monthName = selectedDate.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val year = selectedDate.year

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val newDate = when (viewMode) {
                        CalendarViewMode.MONTH -> selectedDate.minusMonths(1)
                        CalendarViewMode.WEEK -> selectedDate.minusWeeks(1)
                        CalendarViewMode.DAY -> selectedDate.minusDays(1)
                    }
                    onDateSelected(newDate)
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = "$monthName $year",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = {
                    val newDate = when (viewMode) {
                        CalendarViewMode.MONTH -> selectedDate.plusMonths(1)
                        CalendarViewMode.WEEK -> selectedDate.plusWeeks(1)
                        CalendarViewMode.DAY -> selectedDate.plusDays(1)
                    }
                    onDateSelected(newDate)
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
            IconButton(
                onClick = { onDateSelected(LocalDate.now()) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = "Ir a hoy",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented Control (Simple implementation)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CalendarViewMode.values().forEach { mode ->
                val isSelected = viewMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onViewModeChanged(mode) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when(mode) {
                            CalendarViewMode.MONTH -> "Mes"
                            CalendarViewMode.WEEK -> "Semana"
                            CalendarViewMode.DAY -> "Día"
                        },
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DaySelector(selectedDate: LocalDate, onDateChange: (LocalDate) -> Unit, onNavigateToMatches: () -> Unit) {
    val dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDateChange(selectedDate.minusDays(1)) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Día anterior", modifier = Modifier.size(32.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onNavigateToMatches() }
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Text(
                    text = selectedDate.dayOfMonth.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            IconButton(onClick = { onDateChange(selectedDate.plusDays(1)) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Día siguiente", modifier = Modifier.size(32.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNavigateToMatches,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Ver Partidos", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun WeekSelector(selectedDate: LocalDate, globalMatches: List<Match>, onDateChange: (LocalDate) -> Unit) {
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDays.forEach { date ->
            val isSelected = date == selectedDate
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES")).take(3)
            
            val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val hasMatch = remember(globalMatches, dateStr) { globalMatches.any { it.date == dateStr } }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onDateChange(date) }
                    .padding(8.dp)
            ) {
                Text(
                    text = dayName.replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
                
                // Indicador de partido
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (hasMatch) (if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary) else Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun MonthSelector(selectedDate: LocalDate, globalMatches: List<Match>, onDateChange: (LocalDate) -> Unit) {
    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val daysInMonth = selectedDate.lengthOfMonth()
    val startingDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 
    
    val days = (1..daysInMonth).map { firstDayOfMonth.withDayOfMonth(it) }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        val dayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        items(dayNames.size) { index ->
            Text(
                text = dayNames[index],
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        val startOffset = if (startingDayOfWeek == 0) 6 else startingDayOfWeek - 1
        items(startOffset) {
            Spacer(modifier = Modifier.size(40.dp))
        }
        
        items(days) { date ->
            val isSelected = date == selectedDate
            val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val hasMatch = remember(globalMatches, dateStr) { globalMatches.any { it.date == dateStr } }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .border(
                        width = if (hasMatch && !isSelected) 1.5.dp else 0.dp,
                        color = if (hasMatch && !isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onDateChange(date) }
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
