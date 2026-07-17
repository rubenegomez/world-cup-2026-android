import json
import logging
from database import SessionLocal
import models

logger = logging.getLogger('add_tournaments')
logging.basicConfig(level=logging.INFO)

db = SessionLocal()
try:
    with open('fixtures.json', 'r', encoding='utf-8') as f:
        data = json.load(f)

    tournaments = data.get('torneos', [])
    
    id_map = {
        'world_cup_2026': 1,
        'liga_profesional_2026': 5,
        'copa_sudamericana_2026': 4,
        'primera_nacional_2026': 8,
        'copa_argentina_2026': 6,
        'primera_b_metropolitana_2026': 9,
        'primera_c_2026': 10
    }

    teams_cache = {}
    
    for t_data in tournaments:
        mapped_id = id_map.get(t_data['id'], None)
        if mapped_id not in [6, 8]:
            continue
            
        t_type = 'Nacional'
        
        t_obj = db.query(models.Tournament).filter(models.Tournament.id == mapped_id).first()
        if not t_obj:
            t_obj = models.Tournament(
                id=mapped_id,
                name=t_data['nombre'],
                type=t_type,
                order=mapped_id
            )
            db.add(t_obj)
            db.commit()
            logger.info(f'Added tournament {t_obj.name}')
            
        for match_data in t_data.get('partidos', []):
            home_data = match_data.get('equipo_local', {})
            away_data = match_data.get('equipo_visitante', {})
            
            home_name = home_data.get('nombre')
            away_name = away_data.get('nombre')
            
            home_flag = home_data.get('bandera')
            away_flag = away_data.get('bandera')
            
            if home_name not in teams_cache:
                team = db.query(models.Team).filter(models.Team.name == home_name, models.Team.tournament_id == mapped_id).first()
                if not team:
                    team = models.Team(name=home_name, flagUrl=home_flag, tournament_id=mapped_id, group=match_data.get('fase', 'Fase Regular'))
                    db.add(team)
                    db.commit()
                teams_cache[home_name] = team.id
                
            if away_name not in teams_cache:
                team = db.query(models.Team).filter(models.Team.name == away_name, models.Team.tournament_id == mapped_id).first()
                if not team:
                    team = models.Team(name=away_name, flagUrl=away_flag, tournament_id=mapped_id, group=match_data.get('fase', 'Fase Regular'))
                    db.add(team)
                    db.commit()
                teams_cache[away_name] = team.id
                
            home_id = teams_cache[home_name]
            away_id = teams_cache[away_name]
            
            m_obj = db.query(models.Match).filter(models.Match.homeTeamId == home_id, models.Match.awayTeamId == away_id, models.Match.tournament_id == mapped_id).first()
            if not m_obj:
                m_obj = models.Match(
                    tournament_id=mapped_id,
                    homeTeamId=home_id,
                    awayTeamId=away_id,
                    date=f"{match_data.get('fecha', '')} {match_data.get('hora_art', '00:00')}".strip(),
                    status='Scheduled',
                    stadium=match_data.get('estadio')
                )
                db.add(m_obj)
        db.commit()
        logger.info(f'Finished loading matches for {t_data["nombre"]}')
        
except Exception as e:
    logger.error(e)
finally:
    db.close()
