package com.example.worldcup2026.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.example.worldcup2026.data.model.Team
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TeamsScreen(
    teams: List<Team>, 
    favoriteTeamNames: Set<String> = emptySet(),
    onTeamClick: (Team) -> Unit = {}
) {
    // 1. Equipos Favoritos al inicio
    val favTeams = remember(teams, favoriteTeamNames) {
        teams.filter { it.name in favoriteTeamNames }.sortedBy { it.name }
    }

    // 2. Mapeo oficial de nombres de torneos
    val tournamentNames = remember {
        mapOf(
            5 to "🏆 LIGA PROFESIONAL",
            7 to "⚽ PRIMERA NACIONAL",
            8 to "🏟️ PRIMERA B METROPOLITANA",
            15 to "🏔️ TORNEO FEDERAL A",
            10 to "🥅 PRIMERA C METROPOLITANA",
            16 to "🚩 TORNEO REGIONAL FEDERAL AMATEUR",
            13 to "🎖️ TORNEO PROMOCIONAL AMATEUR",
            6 to "🇦🇷 COPA ARGENTINA",
            1 to "🌍 MUNDIAL 2026",
            2 to "🌎 ELIMINATORIAS SUDAMERICANAS",
            3 to "🏆 COPA LIBERTADORES",
            4 to "🏆 COPA SUDAMERICANA"
        )
    }

    // 3. Orden de prioridad jerárquica de torneos
    val tournamentOrder = remember {
        listOf(5, 6, 7, 8, 15, 10, 16, 13, 1, 2, 3, 4, 12, 14)
    }

    // 4. Agrupación por torneo y orden alfabético A-Z dentro de cada torneo
    val groupedByTournament = remember(teams) {
        teams.groupBy { it.tournament_id ?: 5 }
            .mapValues { (_, teamList) -> teamList.sortedBy { it.name } }
            .entries
            .sortedBy { (tId, _) ->
                val idx = tournamentOrder.indexOf(tId)
                if (idx != -1) idx else 999
            }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sección de Favoritos
        if (favTeams.isNotEmpty()) {
            item {
                SectionHeader(title = "⭐ MIS EQUIPOS FAVORITOS")
            }
            item {
                TeamGridRow(teams = favTeams, onTeamClick = onTeamClick)
            }
        }

        // Secciones por Torneo
        groupedByTournament.forEach { (tId, teamList) ->
            val title = tournamentNames[tId] ?: "⚽ TORNEO #$tId"
            item {
                SectionHeader(title = "$title (${teamList.size})")
            }
            item {
                TeamGridRow(teams = teamList, onTeamClick = onTeamClick)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
fun TeamGridRow(teams: List<Team>, onTeamClick: (Team) -> Unit) {
    val rows = teams.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowTeams ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTeams.forEach { team ->
                    Box(modifier = Modifier.weight(1f)) {
                        TeamCard(team = team, onClick = { onTeamClick(team) })
                    }
                }
                repeat(3 - rowTeams.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun TeamCard(team: Team, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(team.flagUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "${team.name} flag",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = team.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSize = 11.sp
            )
        }
    }
}
