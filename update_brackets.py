from database import SessionLocal
import knockout_updater

try:
    print("Connecting to DB...")
    db = SessionLocal()
    print("Updating knockout brackets...")
    knockout_updater.update_knockout_brackets(db)
    print("Brackets updated!")
except Exception as e:
    print(f"Error: {e}")
finally:
    db.close()
