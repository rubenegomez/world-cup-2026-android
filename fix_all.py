import sys
import re

def fix_files():
    # Fix sync_db.py
    with open('/home/ubuntu/Proyectos/world-cup-api/sync_db.py', 'r') as f:
        content = f.read()
    content = content.replace('for tid in [1, 3, 5]:', 'for tid in [1, 3, 5, 9]:')
    
    # regex replace for elif tournament_id == 5
    content = re.sub(
        r'elif tournament_id == 5:\s*league_path = "arg.1"\s*dates_param = "20260101-20261231"',
        'elif tournament_id == 5:\n        league_path = "arg.1"\n        dates_param = "20260101-20261231"\n    elif tournament_id == 9:\n        league_path = "arg.2"\n        dates_param = "20260101-20261231"',
        content
    )
    with open('/home/ubuntu/Proyectos/world-cup-api/sync_db.py', 'w') as f:
        f.write(content)
        
    # Fix scraper_logic.py
    with open('/home/ubuntu/Proyectos/world-cup-api/scraper_logic.py', 'r') as f:
        content = f.read()
    content = re.sub(
        r'elif tournament_id == 5:\s*league_path = "arg.1"',
        'elif tournament_id == 5:\n        league_path = "arg.1"\n    elif tournament_id == 9:\n        league_path = "arg.2"',
        content
    )
    with open('/home/ubuntu/Proyectos/world-cup-api/scraper_logic.py', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    fix_files()
