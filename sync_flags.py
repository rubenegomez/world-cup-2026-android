import sqlite3

conn = sqlite3.connect('worldcup.db')
c = conn.cursor()

# Find all espncdn flags
c.execute("SELECT name, flagUrl FROM teams WHERE flagUrl LIKE '%espncdn%'")
good_flags = {}
for row in c.fetchall():
    name, flag = row
    if name not in good_flags:
        good_flags[name] = flag

updates = 0
for name, flag in good_flags.items():
    # Update those that don't have espncdn
    c.execute("UPDATE teams SET flagUrl = ? WHERE name = ? AND (flagUrl NOT LIKE '%espncdn%' OR flagUrl IS NULL OR flagUrl = '')", (flag, name))
    updates += c.rowcount

conn.commit()
conn.close()
print(f"Synced {updates} team flags from espncdn across duplicates")
