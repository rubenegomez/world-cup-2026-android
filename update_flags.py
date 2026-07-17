import json
import sqlite3

with open('fixtures.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

conn = sqlite3.connect('worldcup.db')
c = conn.cursor()

updates = 0
for torneo in data.get('torneos', []):
    for p in torneo.get('partidos', []):
        for side in ['equipo_local', 'equipo_visitante']:
            if side in p:
                team = p[side]
                if 'bandera' in team and team['bandera'].startswith('http'):
                    c.execute('UPDATE teams SET flagUrl = ? WHERE name = ?', (team['bandera'], team['nombre']))
                    updates += c.rowcount

conn.commit()
conn.close()
print(f"Updated {updates} team flags from fixtures.json")
