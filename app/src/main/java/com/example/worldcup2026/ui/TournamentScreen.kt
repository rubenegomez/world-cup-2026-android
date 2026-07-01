package com.example.worldcup2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TournamentItem(val id: Int, val name: String, val type: String, val gradient: List<Color>)

val internacionales = listOf(
    TournamentItem(1, "World Cup 2026", "Internacional", listOf(Color(0xFF1E3C72), Color(0xFF2A5298))),
    TournamentItem(2, "Eliminatorias Mundial 2030", "Internacional", listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))),
    TournamentItem(3, "CONMEBOL Libertadores", "Internacional", listOf(Color(0xFF000000), Color(0xFF434343))),
    TournamentItem(4, "CONMEBOL Sudamericana", "Internacional", listOf(Color(0xFF141E30), Color(0xFF243B55)))
)

val nacionales = listOf(
    TournamentItem(5, "Liga Profesional de Fútbol", "Nacional", listOf(Color(0xFF1D976C), Color(0xFF93F9B9))),
    TournamentItem(6, "Copa Argentina", "Nacional", listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
    TournamentItem(7, "Supercopa Argentina", "Nacional", listOf(Color(0xFF1F1C2C), Color(0xFF928DAB))),
    TournamentItem(8, "Copa Superliga de Argentina", "Nacional", listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))),
    TournamentItem(9, "Primera Nacional", "Nacional", listOf(Color(0xFF373B44), Color(0xFF4286f4))),
    TournamentItem(10, "Primera B", "Nacional", listOf(Color(0xFFFF4B2B), Color(0xFFFF416C))),
    TournamentItem(11, "Primera C", "Nacional", listOf(Color(0xFFf12711), Color(0xFFf5af19))),
    TournamentItem(12, "Primera D", "Nacional", listOf(Color(0xFF654ea3), Color(0xFFeaafc8)))
)

@Composable
fun TournamentScreen(onTournamentSelected: (Int, String) -> Unit) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Internacionales", "Nacionales")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        val tournamentsToList = if (selectedTabIndex == 0) internacionales else nacionales

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tournamentsToList) { tournament ->
                TournamentCard(tournament, onClick = { onTournamentSelected(tournament.id, tournament.name) })
            }
        }
    }
}

@Composable
fun TournamentCard(tournament: TournamentItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(tournament.gradient))
            .clickable { onClick() }
            .padding(24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = tournament.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tocar para ver fixture",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
