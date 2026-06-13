# Forzar a PowerShell a usar UTF-8 para evitar caracteres corruptos
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Diccionario de traducciones de selecciones de ESPN a espaÃ±ol
$TEAM_TRANSLATION = @{
    "mexico" = "MÃ©xico"
    "south africa" = "SudÃ¡frica"
    "south korea" = "Corea del Sur"
    "czechia" = "RepÃºblica Checa"
    "canada" = "CanadÃ¡"
    "bosnia-herzegovina" = "Bosnia"
    "bosnia and herzegovina" = "Bosnia"
    "qatar" = "Qatar"
    "switzerland" = "Suiza"
    "brazil" = "Brasil"
    "morocco" = "Marruecos"
    "haiti" = "HaitÃ­"
    "scotland" = "Escocia"
    "united states" = "Estados Unidos"
    "usa" = "Estados Unidos"
    "paraguay" = "Paraguay"
    "australia" = "Australia"
    "turkey" = "TurquÃ­a"
    "tÃ¼rkiye" = "TurquÃ­a"
    "germany" = "Alemania"
    "curaÃ§ao" = "Curazao"
    "curacao" = "Curazao"
    "ivory coast" = "Costa de Marfil"
    "cote d'ivoire" = "Costa de Marfil"
    "ecuador" = "Ecuador"
    "netherlands" = "PaÃ­ses Bajos"
    "japan" = "JapÃ³n"
    "sweden" = "Suecia"
    "tunisia" = "TÃºnez"
    "belgium" = "BÃ©lgica"
    "egypt" = "Egipto"
    "iran" = "IrÃ¡n"
    "new zealand" = "Nueva Zelanda"
    "spain" = "EspaÃ±a"
    "cape verde" = "Cabo Verde"
    "saudi arabia" = "Arabia Saudita"
    "uruguay" = "Uruguay"
    "france" = "Francia"
    "senegal" = "Senegal"
    "iraq" = "Irak"
    "norway" = "Noruega"
    "argentina" = "Argentina"
    "algeria" = "Argelia"
    "austria" = "Austria"
    "jordan" = "Jordania"
    "portugal" = "Portugal"
    "dr congo" = "RD Congo"
    "congo dr" = "RD Congo"
    "uzbekistan" = "UzbekistÃ¡n"
    "colombia" = "Colombia"
    "england" = "Inglaterra"
    "croatia" = "Croacia"
    "ghana" = "Ghana"
    "panama" = "PanamÃ¡"
}

# Lista estÃ¡tica de los 72 partidos de grupos (del ID 1 al 72)
$FIXTURE_GROUPS = @(
    @{ id = 1; local = "MÃ©xico"; visitante = "SudÃ¡frica" }
    @{ id = 2; local = "Corea del Sur"; visitante = "RepÃºblica Checa" }
    @{ id = 3; local = "RepÃºblica Checa"; visitante = "SudÃ¡frica" }
    @{ id = 4; local = "MÃ©xico"; visitante = "Corea del Sur" }
    @{ id = 5; local = "RepÃºblica Checa"; visitante = "MÃ©xico" }
    @{ id = 6; local = "SudÃ¡frica"; visitante = "Corea del Sur" }
    @{ id = 7; local = "CanadÃ¡"; visitante = "Bosnia" }
    @{ id = 8; local = "Qatar"; visitante = "Suiza" }
    @{ id = 9; local = "Suiza"; visitante = "Bosnia" }
    @{ id = 10; local = "CanadÃ¡"; visitante = "Qatar" }
    @{ id = 11; local = "Suiza"; visitante = "CanadÃ¡" }
    @{ id = 12; local = "Bosnia"; visitante = "Qatar" }
    @{ id = 13; local = "Brasil"; visitante = "Marruecos" }
    @{ id = 14; local = "HaitÃ­"; visitante = "Escocia" }
    @{ id = 15; local = "Escocia"; visitante = "Marruecos" }
    @{ id = 16; local = "Brasil"; visitante = "HaitÃ­" }
    @{ id = 17; local = "Escocia"; visitante = "Brasil" }
    @{ id = 18; local = "Marruecos"; visitante = "HaitÃ­" }
    @{ id = 19; local = "Estados Unidos"; visitante = "Paraguay" }
    @{ id = 20; local = "Australia"; visitante = "TurquÃ­a" }
    @{ id = 21; local = "TurquÃ­a"; visitante = "Paraguay" }
    @{ id = 22; local = "Estados Unidos"; visitante = "Australia" }
    @{ id = 23; local = "TurquÃ­a"; visitante = "Estados Unidos" }
    @{ id = 24; local = "Paraguay"; visitante = "Australia" }
    @{ id = 25; local = "Alemania"; visitante = "Curazao" }
    @{ id = 26; local = "Costa de Marfil"; visitante = "Ecuador" }
    @{ id = 27; local = "Alemania"; visitante = "Costa de Marfil" }
    @{ id = 28; local = "Curazao"; visitante = "Ecuador" }
    @{ id = 29; local = "Ecuador"; visitante = "Alemania" }
    @{ id = 30; local = "Curazao"; visitante = "Costa de Marfil" }
    @{ id = 31; local = "PaÃ­ses Bajos"; visitante = "JapÃ³n" }
    @{ id = 32; local = "Suecia"; visitante = "TÃºnez" }
    @{ id = 33; local = "JapÃ³n"; visitante = "TÃºnez" }
    @{ id = 34; local = "PaÃ­ses Bajos"; visitante = "Suecia" }
    @{ id = 35; local = "TÃºnez"; visitante = "PaÃ­ses Bajos" }
    @{ id = 36; local = "JapÃ³n"; visitante = "Suecia" }
    @{ id = 37; local = "BÃ©lgica"; visitante = "Egipto" }
    @{ id = 38; local = "IrÃ¡n"; visitante = "Nueva Zelanda" }
    @{ id = 39; local = "BÃ©lgica"; visitante = "IrÃ¡n" }
    @{ id = 40; local = "Egipto"; visitante = "Nueva Zelanda" }
    @{ id = 41; local = "Nueva Zelanda"; visitante = "BÃ©lgica" }
    @{ id = 42; local = "Egipto"; visitante = "IrÃ¡n" }
    @{ id = 43; local = "EspaÃ±a"; visitante = "Cabo Verde" }
    @{ id = 44; local = "Arabia Saudita"; visitante = "Uruguay" }
    @{ id = 45; local = "EspaÃ±a"; visitante = "Arabia Saudita" }
    @{ id = 46; local = "Cabo Verde"; visitante = "Uruguay" }
    @{ id = 47; local = "Uruguay"; visitante = "EspaÃ±a" }
    @{ id = 48; local = "Cabo Verde"; visitante = "Arabia Saudita" }
    @{ id = 49; local = "Francia"; visitante = "Senegal" }
    @{ id = 50; local = "Irak"; visitante = "Noruega" }
    @{ id = 51; local = "Francia"; visitante = "Irak" }
    @{ id = 52; local = "Noruega"; visitante = "Senegal" }
    @{ id = 53; local = "Noruega"; visitante = "Francia" }
    @{ id = 54; local = "Senegal"; visitante = "Irak" }
    @{ id = 55; local = "Austria"; visitante = "Jordania" }
    @{ id = 56; local = "Argentina"; visitante = "Argelia" }
    @{ id = 57; local = "Argentina"; visitante = "Austria" }
    @{ id = 58; local = "Jordania"; visitante = "Argelia" }
    @{ id = 59; local = "Jordania"; visitante = "Argentina" }
    @{ id = 60; local = "Argelia"; visitante = "Austria" }
    @{ id = 61; local = "Portugal"; visitante = "RD Congo" }
    @{ id = 62; local = "UzbekistÃ¡n"; visitante = "Colombia" }
    @{ id = 63; local = "Portugal"; visitante = "UzbekistÃ¡n" }
    @{ id = 64; local = "RD Congo"; visitante = "Colombia" }
    @{ id = 65; local = "Colombia"; visitante = "Portugal" }
    @{ id = 66; local = "RD Congo"; visitante = "UzbekistÃ¡n" }
    @{ id = 67; local = "Inglaterra"; visitante = "Croacia" }
    @{ id = 68; local = "Ghana"; visitante = "PanamÃ¡" }
    @{ id = 69; local = "Inglaterra"; visitante = "Ghana" }
    @{ id = 70; local = "Croacia"; visitante = "PanamÃ¡" }
    @{ id = 71; local = "PanamÃ¡"; visitante = "Inglaterra" }
    @{ id = 72; local = "Croacia"; visitante = "Ghana" }
)

function Translate-Team($name) {
    if ($null -eq $name) { return "" }
    $clean = $name.Trim().ToLower()
    if ($TEAM_TRANSLATION.ContainsKey($clean)) {
        return $TEAM_TRANSLATION[$clean]
    }
    return $name
}

function Find-MatchId($localTrans, $visitTrans) {
    foreach ($m in $FIXTURE_GROUPS) {
        if (($m.local -eq $localTrans -and $m.visitante -eq $visitTrans) -or 
            ($m.local -eq $visitTrans -and $m.visitante -eq $localTrans)) {
            return $m.id
        }
    }
    return $null
}

Write-Host "Iniciando actualizador de resultados en PowerShell (Mundial 2026)..." -ForegroundColor Cyan

$startDate = "20260611"
$endDate = (Get-Date).ToString("yyyyMMdd")
$url = "https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/scoreboard?dates=$startDate-$endDate"

Write-Host "Consultando partidos de ESPN desde el $startDate al $endDate..."

try {
    $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 15
} catch {
    Write-Error "Fallo al conectar con la API de ESPN: $_"
    exit 1
}

$events = $response.events
Write-Host "Se encontraron $($events.Count) partidos." -ForegroundColor Green

$outputMatches = @()

foreach ($event in $events) {
    $eventId = $event.id
    $name = $event.name
    $statusName = $event.status.type.name
    
    $competitors = $event.competitions[0].competitors
    if ($competitors.Count -lt 2) { continue }
    
    $homeTeam = $competitors | Where-Object { $_.homeAway -eq "home" }
    $awayTeam = $competitors | Where-Object { $_.homeAway -eq "away" }
    
    if ($null -eq $homeTeam) { $homeTeam = $competitors[0] }
    if ($null -eq $awayTeam) { $awayTeam = $competitors[1] }
    
    $homeNameRaw = $homeTeam.team.displayName
    $awayNameRaw = $awayTeam.team.displayName
    
    $homeTrans = Translate-Team $homeNameRaw
    $awayTrans = Translate-Team $awayNameRaw
    
    $matchId = Find-MatchId $homeTrans $awayTrans
    
    if ($null -eq $matchId) {
        if ($name.ToLower().Contains("final")) {
            if ($name.ToLower().Contains("third") -or $name.ToLower().Contains("tercer")) {
                $matchId = 132
            } else {
                $matchId = 131
            }
        } else {
            Write-Host "Partido '$name' no corresponde a fase de grupos. Saltando..." -ForegroundColor Yellow
            continue
        }
    }
    
    Write-Host "Procesando Partido $matchId : $homeTrans vs $awayTrans (Estado: $statusName)" -ForegroundColor Gray
    
    $appStatus = "Scheduled"
    if ($statusName -eq "STATUS_FULL_TIME" -or $statusName -eq "STATUS_FINAL") {
        $appStatus = "Finished"
    } elseif ($statusName -eq "STATUS_IN_PROGRESS" -or $statusName.Contains("HALFTIME")) {
        $appStatus = "Live"
    }
    
    $homeScoreVal = $null
    $awayScoreVal = $null
    if ($appStatus -ne "Scheduled") {
        $homeScoreVal = [int]$homeTeam.score
        $awayScoreVal = [int]$awayTeam.score
    }
    
    $homePoss = $null
    $awayPoss = $null
    $homeShots = $null
    $awayShots = $null
    $scorers = @()
    $eventsList = @()
    
    if ($appStatus -ne "Scheduled") {
        $detailUrl = "https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/summary?event=$eventId"
        try {
            $details = Invoke-RestMethod -Uri $detailUrl -Method Get -TimeoutSec 10
            
            # 1. Incidencias y Goles
            if ($null -ne $details.keyEvents) {
                foreach ($ke in $details.keyEvents) {
                    $keType = $ke.type.text
                    $keClock = $ke.clock.displayValue
                    $keTeam = $ke.team.displayName
                    $keText = $ke.text
                    
                    $teamTrans = Translate-Team $keTeam
                    
                    $emoji = "âš½"
                    if ($keType.ToLower().Contains("goal")) {
                        $emoji = "âš½"
                        $cleanText = $keText
                        if ($keText.Contains("!")) {
                            $cleanText = $keText.Split("!")[-1].Trim()
                        }
                        $scorers += "âš½ ${teamTrans}: $cleanText ($keClock)"
                    } elseif ($keType.ToLower().Contains("yellow card")) {
                        $emoji = "ðŸŸ¨"
                    } elseif ($keType.ToLower().Contains("red card")) {
                        $emoji = "ðŸŸ¥"
                    } elseif ($keType.ToLower().Contains("substitution")) {
                        $emoji = "ðŸ”„"
                    } else {
                        continue
                    }
                    
                    $eventsList += "$emoji [$keClock] ${teamTrans}: $keText"
                }
            }
            
            # 2. EstadÃ­sticas Boxscore
            if ($null -ne $details.boxscore -and $null -ne $details.boxscore.teams) {
                foreach ($ts in $details.boxscore.teams) {
                    $tsName = $ts.team.displayName
                    $tsTrans = Translate-Team $tsName
                    
                    $possessionPct = $null
                    $shotsOnTarget = $null
                    
                    if ($null -ne $ts.statistics) {
                        foreach ($stat in $ts.statistics) {
                            if ($stat.name -eq "possessionPct") {
                                $possessionPct = [int][double]$stat.displayValue
                            } elseif ($stat.name -eq "shotsOnTarget") {
                                $shotsOnTarget = [int]$stat.displayValue
                            }
                        }
                    }
                    
                    if ($tsTrans -eq $homeTrans) {
                        $homePoss = $possessionPct
                        $homeShots = $shotsOnTarget
                    } elseif ($tsTrans -eq $awayTrans) {
                        $awayPoss = $possessionPct
                        $awayShots = $shotsOnTarget
                    }
                }
            }
        } catch {
            Write-Host "Error al descargar detalles para el evento $eventId" -ForegroundColor Red
        }
    }
    
    $matchData = @{
        matchId = $matchId
        homeScore = $homeScoreVal
        awayScore = $awayScoreVal
        status = $appStatus
        homePossession = $homePoss
        awayPossession = $awayPoss
        homeShots = $homeShots
        awayShots = $awayShots
        scorers = $scorers
        events = $eventsList
    }
    $outputMatches += $matchData
}

$outputPath = Join-Path $PSScriptRoot "..\fixtures_live.json"
$outputJson = ConvertTo-Json -InputObject $outputMatches -Depth 10
[System.IO.File]::WriteAllText($outputPath, $outputJson, [System.Text.Encoding]::UTF8)

Write-Host "Resultados de fÃºtbol actualizados con Ã©xito en: $outputPath" -ForegroundColor Green

