import sys

def fix_sync_db(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    
    # 1. Fix the broken sed replace
    content = content.replace(
        'elif tournament_id == 5:\\n        league_path = " arg.1\\\n elif tournament_id == 9:\n        league_path = "arg.1"', 
        'elif tournament_id == 5:\n        league_path = "arg.1"'
    )
    
    # 2. Re-apply the correct logic for tid in [1, 3, 5] to [1, 3, 5, 9]
    content = content.replace('for tid in [1, 3, 5]:', 'for tid in [1, 3, 5, 9]:')
    
    # 3. Re-apply the correct logic for tournament_id == 9
    content = content.replace(
        'elif tournament_id == 5:\n        league_path = "arg.1"\n        dates_param = "20260101-20261231"',
        'elif tournament_id == 5:\n        league_path = "arg.1"\n        dates_param = "20260101-20261231"\n    elif tournament_id == 9:\n        league_path = "arg.2"\n        dates_param = "20260101-20261231"'
    )
    
    with open(filepath, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    fix_sync_db(sys.argv[1])
