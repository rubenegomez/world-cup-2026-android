# World Cup 2026 API - Contratos JSON

Este documento define la estructura de la API que servirá el nuevo backend en Python (FastAPI) a la aplicación Android. Estos esquemas unifican los datos basados en los modelos definidos en `Models.kt` y `NetworkModule.kt` (como `LiveMatchDto`), de manera que el Frontend consumirá de estos endpoints y ya no requerirá hacer scraping de manera local.

## Base URL (Ejemplo)
`https://api.worldcup2026.app/v1`

---

## Endpoints y Esquemas

### 1. `GET /api/teams`
Obtiene la lista de selecciones. Puede admitir query params (ej. `?groupId=A`) para filtrar.

**Response Schema:**
```json
[
  {
    "id": 1,
    "name": "Argentina",
    "flagUrl": "https://url.com/flags/ar.png",
    "group": "A",
    "players": [
      {
        "id": 10,
        "name": "Lionel Messi",
        "photoUrl": "https://url.com/photos/messi.png",
        "age": 38,
        "position": "FW",
        "club": "Inter Miami"
      }
    ]
  }
]
```

### 2. `GET /api/groups`
Obtiene la lista de grupos con sus respectivos equipos. 

**Response Schema:**
```json
[
  {
    "name": "Group A",
    "teams": [
      {
        "id": 1,
        "name": "Argentina",
        "flagUrl": "https://url.com/flags/ar.png",
        "group": "A"
      }
      // ... otros equipos
    ]
  }
]
```

### 3. `GET /api/matches`
Obtiene la lista completa de partidos del torneo (fixture).

**Response Schema:**
```json
[
  {
    "id": 1,
    "homeTeam": {
      "id": 1,
      "name": "Argentina",
      "flagUrl": "https://url.com/flags/ar.png",
      "group": "A"
    },
    "awayTeam": {
      "id": 2,
      "name": "Canada",
      "flagUrl": "https://url.com/flags/ca.png",
      "group": "A"
    },
    "homeScore": 2,
    "awayScore": 0,
    "homePenalties": null,
    "awayPenalties": null,
    "date": "2026-06-11T16:00:00Z",
    "status": "FINISHED",
    "stadium": "MetLife Stadium",
    "city": "New York",
    "homePossession": 65,
    "awayPossession": 35,
    "homeShots": 15,
    "awayShots": 5,
    "scorers": ["Messi 45'", "Alvarez 80'"],
    "events": ["Yellow Card - Romero 30'"],
    "vipStats": "Man of the match: Messi",
    "clock": "90'"
  }
]
```

### 4. `GET /api/matches/live`
Endpoint de alto rendimiento, preferiblemente cacheado o vía WebSockets en un futuro, que devuelve el estado actualizado de los partidos en curso (reemplaza a `fixtures_live.json` mapeando a la clase `LiveMatchDto`).

**Response Schema:**
```json
[
  {
    "matchId": 1,
    "homeScore": 2,
    "awayScore": 0,
    "status": "LIVE",
    "homePossession": 65,
    "awayPossession": 35,
    "homeShots": 15,
    "awayShots": 5,
    "homeFouls": 10,
    "awayFouls": 12,
    "homeCorners": 5,
    "awayCorners": 2,
    "homeSaves": 2,
    "awaySaves": 5,
    "homeYellowCards": 1,
    "awayYellowCards": 2,
    "homeRedCards": 0,
    "awayRedCards": 0,
    "homePasses": "500",
    "awayPasses": "250",
    "scorers": ["Messi 45'"],
    "events": ["Goal - Messi 45'"],
    "clock": "45'"
  }
]
```
