package com.example.worldcup2026.data.model

data class Team(
    val id: Int,
    val name: String,
    val flagUrl: String?,
    val group: String,
    val tournament_id: Int? = null,
    val players: List<Player>? = emptyList()
)

data class Player(
    val id: Int,
    val name: String,
    val photoUrl: String,
    val age: Int,
    val position: String,
    val club: String
)

data class Group(
    val name: String,
    val teams: List<Team>
)

data class Match(
    val id: Int,
    val tournament_id: Int? = null,
    val matchday: Int? = null,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int?,
    val awayScore: Int?,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val date: String?,
    val status: String,
    val stadium: String? = "",
    val city: String? = "",
    // Campos para el Prode
    val predictedWinner: String? = null, // "L", "E", "V"
    val predictedHomeScore: Int? = null,
    val predictedAwayScore: Int? = null,
    val predictedHomePenalties: Int? = null,
    val predictedAwayPenalties: Int? = null,
    // Estadísticas VIP
    val homePossession: Int? = null,
    val awayPossession: Int? = null,
    val homeShots: Int? = null,
    val awayShots: Int? = null,
    val scorers: List<String> = emptyList(),
    val events: List<String> = emptyList(),
    val vipStats: String? = null,
    val clock: String? = null,
    val is_featured: Boolean = false
)


fun Match.getDisplayHomeScore(): Int? {
    val base = homeScore
    val eventGoals = countGoalsFromEvents(isHome = true)
    val otherEventGoals = countGoalsFromEvents(isHome = false)
    val hasClockOrLive = !clock.isNullOrBlank() || status.uppercase() in listOf("LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE", "1T", "2T", "ET", "PENALES", "FINISHED", "FINALIZADO", "FT")

    if (base != null) {
        return maxOf(base, eventGoals)
    }
    if (eventGoals > 0) {
        return eventGoals
    }
    if (otherEventGoals > 0 || hasClockOrLive) {
        return 0
    }
    return if (status.equals("Scheduled", ignoreCase = true)) null else 0
}

fun Match.getDisplayAwayScore(): Int? {
    val base = awayScore
    val eventGoals = countGoalsFromEvents(isHome = false)
    val otherEventGoals = countGoalsFromEvents(isHome = true)
    val hasClockOrLive = !clock.isNullOrBlank() || status.uppercase() in listOf("LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE", "1T", "2T", "ET", "PENALES", "FINISHED", "FINALIZADO", "FT")

    if (base != null) {
        return maxOf(base, eventGoals)
    }
    if (eventGoals > 0) {
        return eventGoals
    }
    if (otherEventGoals > 0 || hasClockOrLive) {
        return 0
    }
    return if (status.equals("Scheduled", ignoreCase = true)) null else 0
}

fun Match.countGoalsFromEvents(isHome: Boolean): Int {
    val targetTeam = if (isHome) homeTeam.name.lowercase().trim() else awayTeam.name.lowercase().trim()
    var count = 0
    if (scorers.isNotEmpty()) {
        scorers.forEach { s ->
            val sLow = s.lowercase()
            if ((sLow.contains("⚽") || sLow.contains("gol")) &&
                !sLow.contains("anulado") && !sLow.contains("disallowed") && !sLow.contains("var")
            ) {
                val colonIdx = s.indexOf(':')
                val teamPart = if (colonIdx != -1) s.substring(0, colonIdx).lowercase() else sLow
                if (teamPart.contains(targetTeam) || targetTeam.contains(teamPart.replace("⚽", "").trim())) {
                    count++
                }
            }
        }
        if (count > 0) return count
    }
    if (events.isNotEmpty()) {
        events.forEach { ev ->
            val evLow = ev.lowercase()
            if ((evLow.contains("⚽") || evLow.contains("gol")) &&
                !evLow.contains("anulado") && !evLow.contains("disallowed") && !evLow.contains("var") &&
                !evLow.contains("tanda de penales") && !evLow.contains("[penales]")
            ) {
                val colonIdx = ev.indexOf(':')
                val endBracket = ev.indexOf(']')
                val teamPart = if (endBracket != -1 && colonIdx != -1 && colonIdx > endBracket) {
                    ev.substring(endBracket + 1, colonIdx).lowercase().trim()
                } else if (colonIdx != -1) {
                    ev.substring(0, colonIdx).lowercase().trim()
                } else ""

                if (teamPart.isNotEmpty() && (teamPart.contains(targetTeam) || targetTeam.contains(teamPart))) {
                    count++
                }
            }
        }
    }
    return count
}

fun Match.isMatchLive(): Boolean {
    val rawStatus = status.uppercase().trim()
    val isCancelledOrPostponed = rawStatus.contains("POSTP") || rawStatus.contains("SUSPEND") || rawStatus.contains("CANC")
    if (isCancelledOrPostponed) return false

    val isFinished = rawStatus in listOf("FINISHED", "FINALIZADO", "FT", "FINAL", "TERMINADO")
    if (isFinished) return false

    val isLiveStatus = rawStatus in listOf("LIVE", "HALFTIME", "ENTREETIEMPO", "PAUSA", "PAUSE", "EN VIVO", "1T", "2T", "ET", "PENALES") ||
            rawStatus.contains("LIVE") || rawStatus.contains("VIVO")
    if (isLiveStatus) return true

    if (!clock.isNullOrBlank()) return true

    if (scorers.isNotEmpty() || events.isNotEmpty()) return true

    if (homeScore != null && awayScore != null) return true

    return try {
        val rawDate = date ?: ""
        if (rawDate.length >= 16) {
            val dt = if (rawDate.contains("T")) java.time.LocalDateTime.parse(rawDate.take(19))
                     else java.time.LocalDateTime.parse(rawDate.take(16), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            val now = java.time.LocalDateTime.now()
            dt.isBefore(now) && dt.plusMinutes(135).isAfter(now)
        } else false
    } catch (e: Exception) { false }
}
