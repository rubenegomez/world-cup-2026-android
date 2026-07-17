import sqlite3
conn = sqlite3.connect('worldcup.db')
conn.execute('UPDATE matches SET homeScore=0, awayScore=2 WHERE id=101')
conn.commit()
