import requests
import json
import os
import sys

# Traducción de nombres de selecciones de ESPN (inglés) a la base de datos de la app (español)
TEAM_TRANSLATION = {
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
}

# Lista estática de los 72 partidos de grupos (del ID 1 al 72) para poder asociarlos
# Estructura: (id, local, visitante)
FIXTURE_GROUPS = [
    (1, "México", "Sudáfrica"), (2, "Corea del Sur", "República Checa"), (3, "República Checa", "Sudáfrica"),
    (4, "México", "Corea del Sur"), (5, "República Checa", "México"), (6, "Sudáfrica", "Corea del Sur"),
    (7, "Canadá", "Bosnia"), (8, "Qatar", "Suiza"), (9, "Suiza", "Bosnia"),
    (10, "Canadá", "Qatar"), (11, "Suiza", "Canadá"), (12, "Bosnia", "Qatar"),
    (13, "Brasil", "Marruecos"), (14, "Haití", "Escocia"), (15, "Escocia", "Marruecos"),
    (16, "Brasil", "Haití"), (17, "Escocia", "Brasil"), (18, "Marruecos", "Haití"),
    (19, "Estados Unidos", "Paraguay"), (20, "Australia", "Turquía"), (21, "Turquía", "Paraguay"),
    (22, "Estados Unidos", "Australia"), (23, "Turquía", "Estados Unidos"), (24, "Paraguay", "Australia"),
    (25, "Alemania", "Curazao"), (26, "Costa de Marfil", "Ecuador"), (27, "Alemania", "Costa de Marfil"),
    (28, "Curazao", "Ecuador"), (29, "Ecuador", "Alemania"), (30, "Curazao", "Costa de Marfil"),
    (31, "Países Bajos", "Japón"), (32, "Suecia", "Túnez"), (33, "Japón", "Túnez"),
    (34, "Países Bajos", "Suecia"), (35, "Túnez", "Países Bajos"), (36, "Japón", "Suecia"),
    (37, "Bélgica", "Egipto"), (38, "Irán", "Nueva Zelanda"), (39, "Bélgica", "Irán"),
    (40, "Egipto", "Nueva Zelanda"), (41, "Nueva Zelanda", "Bélgica"), (42, "Egipto", "Irán"),
    (43, "España", "Cabo Verde"), (44, "Arabia Saudita", "Uruguay"), (45, "España", "Arabia Saudita"),
    (46, "Cabo Verde", "Uruguay"), (47, "Uruguay", "España"), (48, "Cabo Verde", "Arabia Saudita"),
    (49, "Francia", "Senegal"), (50, "Irak", "Noruega"), (51, "Francia", "Irak"),
    (52, "Noruega", "Senegal"), (53, "Noruega", "Francia"), (54, "Senegal", "Irak"),
    (55, "Austria", "Jordania"), (56, "Argentina", "Argelia"), (57, "Argentina", "Austria"),
    (58, "Jordania", "Argelia"), (59, "Jordania", "Argentina"), (60, "Argelia", "Austria"),
    (61, "Portugal", "RD Congo"), (62, "Uzbekistán", "Colombia"), (63, "Portugal", "Uzbekistán"),
    (64, "RD Congo", "Colombia"), (65, "Colombia", "Portugal"), (66, "RD Congo", "Uzbekistán"),
    (67, "Inglaterra", "Croacia"), (68, "Ghana", "Panamá"), (69, "Inglaterra", "Ghana"),
    (70, "Croacia", "Panamá"), (71, "Panamá", "Inglaterra"), (72, "Croacia", "Ghana")
]

def translate_team(name):
    clean_name = name.strip().lower()
    return TEAM_TRANSLATION.get(clean_name, name)

def find_match_id(home_translated, away_translated):
    # Intentar buscar coincidencia exacta en los grupos
    for match_id, local, visitante in FIXTURE_GROUPS:
        if (local == home_translated and visitante == away_translated) or \
           (local == away_translated and visitante == home_translated):
            return match_id
    return None

def fetch_match_details(event_id):
    url = f"https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/summary?event={event_id}"
    try:
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            return response.json()
    except Exception as e:
        print(f"Error cargando detalle del evento {event_id}: {e}")
    return None

def main():
    print("Iniciando actualizador de resultados de la Copa del Mundo 2026...")
    
    # Rango de fechas del mundial para buscar partidos (del 11 de junio al 19 de julio de 2026)
    # Para la automatización del día a día, con un rango móvil de ayer a mañana basta.
    # Pero para inicializar, podemos cargar desde el inicio (20260611) hasta el día actual.
    # Haremos una petición para traer todos los partidos jugados hasta hoy.
    import datetime
    today = datetime.date.today()
    start_date = "20260611"
    end_date = today.strftime("%Y%m%d")
    
    # URL del Scoreboard de ESPN para el rango
    url = f"https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/scoreboard?dates={start_date}-{end_date}"
    print(f"Consultando partidos de ESPN en el rango {start_date} al {end_date}...")
    
    try:
        response = requests.get(url, timeout=15)
        if response.status_code != 200:
            print(f"Error al consultar el Scoreboard de ESPN: {response.status_code}")
            sys.exit(1)
        data = response.json()
    except Exception as e:
        print(f"Error de red: {e}")
        sys.exit(1)
        
    events = data.get("events", [])
    print(f"Se encontraron {len(events)} partidos en el Scoreboard.")
    
    output_matches = []
    
    for event in events:
        event_id = event.get("id")
        name = event.get("name", "")
        status_name = event.get("status", {}).get("type", {}).get("name", "")
        
        # Equipos del partido
        competitions = event.get("competitions", [])
        if not competitions:
            continue
        competitors = competitions[0].get("competitors", [])
        if len(competitors) < 2:
            continue
            
        home_team = next((c for c in competitors if c.get("homeAway") == "home"), competitors[0])
        away_team = next((c for c in competitors if c.get("homeAway") == "away"), competitors[1])
        
        home_name_raw = home_team.get("team", {}).get("displayName", "")
        away_name_raw = away_team.get("team", {}).get("displayName", "")
        
        home_translated = translate_team(home_name_raw)
        away_translated = translate_team(away_name_raw)
        
        match_id = find_match_id(home_translated, away_translated)
        if not match_id:
            # Mapeo de la Gran Final (ID 131) y Tercer puesto (ID 132):
            if "final" in name.lower():
                if "third" in name.lower() or "tercer" in name.lower():
                    match_id = 132
                else:
                    match_id = 131
            else:
                print(f"Partido '{name}' ({event_id}) no corresponde a fase de grupos. Saltando...")
                continue
                
        print(f"Procesando Partido {match_id}: {home_translated} vs {away_translated} (ESPN ID: {event_id}). Estado: {status_name}")
        
        # Mapeamos estado de ESPN a la app
        # ESPN: STATUS_SCHEDULED (pre), STATUS_IN_PROGRESS (live), STATUS_FULL_TIME (Finished)
        app_status = "Scheduled"
        if status_name == "STATUS_FULL_TIME" or status_name == "STATUS_FINAL":
            app_status = "Finished"
        elif status_name == "STATUS_IN_PROGRESS" or "STATUS_HALFTIME" in status_name:
            app_status = "Live"
            
        # Puntuaciones
        home_score = home_team.get("score")
        away_score = away_team.get("score")
        
        # Si está programado y no ha empezado, no hay marcador
        if app_status == "Scheduled":
            home_score_val = None
            away_score_val = None
        else:
            home_score_val = int(home_score) if home_score is not None else 0
            away_score_val = int(away_score) if away_score is not None else 0
            
        # Por defecto
        home_possession = None
        away_possession = None
        home_shots = None
        away_shots = None
        home_fouls = None
        away_fouls = None
        home_corners = None
        away_corners = None
        home_saves = None
        away_saves = None
        home_yellow = None
        away_yellow = None
        home_red = None
        away_red = None
        home_passes = None
        away_passes = None
        scorers = []
        events_list = []
        
        # Si el partido ya empezó o terminó, cargamos los detalles (estadísticas y eventos)
        if app_status != "Scheduled":
            details = fetch_match_details(event_id)
            if details:
                # 1. Extraer Goles e Incidencias cronológicas (keyEvents)
                key_events = details.get("keyEvents", [])
                for ke in key_events:
                    ke_type = ke.get("type", {}).get("text", "")
                    ke_clock = ke.get("clock", {}).get("displayValue", "")
                    ke_team = ke.get("team", {}).get("displayName", "")
                    ke_text = ke.get("text", "")
                    
                    team_trans = translate_team(ke_team)
                    
                    # Formatear el texto de la incidencia para la app
                    emoji = "⚽"
                    if "goal" in ke_type.lower():
                        emoji = "⚽"
                        # Añadir a la lista de goleadores simples (para la tarjeta de partido)
                        scorers.append(f"⚽ {team_trans}: {ke_text.split('!')[-1].strip()} ({ke_clock})")
                    elif "yellow card" in ke_type.lower():
                        emoji = "🟨"
                    elif "red card" in ke_type.lower():
                        emoji = "🟥"
                    elif "substitution" in ke_type.lower():
                        emoji = "🔄"
                    else:
                        continue # Evitamos Kickoff u otros eventos genéricos
                        
                    events_list.append(f"{emoji} [{ke_clock}] {team_trans}: {ke_text}")
                    
                # 2. Extraer Estadísticas (boxscore)
                boxscore = details.get("boxscore", {})
                teams_stats = boxscore.get("teams", [])
                for ts in teams_stats:
                    ts_name = ts.get("team", {}).get("displayName", "")
                    ts_trans = translate_team(ts_name)
                    
                    # Buscamos las estadísticas que nos interesan
                    statistics = ts.get("statistics", [])
                    possession_pct = None
                    shots_on_target = None
                    fouls_committed = None
                    won_corners = None
                    saves = None
                    yellow_cards = None
                    red_cards = None
                    accurate_passes = None
                    total_passes = None
                    pass_pct = None
                    
                    for stat in statistics:
                        stat_name = stat.get("name")
                        stat_val = stat.get("displayValue")
                        if stat_name == "possessionPct":
                            possession_pct = int(float(stat_val))
                        elif stat_name == "shotsOnTarget":
                            shots_on_target = int(stat_val)
                        elif stat_name == "foulsCommitted":
                            fouls_committed = int(stat_val)
                        elif stat_name == "wonCorners":
                            won_corners = int(stat_val)
                        elif stat_name == "saves":
                            saves = int(stat_val)
                        elif stat_name == "yellowCards":
                            yellow_cards = int(stat_val)
                        elif stat_name == "redCards":
                            red_cards = int(stat_val)
                        elif stat_name == "accuratePasses":
                            accurate_passes = int(stat_val)
                        elif stat_name == "totalPasses":
                            total_passes = int(stat_val)
                        elif stat_name == "passPct":
                            pass_pct = stat_val
                            
                    passes_str = f"{accurate_passes}/{total_passes} ({pass_pct})" if accurate_passes is not None and total_passes is not None else None
                            
                    if ts_trans == home_translated:
                        home_possession = possession_pct
                        home_shots = shots_on_target
                        home_fouls = fouls_committed
                        home_corners = won_corners
                        home_saves = saves
                        home_yellow = yellow_cards
                        home_red = red_cards
                        home_passes = passes_str
                    elif ts_trans == away_translated:
                        away_possession = possession_pct
                        away_shots = shots_on_target
                        away_fouls = fouls_committed
                        away_corners = won_corners
                        away_saves = saves
                        away_yellow = yellow_cards
                        away_red = red_cards
                        away_passes = passes_str
                        
        # Armamos el partido procesado
        match_data = {
            "matchId": match_id,
            "homeScore": home_score_val,
            "awayScore": away_score_val,
            "status": app_status,
            "homePossession": home_possession,
            "awayPossession": away_possession,
            "homeShots": home_shots,
            "awayShots": away_shots,
            "homeFouls": home_fouls,
            "awayFouls": away_fouls,
            "homeCorners": home_corners,
            "awayCorners": away_corners,
            "homeSaves": home_saves,
            "awaySaves": away_saves,
            "homeYellowCards": home_yellow,
            "awayYellowCards": away_yellow,
            "homeRedCards": home_red,
            "awayRedCards": away_red,
            "homePasses": home_passes,
            "awayPasses": away_passes,
            "scorers": scorers,
            "events": events_list
        }
        output_matches.append(match_data)

    # Guardar en archivo JSON local
    output_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures_live.json")
    try:
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(output_matches, f, ensure_ascii=False, indent=2)
        print(f"Resultados actualizados guardados con éxito en: {output_path}")
    except Exception as e:
        print(f"Error escribiendo el archivo JSON: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
