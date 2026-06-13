const fs = require('fs');
const path = require('path');

const TEAM_TRANSLATION = {
    "mexico": "México", "south africa": "Sudáfrica", "south korea": "Corea del Sur", "czechia": "República Checa",
    "canada": "Canadá", "bosnia-herzegovina": "Bosnia", "bosnia and herzegovina": "Bosnia", "qatar": "Qatar", "switzerland": "Suiza",
    "brazil": "Brasil", "morocco": "Marruecos", "haiti": "Haití", "scotland": "Escocia",
    "united states": "Estados Unidos", "usa": "Estados Unidos", "paraguay": "Paraguay", "australia": "Australia", "turkey": "Turquía", "türkiye": "Turquía",
    "germany": "Alemania", "curaçao": "Curazao", "curacao": "Curazao", "ivory coast": "Costa de Marfil", "cote d'ivoire": "Costa de Marfil", "ecuador": "Ecuador",
    "netherlands": "Países Bajos", "japan": "Japón", "sweden": "Suecia", "tunisia": "Túnez",
    "belgium": "Bélgica", "egypt": "Egipto", "iran": "Irán", "new zealand": "Nueva Zelanda",
    "spain": "España", "cape verde": "Cabo Verde", "saudi arabia": "Arabia Saudita", "uruguay": "Uruguay",
    "france": "Francia", "senegal": "Senegal", "iraq": "Irak", "norway": "Noruega",
    "argentina": "Argentina", "algeria": "Argelia", "austria": "Austria", "jordan": "Jordania",
    "portugal": "Portugal", "dr congo": "RD Congo", "congo dr": "RD Congo", "uzbekistan": "Uzbekistán", "colombia": "Colombia",
    "england": "Inglaterra", "croatia": "Croacia", "ghana": "Ghana", "panama": "Panamá"
};

const FIXTURE_GROUPS = [
    [1, "México", "Sudáfrica"], [2, "Corea del Sur", "República Checa"], [3, "República Checa", "Sudáfrica"],
    [4, "México", "Corea del Sur"], [5, "República Checa", "México"], [6, "Sudáfrica", "Corea del Sur"],
    [7, "Canadá", "Bosnia"], [8, "Qatar", "Suiza"], [9, "Suiza", "Bosnia"],
    [10, "Canadá", "Qatar"], [11, "Suiza", "Canadá"], [12, "Bosnia", "Qatar"],
    [13, "Brasil", "Marruecos"], [14, "Haití", "Escocia"], [15, "Escocia", "Marruecos"],
    [16, "Brasil", "Haití"], [17, "Escocia", "Brasil"], [18, "Marruecos", "Haití"],
    [19, "Estados Unidos", "Paraguay"], [20, "Australia", "Turquía"], [21, "Turquía", "Paraguay"],
    [22, "Estados Unidos", "Australia"], [23, "Turquía", "Estados Unidos"], [24, "Paraguay", "Australia"],
    [25, "Alemania", "Curazao"], [26, "Costa de Marfil", "Ecuador"], [27, "Alemania", "Costa de Marfil"],
    [28, "Curazao", "Ecuador"], [29, "Ecuador", "Alemania"], [30, "Curazao", "Costa de Marfil"],
    [31, "Países Bajos", "Japón"], [32, "Suecia", "Túnez"], [33, "Japón", "Túnez"],
    [34, "Países Bajos", "Suecia"], [35, "Túnez", "Países Bajos"], [36, "Japón", "Suecia"],
    [37, "Bélgica", "Egipto"], [38, "Irán", "Nueva Zelanda"], [39, "Bélgica", "Irán"],
    [40, "Egipto", "Nueva Zelanda"], [41, "Nueva Zelanda", "Bélgica"], [42, "Egipto", "Irán"],
    [43, "España", "Cabo Verde"], [44, "Arabia Saudita", "Uruguay"], [45, "España", "Arabia Saudita"],
    [46, "Cabo Verde", "Uruguay"], [47, "Uruguay", "España"], [48, "Cabo Verde", "Arabia Saudita"],
    [49, "Francia", "Senegal"], [50, "Irak", "Noruega"], [51, "Francia", "Irak"],
    [52, "Noruega", "Senegal"], [53, "Noruega", "Francia"], [54, "Senegal", "Irak"],
    [55, "Austria", "Jordania"], [56, "Argentina", "Argelia"], [57, "Argentina", "Austria"],
    [58, "Jordania", "Argelia"], [59, "Jordania", "Argentina"], [60, "Argelia", "Austria"],
    [61, "Portugal", "RD Congo"], [62, "Uzbekistán", "Colombia"], [63, "Portugal", "Uzbekistán"],
    [64, "RD Congo", "Colombia"], [65, "Colombia", "Portugal"], [66, "RD Congo", "Uzbekistán"],
    [67, "Inglaterra", "Croacia"], [68, "Ghana", "Panamá"], [69, "Inglaterra", "Ghana"],
    [70, "Croacia", "Panamá"], [71, "Panamá", "Inglaterra"], [72, "Croacia", "Ghana"]
];

function translateTeam(name) {
    if (!name) return "";
    const cleanName = name.trim().toLowerCase();
    return TEAM_TRANSLATION[cleanName] || name;
}

function findMatchId(homeTranslated, awayTranslated) {
    for (const [matchId, local, visitante] of FIXTURE_GROUPS) {
        if ((local === homeTranslated && visitante === awayTranslated) ||
            (local === awayTranslated && visitante === homeTranslated)) {
            return matchId;
        }
    }
    return null;
}

function cleanAndTranslateEvent(keType, keText, teamTrans, keClock) {
    const keTypeLower = keType.toLowerCase();
    
    if (keTypeLower.includes("goal")) {
        const emoji = "⚽";
        const playerMatch = keText.match(/(?:Goal!.*?\.\s*)?([^()]+)\s*\(([^()]+)\)/);
        if (playerMatch) {
            let playerName = playerMatch[1].trim();
            if (playerName.includes("Goal!")) {
                const parts = playerName.split(".");
                playerName = parts[parts.length - 1].trim();
            }
            
            const assistantMatch = keText.match(/[Aa]ssisted by ([^.]+)/);
            if (assistantMatch) {
                const assistantName = assistantMatch[1].trim();
                return `${emoji} [${keClock}] ${teamTrans}: ${playerName} (Asistencia: ${assistantName})`;
            } else {
                return `${emoji} [${keClock}] ${teamTrans}: ${playerName}`;
            }
        } else {
            return `${emoji} [${keClock}] ${teamTrans}: Gol`;
        }
    } else if (keTypeLower.includes("yellow card")) {
        const emoji = "🟨";
        const playerMatch = keText.match(/([^()]+)\s*\(([^()]+)\)\s*is shown the yellow card/);
        if (playerMatch) {
            const playerName = playerMatch[1].trim();
            return `${emoji} [${keClock}] ${teamTrans}: ${playerName}`;
        } else {
            return `${emoji} [${keClock}] ${teamTrans}: Tarjeta Amarilla`;
        }
    } else if (keTypeLower.includes("red card")) {
        const emoji = "🟥";
        const playerMatch = keText.match(/([^()]+)\s*\(([^()]+)\)\s*is shown the red card/);
        if (playerMatch) {
            const playerName = playerMatch[1].trim();
            return `${emoji} [${keClock}] ${teamTrans}: ${playerName}`;
        } else {
            return `${emoji} [${keClock}] ${teamTrans}: Tarjeta Roja`;
        }
    } else if (keTypeLower.includes("substitution")) {
        const emoji = "🔄";
        const subMatch = keText.match(/Substitution,\s*[^.]+\.\s*(.+?)\s+replaces\s+([^.]+?)(?:\s+because|\.|$)/);
        if (subMatch) {
            const playerIn = subMatch[1].trim();
            const playerOut = subMatch[2].trim();
            return `${emoji} [${keClock}] ${teamTrans}: 🟢 ${playerIn} 🔴 ${playerOut}`;
        } else {
            return `${emoji} [${keClock}] ${teamTrans}: Cambio`;
        }
    }
    return null;
}

function cleanScorerText(keText, teamTrans, keClock) {
    const emoji = "⚽";
    const playerMatch = keText.match(/(?:Goal!.*?\.\s*)?([^()]+)\s*\(([^()]+)\)/);
    if (playerMatch) {
        let playerName = playerMatch[1].trim();
        if (playerName.includes("Goal!")) {
            const parts = playerName.split(".");
            playerName = parts[parts.length - 1].trim();
        }
        
        const assistantMatch = keText.match(/[Aa]ssisted by ([^.]+)/);
        if (assistantMatch) {
            const assistantName = assistantMatch[1].trim();
            return `${emoji} ${teamTrans}: ${playerName} (Asistencia: ${assistantName}) (${keClock})`;
        } else {
            return `${emoji} ${teamTrans}: ${playerName} (${keClock})`;
        }
    }
    return `${emoji} ${teamTrans}: Gol (${keClock})`;
}

async function fetchMatchDetails(eventId) {
    const url = `https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/summary?event=${eventId}`;
    try {
        const response = await fetch(url, { signal: AbortSignal.timeout(10000) });
        if (response.ok) {
            return await response.json();
        }
    } catch (e) {
        console.error(`Error cargando detalle del evento ${eventId}:`, e.message);
    }
    return null;
}

async function main() {
    console.log("Iniciando actualizador de resultados de la Copa del Mundo 2026 (NodeJS)...");
    
    const start_date = "20260611";
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    const end_date = `${year}${month}${day}`;
    
    const url = `https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/scoreboard?dates=${start_date}-${end_date}`;
    console.log(`Consultando partidos de ESPN en el rango ${start_date} al ${end_date}...`);
    
    try {
        const response = await fetch(url, { signal: AbortSignal.timeout(15000) });
        if (!response.ok) {
            console.error(`Error al consultar el Scoreboard de ESPN: ${response.status}`);
            process.exit(1);
        }
        const data = await response.json();
        const events = data.events || [];
        console.log(`Se encontraron ${events.length} partidos en el Scoreboard.`);
        
        const outputMatches = [];
        
        for (const event of events) {
            const eventId = event.id;
            const name = event.name || "";
            const statusName = event.status?.type?.name || "";
            
            const competitions = event.competitions || [];
            if (competitions.length === 0) continue;
            const competitors = competitions[0].competitors || [];
            if (competitors.length < 2) continue;
            
            const homeTeam = competitors.find(c => c.homeAway === 'home') || competitors[0];
            const awayTeam = competitors.find(c => c.homeAway === 'away') || competitors[1];
            
            const homeNameRaw = homeTeam.team?.displayName || "";
            const awayNameRaw = awayTeam.team?.displayName || "";
            
            const homeTranslated = translateTeam(homeNameRaw);
            const awayTranslated = translateTeam(awayNameRaw);
            
            let matchId = findMatchId(homeTranslated, awayTranslated);
            if (!matchId) {
                if (name.toLowerCase().includes("final")) {
                    if (name.toLowerCase().includes("third") || name.toLowerCase().includes("tercer")) {
                        matchId = 132;
                    } else {
                        matchId = 131;
                    }
                } else {
                    console.log(`Partido '${name}' (${eventId}) no corresponde a fase de grupos. Saltando...`);
                    continue;
                }
            }
            
            console.log(`Procesando Partido ${matchId}: ${homeTranslated} vs ${awayTranslated} (ESPN ID: ${eventId}). Estado: ${statusName}`);
            
            let appStatus = "Scheduled";
            if (statusName === "STATUS_FULL_TIME" || statusName === "STATUS_FINAL") {
                appStatus = "Finished";
            } else if (statusName === "STATUS_IN_PROGRESS" || statusName.includes("STATUS_HALFTIME")) {
                appStatus = "Live";
            }
            
            const homeScore = homeTeam.score;
            const awayScore = awayTeam.score;
            
            let homeScoreVal = null;
            let awayScoreVal = null;
            
            if (appStatus !== "Scheduled") {
                homeScoreVal = homeScore !== undefined ? parseInt(homeScore) : 0;
                awayScoreVal = awayScore !== undefined ? parseInt(awayScore) : 0;
            }
            
            let homePossession = null;
            let awayPossession = null;
            let homeShots = null;
            let awayShots = null;
            let homeFouls = null;
            let awayFouls = null;
            let homeCorners = null;
            let awayCorners = null;
            let homeSaves = null;
            let awaySaves = null;
            let homeYellow = null;
            let awayYellow = null;
            let homeRed = null;
            let awayRed = null;
            let homePasses = null;
            let awayPasses = null;
            const scorers = [];
            const eventsList = [];
            
            if (appStatus !== "Scheduled") {
                const details = await fetchMatchDetails(eventId);
                if (details) {
                    const keyEvents = details.keyEvents || [];
                    for (const ke of keyEvents) {
                        const keType = ke.type?.text || "";
                        const keClock = ke.clock?.displayValue || "";
                        const keTeam = ke.team?.displayName || "";
                        const keText = ke.text || "";
                        
                        const teamTrans = translateTeam(keTeam);
                        
                        if (keType.toLowerCase().includes("goal")) {
                            const scorerClean = cleanScorerText(keText, teamTrans, keClock);
                            scorers.push(scorerClean);
                        }
                        
                        const translatedEvt = cleanAndTranslateEvent(keType, keText, teamTrans, keClock);
                        if (translatedEvt) {
                            eventsList.push(translatedEvt);
                        }
                    }
                    
                    const boxscore = details.boxscore || {};
                    const teamsStats = boxscore.teams || [];
                    for (const ts of teamsStats) {
                        const tsName = ts.team?.displayName || "";
                        const tsTrans = translateTeam(tsName);
                        
                        const statistics = ts.statistics || [];
                        let possessionPct = null;
                        let shotsOnTarget = null;
                        let foulsCommitted = null;
                        let wonCorners = null;
                        let savesVal = null;
                        let yellowCards = null;
                        let redCards = null;
                        let accuratePasses = null;
                        let totalPasses = null;
                        let passPct = null;
                        
                        for (const stat of statistics) {
                            const statName = stat.name;
                            const statVal = stat.displayValue;
                            if (statName === "possessionPct") {
                                possessionPct = parseInt(parseFloat(statVal));
                            } else if (statName === "shotsOnTarget") {
                                shotsOnTarget = parseInt(statVal);
                            } else if (statName === "foulsCommitted") {
                                foulsCommitted = parseInt(statVal);
                            } else if (statName === "wonCorners") {
                                wonCorners = parseInt(statVal);
                            } else if (statName === "saves") {
                                savesVal = parseInt(statVal);
                            } else if (statName === "yellowCards") {
                                yellowCards = parseInt(statVal);
                            } else if (statName === "redCards") {
                                redCards = parseInt(statVal);
                            } else if (statName === "accuratePasses") {
                                accuratePasses = parseInt(statVal);
                            } else if (statName === "totalPasses") {
                                totalPasses = parseInt(statVal);
                            } else if (statName === "passPct") {
                                passPct = statVal;
                            }
                        }
                        
                        const passesStr = (accuratePasses !== null && totalPasses !== null) ? `${accuratePasses}/${totalPasses} (${passPct})` : null;
                        
                        if (tsTrans === homeTranslated) {
                            homePossession = possessionPct;
                            homeShots = shotsOnTarget;
                            homeFouls = foulsCommitted;
                            homeCorners = wonCorners;
                            homeSaves = savesVal;
                            homeYellow = yellowCards;
                            homeRed = redCards;
                            homePasses = passesStr;
                        } else if (tsTrans === awayTranslated) {
                            awayPossession = possessionPct;
                            awayShots = shotsOnTarget;
                            awayFouls = foulsCommitted;
                            awayCorners = wonCorners;
                            awaySaves = savesVal;
                            awayYellow = yellowCards;
                            awayRed = redCards;
                            awayPasses = passesStr;
                        }
                    }
                }
            }
            
            const matchData = {
                matchId,
                homeScore: homeScoreVal,
                awayScore: awayScoreVal,
                status: appStatus,
                homePossession,
                awayPossession,
                homeShots,
                awayShots,
                homeFouls,
                awayFouls,
                homeCorners,
                awayCorners,
                homeSaves,
                awaySaves,
                homeYellowCards: homeYellow,
                awayYellowCards: awayYellow,
                homeRedCards: homeRed,
                awayRedCards: awayRed,
                homePasses,
                awayPasses,
                scorers,
                events: eventsList
            };
            outputMatches.push(matchData);
        }
        
        const outputPath = path.join(__dirname, "..", "fixtures_live.json");
        fs.writeFileSync(outputPath, JSON.stringify(outputMatches, null, 2), 'utf-8');
        console.log(`Resultados actualizados guardados con éxito en: ${outputPath}`);
        
    } catch (e) {
        console.error("Error de red o procesamiento:", e.message);
        process.exit(1);
    }
}

main();
