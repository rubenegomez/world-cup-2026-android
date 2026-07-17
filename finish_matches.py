import sqlite3
import datetime

conn = sqlite3.connect('worldcup.db')
c = conn.cursor()

c.execute("UPDATE matches SET status = 'Finished', clock = 'Finalizado' WHERE status = 'LIVE'")
print(f"Set {c.rowcount} matches from 'LIVE' to 'Finished'")

conn.commit()
conn.close()
