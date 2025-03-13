# Basketball Game Schedule and Odds API

This API provides access to basketball game schedules and associated betting odds.

## Endpoints

### Upcoming Schedule by Date

Retrieve a list of upcoming basketball games for a specific date.

#### Request Parameters
- `Date` - The date for which to retrieve the schedule

#### Response Structure
```json
[
  {
    "teamHome": "string",
    "teamAway": "string",
    "dateTime": "string",
    "location": "string",
    "homeSpread": "string",
    "awaySpread": "string",
    "homeML": "string",
    "awayML": "string",
    "neutral": "boolean"
  }
]
```

#### Response Fields
| Field | Description |
|-------|-------------|
| `teamHome` | The home team |
| `teamAway` | The away team |
| `dateTime` | Game start date and time |
| `location` | Game venue |
| `homeSpread` | Spread odds for the home team |
| `awaySpread` | Spread odds for the away team |
| `homeML` | Moneyline odds for the home team |
| `awayML` | Moneyline odds for the away team |
| `neutral` | Whether the game is at a neutral location |