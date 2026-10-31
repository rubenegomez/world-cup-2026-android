package com.example.worldcup2026.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Player
import com.example.worldcup2026.data.model.Team

/**
 * Función para acortar nombres en sustituciones o canchas:
 * Si tiene varios nombres/apellidos, prioriza la inicial + apellido más conocido.
 */
fun shortenPlayerName(fullName: String): String {
    val clean = fullName
        .replace("🟢", "").replace("🔴", "").replace("⚽", "").replace("🔄", "")
        .replace("[", "").replace("]", "").trim()
    val parts = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> clean
        parts.size == 1 -> parts[0]
        parts.size == 2 -> "${parts[0].first()}. ${parts[1]}"
        else -> "${parts[0].first()}. ${parts.last()}"
    }
}

/**
 * Badge rectangular verde "ENTRA" estilo cartel del cuarto árbitro
 */
@Composable
fun EntraBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(3.dp),
        color = Color(0xFF2E7D32),
        border = BorderStroke(1.dp, Color(0xFF1B5E20)),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Text(
            text = "ENTRA",
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
        )
    }
}

/**
 * Badge rectangular rojo "SALE" estilo cartel del cuarto árbitro
 */
@Composable
fun SaleBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(3.dp),
        color = Color(0xFFC62828),
        border = BorderStroke(1.dp, Color(0xFFB71C1C)),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Text(
            text = "SALE",
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
        )
    }
}

/**
 * Fila de sustitución con los badges oficiales ENTRA y SALE
 */
@Composable
fun SubstitutionBadgesRow(
    playerIn: String,
    playerOut: String,
    isHome: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isHome) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // ENTRA
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isHome) Arrangement.End else Arrangement.Start
        ) {
            if (!isHome) {
                EntraBadge()
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = shortenPlayerName(playerIn),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = shortenPlayerName(playerIn),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                EntraBadge()
            }
        }

        // SALE
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isHome) Arrangement.End else Arrangement.Start
        ) {
            if (!isHome) {
                SaleBadge()
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = shortenPlayerName(playerOut),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = shortenPlayerName(playerOut),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                SaleBadge()
            }
        }
    }
}

/**
 * Barra de estadísticas comparativas con diseño limpio y barras verdes estilo Anulo Mufo
 */
@Composable
fun AnuloMufoStatBar(
    label: String,
    homeVal: Int,
    awayVal: Int,
    homeStr: String = "$homeVal",
    awayStr: String = "$awayVal",
    isPercentage: Boolean = false
) {
    val total = if (isPercentage) 100 else (homeVal + awayVal).coerceAtLeast(1)
    val homeWeight = (homeVal.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val awayWeight = (awayVal.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(3.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Valor Local
            Text(
                text = homeStr,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.Start
            )

            // Barras visuales comparativas
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(9.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2A2E39)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Barra local (desde izquierda a derecha en verde brillante)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(end = 1.dp)
                        .background(Color(0xFF1E222D)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(homeWeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF00E676))
                    )
                }

                // Barra visitante (desde derecha a izquierda en gris plata / verde suave)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(start = 1.dp)
                        .background(Color(0xFF1E222D)),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(awayWeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF81C784))
                    )
                }
            }

            // Valor Visitante
            Text(
                text = awayStr,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

data class PitchPlayer(
    val number: Int,
    val name: String,
    val xPercent: Float, // 0.0f (Arco Local) a 1.0f (Arco Visitante)
    val yPercent: Float, // 0.0f (Arriba) a 1.0f (Abajo)
    val isHome: Boolean
)

/**
 * Genera la lista de 22 jugadores ubicados tácticamente en una cancha horizontal,
 * con los dos equipos enfrentados (Delantera Local frente a Defensa Visitante, y viceversa).
 */
fun buildTacticalLineup(match: Match): Pair<List<PitchPlayer>, Pair<String, String>> {
    val hName = match.homeTeam.name
    val aName = match.awayTeam.name

    // 1. Planteles / Nombres conocidos
    val homeKnownNames = getRosterOrDefaults(hName, match.homeTeam.players, isHome = true)
    val awayKnownNames = getRosterOrDefaults(aName, match.awayTeam.players, isHome = false)

    // Esquemas tácticos (por defecto 4-4-2 o 4-3-3 o 3-4-2-1)
    val homeFormation = if (hName.contains("Riestra", ignoreCase = true)) "3-4-2-1" else "4-3-3"
    val awayFormation = if (aName.contains("Talleres", ignoreCase = true)) "3-4-3" else "4-4-2"

    val playersList = mutableListOf<PitchPlayer>()

    // LOCAL (Ataca hacia la derecha -> Arquero en X=0.07, Defensas en X=0.22, Medios en X=0.40, Delanteros en X=0.62)
    // Arquero (PO)
    playersList.add(PitchPlayer(homeKnownNames[0].first, homeKnownNames[0].second, 0.07f, 0.50f, true))
    // Defensas (DF) - 4 jugadores
    val hDefYs = listOf(0.18f, 0.39f, 0.61f, 0.82f)
    for (i in 0 until 4) {
        val p = homeKnownNames.getOrElse(1 + i) { 2 + i to "Defensa ${i + 1}" }
        playersList.add(PitchPlayer(p.first, p.second, 0.22f, hDefYs[i], true))
    }
    // Mediocampistas (MC) - 3 jugadores
    val hMedYs = listOf(0.28f, 0.50f, 0.72f)
    for (i in 0 until 3) {
        val p = homeKnownNames.getOrElse(5 + i) { 6 + i to "Medio ${i + 1}" }
        playersList.add(PitchPlayer(p.first, p.second, 0.40f, hMedYs[i], true))
    }
    // Delanteros (DL) - 3 jugadores (avanzados en campo rival enfrentando la defensa visitante)
    val hDelYs = listOf(0.26f, 0.50f, 0.74f)
    for (i in 0 until 3) {
        val p = homeKnownNames.getOrElse(8 + i) { 9 + i to "Delantero ${i + 1}" }
        playersList.add(PitchPlayer(p.first, p.second, 0.62f, hDelYs[i], true))
    }

    // VISITANTE (Ataca hacia la izquierda -> Arquero en X=0.93, Defensas en X=0.78, Medios en X=0.60, Delanteros en X=0.38)
    // Arquero (PO)
    playersList.add(PitchPlayer(awayKnownNames[0].first, awayKnownNames[0].second, 0.93f, 0.50f, false))
    // Defensas (DF) - 4 jugadores (enfrentan la delantera local)
    val aDefYs = listOf(0.18f, 0.39f, 0.61f, 0.82f)
    for (i in 0 until 4) {
        val p = awayKnownNames.getOrElse(1 + i) { 2 + i to "Defensa ${i + 1}" }
        playersList.add(PitchPlayer(p.first, p.second, 0.78f, aDefYs[i], false))
    }
    // Mediocampistas (MC) - 3 jugadores (enfrentan el mediocampo local)
    val aMedYs = listOf(0.28f, 0.50f, 0.72f)
    for (i in 0 until 3) {
        val p = awayKnownNames.getOrElse(5 + i) { 6 + i to "Medio ${i + 1}" }
        playersList.add(PitchPlayer(p.first, p.second, 0.60f, aMedYs[i], false))
    }
    // Delanteros (DL) - 3 jugadores (avanzados en campo local enfrentando la defensa local)
    val aDelYs = listOf(0.26f, 0.50f, 0.74f)
    for (i in 0 until 3) {
        val p = awayKnownNames.getOrElse(8 + i) { 9 + i to "Delantero ${i + 1}" }
        playersList.add(PitchPlayer(p.first, p.second, 0.38f, aDelYs[i], false))
    }

    return playersList to (homeFormation to awayFormation)
}

private fun getRosterOrDefaults(teamName: String, players: List<Player>?, isHome: Boolean): List<Pair<Int, String>> {
    if (!players.isNullOrEmpty() && players.size >= 11) {
        return players.take(11).mapIndexed { idx, pl -> (idx + 1) to pl.name }
    }
    val tn = teamName.lowercase()
    return when {
        tn.contains("riestra") -> listOf(
            23 to "Arzamendia", 24 to "Miño", 2 to "Quintana", 22 to "Paz", 5 to "Ramírez",
            16 to "Watson", 8 to "Céliz", 36 to "Bracamonte", 11 to "Díaz", 10 to "Sánchez", 7 to "Alonso"
        )
        tn.contains("talleres") -> listOf(
            32 to "Unsain", 6 to "Riquelme", 4 to "Catalán", 13 to "Fascendini", 20 to "Schott",
            8 to "Fattori", 5 to "Galarza", 3 to "Maidana", 11 to "Depietri", 9 to "Álvarez", 37 to "Rick"
        )
        tn.contains("boca") -> listOf(
            1 to "Romero", 17 to "Advíncula", 2 to "Lema", 6 to "Rojo", 23 to "Blanco",
            8 to "Pol", 5 to "Miramón", 22 to "Zenón", 21 to "Medel", 10 to "Cavani", 16 to "Merentiel"
        )
        tn.contains("river") -> listOf(
            1 to "Armani", 16 to "Bustos", 6 to "Pezzella", 17 to "Díaz", 24 to "Acuña",
            5 to "Kranevitter", 8 to "Simón", 11 to "Meza", 10 to "Lanzini", 9 to "Borja", 19 to "Colidio"
        )
        tn.contains("racing") -> listOf(
            21 to "Arias", 30 to "Martirena", 2 to "Di Cesare", 13 to "García Basso", 27 to "Rojas",
            5 to "Nardoni", 15 to "Zuculini", 8 to "Quintero", 10 to "Salas", 9 to "Maravilla Martínez", 7 to "Carbonero"
        )
        tn.contains("independiente") -> listOf(
            33 to "Rey", 4 to "Loyola", 2 to "Lomónaco", 3 to "Lasorda", 23 to "Pérez",
            8 to "Marcone", 21 to "Loyola", 10 to "Montiel", 11 to "Mancuello", 9 to "Ávalos", 7 to "Tarzia"
        )
        tn.contains("san lorenzo") -> listOf(
            13 to "Gómez", 4 to "Tripichio", 2 to "Romaña", 6 to "Campi", 22 to "Báez",
            5 to "Irala", 8 to "Remedi", 10 to "Bustos", 18 to "Cerutti", 9 to "Cuello", 11 to "Reali"
        )
        else -> {
            val prefix = if (isHome) "L" else "V"
            listOf(
                1 to "Arquero $prefix",
                2 to "Defensor $prefix",
                3 to "Defensor $prefix",
                4 to "Defensor $prefix",
                6 to "Defensor $prefix",
                5 to "Mediocentro $prefix",
                8 to "Volante $prefix",
                10 to "Enganche $prefix",
                7 to "Extremo $prefix",
                9 to "Delantero $prefix",
                11 to "Punta $prefix"
            )
        }
    }
}

/**
 * Cancha de fútbol horizontal con los equipos enfrentados y líneas tácticas en espejo
 */
@Composable
fun HorizontalPitchTacticsView(
    match: Match,
    modifier: Modifier = Modifier
) {
    val (players, formations) = remember(match.id) { buildTacticalLineup(match) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A24)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Cabecera con formaciones tácticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00E676)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${match.homeTeam.name} (${formations.first})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "VS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${match.awayTeam.name} (${formations.second})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF81C784)))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Campo de juego Horizontal (Aspect ratio 16:9 / altura 230 dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Dibujo del campo en Canvas (césped a franjas y líneas de cal)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Franjas de césped
                    val stripes = 10
                    val stripeW = w / stripes
                    for (i in 0 until stripes) {
                        val c = if (i % 2 == 0) Color(0xFF1B4D20) else Color(0xFF163E1A)
                        drawRect(color = c, topLeft = Offset(i * stripeW, 0f), size = Size(stripeW, h))
                    }

                    val whiteLine = Color.White.copy(alpha = 0.45f)
                    val stroke = Stroke(width = 1.5.dp.toPx())

                    // Perímetro del campo
                    drawRect(color = whiteLine, topLeft = Offset(4.dp.toPx(), 4.dp.toPx()), size = Size(w - 8.dp.toPx(), h - 8.dp.toPx()), style = stroke)

                    // Línea del medio
                    drawLine(color = whiteLine, start = Offset(w / 2f, 4.dp.toPx()), end = Offset(w / 2f, h - 4.dp.toPx()), strokeWidth = stroke.width)

                    // Círculo central
                    drawCircle(color = whiteLine, radius = 32.dp.toPx(), center = Offset(w / 2f, h / 2f), style = stroke)
                    drawCircle(color = whiteLine, radius = 2.dp.toPx(), center = Offset(w / 2f, h / 2f))

                    // Área grande izquierda (Local)
                    val areaW = w * 0.16f
                    val areaH = h * 0.60f
                    val areaY = (h - areaH) / 2f
                    drawRect(color = whiteLine, topLeft = Offset(4.dp.toPx(), areaY), size = Size(areaW, areaH), style = stroke)

                    // Área grande derecha (Visitante)
                    drawRect(color = whiteLine, topLeft = Offset(w - 4.dp.toPx() - areaW, areaY), size = Size(areaW, areaH), style = stroke)

                    // Puntos de penal
                    drawCircle(color = whiteLine, radius = 2.dp.toPx(), center = Offset(w * 0.11f, h / 2f))
                    drawCircle(color = whiteLine, radius = 2.dp.toPx(), center = Offset(w * 0.89f, h / 2f))
                }

                // Renderizado de jugadores en sus coordenadas relativas
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val fieldW = maxWidth
                    val fieldH = maxHeight

                    players.forEach { p ->
                        val posX = (fieldW * p.xPercent) - 18.dp
                        val posY = (fieldH * p.yPercent) - 18.dp

                        Column(
                            modifier = Modifier
                                .offset(x = posX, y = posY)
                                .width(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Círculo / Dorsal del jugador
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (p.isHome) Color(0xFF00E676) else Color(0xFFFFD700))
                                    .border(1.dp, Color.Black.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.number.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }

                            // Nombre / Apellido del jugador
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = Color.Black.copy(alpha = 0.70f),
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                Text(
                                    text = shortenPlayerName(p.name),
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "⚡ Formaciones tácticas enfrentadas: Líneas ofensivas y defensivas en espejo",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
