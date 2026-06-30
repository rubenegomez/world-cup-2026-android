import json
from database import SessionLocal
import models
import knockout_updater

def load_fixtures():
    db = SessionLocal()
    try:
        with open("fixtures_live.json", "r", encoding="utf-8") as f:
            data = json.load(f)
        
        for item in data:
            m_id = item.get("matchId")
            if m_id and m_id <= 72:
                match_record = db.query(models.Match).filter_by(id=m_id).first()
                if match_record:
                    match_record.homeScore = item.get("homeScore")
                    match_record.awayScore = item.get("awayScore")
                    match_record.status = item.get("status")
                    print(f"Updated match {m_id}: {match_record.homeScore} - {match_record.awayScore}")
        
        db.commit()
        print("Commited group stage scores.")
        
        # Now update knockouts based on the new group stage scores
        print("Updating knockouts based on live standings...")
        knockout_updater.update_knockout_brackets(db)
        print("Knockouts updated.")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        db.close()

if __name__ == "__main__":
    load_fixtures()
