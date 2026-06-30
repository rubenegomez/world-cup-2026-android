import sys

with open("/app/knockout_updater.py", "r", encoding="utf-8") as f:
    lines = f.readlines()

start_index = -1
for i, line in enumerate(lines):
    if "# 3. Actualizar Dieciseisavos (101-116)" in line:
        start_index = i
        break

if start_index == -1:
    print("Could not find start index")
    sys.exit(1)

new_code = """    # 3. Actualizar Dieciseisavos (101-116) según fixture oficial FIFA
    # Asignación de los mejores 8 terceros de forma ordenada a los 8 cupos disponibles
    t3 = best_thirds.copy()
    def get_t3():
        return t3.pop(0) if t3 else None

    update_match_teams(101, get_team_at("A", 1), get_team_at("B", 1)) # P73: 2A vs 2B
    update_match_teams(102, get_team_at("E", 0), get_t3())            # P74: 1E vs 3er
    update_match_teams(103, get_team_at("F", 0), get_team_at("C", 1)) # P75: 1F vs 2C
    update_match_teams(104, get_team_at("C", 0), get_team_at("F", 1)) # P76: 1C vs 2F
    update_match_teams(105, get_team_at("I", 0), get_t3())            # P77: 1I vs 3er
    update_match_teams(106, get_team_at("E", 1), get_team_at("I", 1)) # P78: 2E vs 2I
    update_match_teams(107, get_team_at("A", 0), get_t3())            # P79: 1A vs 3er
    update_match_teams(108, get_team_at("L", 0), get_t3())            # P80: 1L vs 3er
    update_match_teams(109, get_team_at("D", 0), get_t3())            # P81: 1D vs 3er
    update_match_teams(110, get_team_at("G", 0), get_t3())            # P82: 1G vs 3er
    update_match_teams(111, get_team_at("K", 1), get_team_at("L", 1)) # P83: 2K vs 2L
    update_match_teams(112, get_team_at("H", 0), get_team_at("J", 1)) # P84: 1H vs 2J
    update_match_teams(113, get_team_at("B", 0), get_t3())            # P85: 1B vs 3er
    update_match_teams(114, get_team_at("J", 0), get_team_at("H", 1)) # P86: 1J vs 2H
    update_match_teams(115, get_team_at("K", 0), get_t3())            # P87: 1K vs 3er
    update_match_teams(116, get_team_at("D", 1), get_team_at("G", 1)) # P88: 2D vs 2G

    # 4. Actualizar fases posteriores a partir del ganador de los partidos eliminatorios
    def get_winner(m_id):
        m = db.query(models.Match).filter(models.Match.id == m_id).first()
        if m and m.status == "Finished":
            h = m.homeScore or 0
            a = m.awayScore or 0
            if h > a:
                return db.query(models.Team).filter(models.Team.id == m.homeTeamId).first()
            elif a > h:
                return db.query(models.Team).filter(models.Team.id == m.awayTeamId).first()
            else:
                hp = m.homePenalties or 0
                ap = m.awayPenalties or 0
                if hp > ap:
                    return db.query(models.Team).filter(models.Team.id == m.homeTeamId).first()
                elif ap > hp:
                    return db.query(models.Team).filter(models.Team.id == m.awayTeamId).first()
                else:
                    return None
        return None

    def get_loser(m_id):
        m = db.query(models.Match).filter(models.Match.id == m_id).first()
        if m and m.status == "Finished":
            h = m.homeScore or 0
            a = m.awayScore or 0
            if h < a:
                return db.query(models.Team).filter(models.Team.id == m.homeTeamId).first()
            elif a < h:
                return db.query(models.Team).filter(models.Team.id == m.awayTeamId).first()
            else:
                hp = m.homePenalties or 0
                ap = m.awayPenalties or 0
                if hp < ap:
                    return db.query(models.Team).filter(models.Team.id == m.homeTeamId).first()
                elif ap < hp:
                    return db.query(models.Team).filter(models.Team.id == m.awayTeamId).first()
                else:
                    return None
        return None

    # Octavos (117-124) según cascada FIFA oficial
    update_match_teams(117, get_winner(102), get_winner(105))  # FIFA P89: G(P74) vs G(P77)
    update_match_teams(118, get_winner(101), get_winner(103))  # FIFA P90: G(P73) vs G(P75)
    update_match_teams(119, get_winner(104), get_winner(106))  # FIFA P91: G(P76) vs G(P78)
    update_match_teams(120, get_winner(107), get_winner(108))  # FIFA P92: G(P79) vs G(P80)
    update_match_teams(121, get_winner(111), get_winner(112))  # FIFA P93: G(P83) vs G(P84)
    update_match_teams(122, get_winner(109), get_winner(110))  # FIFA P94: G(P81) vs G(P82)
    update_match_teams(123, get_winner(114), get_winner(116))  # FIFA P95: G(P86) vs G(P88)
    update_match_teams(124, get_winner(113), get_winner(115))  # FIFA P96: G(P85) vs G(P87)

    # Cuartos (125-128) según FIFA oficial
    update_match_teams(125, get_winner(117), get_winner(118))  # FIFA P97: G(P89) vs G(P90)
    update_match_teams(126, get_winner(121), get_winner(122))  # FIFA P98: G(P93) vs G(P94)
    update_match_teams(127, get_winner(119), get_winner(120))  # FIFA P99: G(P91) vs G(P92)
    update_match_teams(128, get_winner(123), get_winner(124))  # FIFA P100: G(P95) vs G(P96)

    # Semifinales (129-130)
    update_match_teams(129, get_winner(125), get_winner(126))  # FIFA P101: G(P97) vs G(P98)
    update_match_teams(130, get_winner(127), get_winner(128))  # FIFA P102: G(P99) vs G(P100)

    # Final (131) y Tercer puesto (132)
    update_match_teams(131, get_winner(129), get_winner(130))  # FIFA P104: G(P101) vs G(P102)
    update_match_teams(132, get_loser(129), get_loser(130))    # FIFA P103: L(P101) vs L(P102)

"""

lines = lines[:start_index]
lines.append(new_code)

with open("/app/knockout_updater.py", "w", encoding="utf-8") as f:
    f.writelines(lines)

print("knockout_updater.py rewritten successfully!")
