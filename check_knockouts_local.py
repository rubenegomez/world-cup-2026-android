from database import SessionLocal
import models

db = SessionLocal()
matches = db.query(models.Match).filter(models.Match.id >= 101).filter(models.Match.id <= 116).all()
for m in matches:
    t1 = db.query(models.Team).get(m.homeTeamId).name if m.homeTeamId else "TBD"
    t2 = db.query(models.Team).get(m.awayTeamId).name if m.awayTeamId else "TBD"
    print(f"Match {m.id}: {t1} vs {t2}")
db.close()
