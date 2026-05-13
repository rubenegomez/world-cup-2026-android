package com.example.worldcup2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team

@Composable
fun StandingsScreen(matches: List<Match>) {
    val teamsByGroup = matches.flatMap { listOf(it.homeTeam, it.awayTeam) }
        .distinctBy { it.id }
        .filter { it.id > 0 } // Solo equipos reales
        .groupBy { it.group }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        teamsByGroup.forEach { (groupName, teams) ->
            item {
                GroupStandingsTable(groupName, teams, matches)
            }
        }
    }
}

@Composable
fun GroupStandingsTable(groupName: String, teams: List<Team>, matches: List<Match>) {
    val standings = teams.map { team ->
        calculateTeamStats(team, matches)
    }.sortedWith(compareByDescending<TeamStats> { it.pts }.thenByDescending { it.gd }.thenByDescending { it.gf })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Grupo $groupName",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        // Cabecera idéntica a la imagen del usuario
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pos", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("Equipo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("PJ", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("G", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("E", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("P", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("GF", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("GC", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("DG", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("Pts", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        standings.forEachIndexed { index, stats ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${index + 1}", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(stats.team.flagUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Text(
                    text = stats.team.name,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (index < 2) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text("${stats.pj}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.g}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.e}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.p}", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.gf}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.ga}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.gd}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text("${stats.pts}", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black)
            }
            if (index < standings.size - 1) {
                HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

data class TeamStats(
    val team: Team,
    val pj: Int,
    val g: Int,
    val e: Int,
    val p: Int,
    val gf: Int,
    val ga: Int,
    val gd: Int,
    val pts: Int
)

fun calculateTeamStats(team: Team, matches: List<Match>): TeamStats {
    var pj = 0
    var g = 0
    var e = 0
    var p = 0
    var gf = 0
    var ga = 0
    var pts = 0

    matches.filter { it.id <= 100 }.forEach { match ->
        if (match.homeTeam.id == team.id || match.awayTeam.id == team.id) {
            val hScore = match.homeScore
            val aScore = match.awayScore
            
            if (hScore != null && aScore != null) {
                pj++
                val (teamScore, opponentScore) = if (match.homeTeam.id == team.id) hScore to aScore else aScore to hScore
                
                gf += teamScore
                ga += opponentScore
                
                when {
                    teamScore > opponentScore -> {
                        g++
                        pts += 3
                    }
                    teamScore == opponentScore -> {
                        e++
                        pts += 1
                    }
                    else -> p++
                }
            }
        }
    }

    return TeamStats(team, pj, g, e, p, gf, ga, gf - ga, pts)
}
