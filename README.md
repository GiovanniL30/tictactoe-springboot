# Tic-Tac-Toe API Routes

All routes are relative to `http://localhost:8080`. Requests and responses use JSON unless a route has no request body.

## Route summary

| Method | Route | Use case |
|---|---|---|
| `GET` | `/api/v1/rooms` | List rooms and their game summaries |
| `POST` | `/api/v1/rooms` | Create a room and its first game round |
| `POST` | `/api/v1/rooms/{roomCode}/join` | Join a room as player O or as a spectator |
| `GET` | `/api/v1/rooms/{roomCode}` | Get the games belonging to one room |
| `POST` | `/api/v1/rooms/{roomCode}/play-again` | Start another round in the room |
| `DELETE` | `/api/v1/rooms/{roomCode}` | Delete the room and its game data |
| `GET` | `/api/v1/games/{gameId}` | Get full information for one game |
| `GET` | `/api/v1/games/{gameId}/board` | Get a game's board |
| `GET` | `/api/v1/games/{gameId}/moves` | Get a game's moves and result |
| `POST` | `/api/v1/games/{gameId}/move` | Place a move in the active game |
| `GET` | `/api/v1/players` | List known players |
| `GET` | `/api/v1/players/{playerName}/games` | Get one player's game history |

## API conventions

- A `roomCode` identifies a room across all its rounds.
- A `gameId` is a UUID identifying one specific round. Starting another round creates a new `gameId`.
- Valid symbols are `X` and `O`.
- Board coordinates are zero-based: `x` is the row and `y` is the column. Both must be from `0` through `2`.
- Game statuses are `WAITING_FOR_PLAYERS`, `IN_PROGRESS`, and `COMPLETED`.
- The room creator is player X. The second unique player is player O. Later players join as spectators.
- Player-name comparison is case-insensitive and ignores leading and trailing spaces.
- A winning player's score increases by one. A completed draw has `winner: "DRAW"`; otherwise `winner` is the winning player's display name.
- Empty board positions and unavailable values such as `currentTurn`, `winner`, and a spectator's `symbol` are returned as `null`.

## Room routes

### Create a room

`POST /api/v1/rooms`

Creates a room, round 1, and player X. The game initially waits for a second player.

Request body:

```json
{
  "playerName": "Gio"
}
```

`playerName` is required and cannot be blank.

Success: `201 Created`

```json
{
  "message": "Game created successfully.",
  "roomCode": "H9LL",
  "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
  "player": {
    "playerName": "Gio",
    "score": 0,
    "symbol": "X",
    "type": "PLAYER"
  }
}
```

### Join a room

`POST /api/v1/rooms/{roomCode}/join`

Adds the second player as player O and changes the game to `IN_PROGRESS`. If X and O already exist, the player joins as a spectator instead.

Request body:

```json
{
  "playerName": "Vanni"
}
```

`playerName` is required, cannot be blank, and must be unique within the room.

Success for player O: `200 OK`

```json
{
  "message": "Player joined successfully.",
  "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
  "player": {
    "playerName": "Vanni",
    "score": 0,
    "symbol": "O",
    "type": "PLAYER"
  }
}
```

Success for a spectator: `200 OK`

```json
{
  "message": "Game already has two players. Joined as spectator.",
  "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
  "player": {
    "playerName": "Observer",
    "score": 0,
    "symbol": null,
    "type": "SPECTATOR"
  }
}
```

### List rooms

`GET /api/v1/rooms`

Returns every room with a summary of its games.

Request body: none.

Success: `200 OK`

```json
{
  "totalRooms": 1,
  "totalGames": 2,
  "rooms": [
    {
      "roomCode": "H9LL",
      "games": [
        {
          "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
          "status": "COMPLETED",
          "winner": "Gio"
        },
        {
          "gameId": "7291b377-d29d-4d69-82b0-05d20a851b3d",
          "status": "IN_PROGRESS"
        }
      ]
    }
  ]
}
```

### Get room information

`GET /api/v1/rooms/{roomCode}`

Returns only the room code and the ID, status, and winner of each game in the room.

Request body: none.

Success: `200 OK`

```json
{
  "roomCode": "H9LL",
  "games": [
    {
      "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
      "status": "COMPLETED",
      "winner": "Gio"
    },
    {
      "gameId": "7291b377-d29d-4d69-82b0-05d20a851b3d",
      "status": "IN_PROGRESS"
    }
  ]
}
```

### Start another round

`POST /api/v1/rooms/{roomCode}/play-again`

Completes the previous round if needed and starts the next round with an empty board. Both players must already be present. Scores and spectators remain attached to the room.

Request body: none.

Success: `200 OK`

```json
{
  "message": "New round started.",
  "roomCode": "H9LL",
  "gameId": "7291b377-d29d-4d69-82b0-05d20a851b3d",
  "currentRound": 2,
  "currentTurn": "X"
}
```

Use the new `gameId` when placing moves in the new round.

### Delete a room

`DELETE /api/v1/rooms/{roomCode}`

Deletes the room, all its games and moves, its players, and the associated player-game history. The response contains the room summary captured immediately before deletion.

Request body: none.

Success: `200 OK`

```json
{
  "roomCode": "H9LL",
  "games": [
    {
      "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
      "status": "COMPLETED",
      "winner": "Gio"
    },
    {
      "gameId": "7291b377-d29d-4d69-82b0-05d20a851b3d",
      "status": "IN_PROGRESS"
    }
  ]
}
```

## Game routes

### Get game information

`GET /api/v1/games/{gameId}`

Returns the selected game's players, scores, board state metadata, status, and winner.

Request body: none.

Success: `200 OK`

```json
{
  "players": [
    {
      "playerName": "Gio",
      "score": 1,
      "symbol": "X",
      "type": "PLAYER"
    },
    {
      "playerName": "Vanni",
      "score": 0,
      "symbol": "O",
      "type": "PLAYER"
    }
  ],
  "roomCode": "H9LL",
  "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
  "round": 1,
  "currentTurn": null,
  "spectatorCount": 1,
  "status": "COMPLETED",
  "winner": "Gio",
  "message": "Game information retrieved successfully."
}
```

### Get the board

`GET /api/v1/games/{gameId}/board`

Returns the board, turn, and status for the selected game.

Request body: none.

Success: `200 OK`

```json
{
  "message": "Latest Board Grid",
  "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
  "grid": [
    ["X", "X", "X"],
    ["O", "O", null],
    [null, null, null]
  ],
  "currentTurn": null,
  "status": "COMPLETED"
}
```

### Place a move

`POST /api/v1/games/{gameId}/move`

Places the current player's symbol at a board coordinate. The route accepts only the room's active `gameId`.

Request body:

```json
{
  "x": 0,
  "y": 1,
  "symbol": "X"
}
```

All fields are required. `x` and `y` must each be between `0` and `2`.

Success: `200 OK`

```json
{
  "message": "Move placed successfully.",
  "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
  "grid": [
    [null, "X", null],
    [null, null, null],
    [null, null, null]
  ],
  "currentTurn": "O",
  "status": "IN_PROGRESS"
}
```

When the move wins the round, `status` becomes `COMPLETED` and `currentTurn` becomes `null`.

### Get a game's moves

`GET /api/v1/games/{gameId}/moves`

Returns all moves for one round in move-number order, followed by the round's current result.

Request body: none.

Success: `200 OK`

```json
{
  "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
  "roomCode": "H9LL",
  "round": 1,
  "moves": [
    {
      "moveNumber": 1,
      "playerName": "Gio",
      "symbol": "X",
      "x": 0,
      "y": 0,
      "playedAt": "2026-09-16T01:30:00Z"
    },
    {
      "moveNumber": 2,
      "playerName": "Vanni",
      "symbol": "O",
      "x": 1,
      "y": 0,
      "playedAt": "2026-09-16T01:30:05Z"
    }
  ],
  "result": {
    "status": "COMPLETED",
    "winner": "Gio"
  }
}
```

## Player routes

### List players

`GET /api/v1/players`

Returns distinct game players sorted by normalized name. Spectators are not included.

Request body: none.

Success: `200 OK`

```json
{
  "totalPlayers": 2,
  "players": [
    {
      "playerName": "Gio"
    },
    {
      "playerName": "Vanni"
    }
  ]
}
```

### Get a player's games

`GET /api/v1/players/{playerName}/games`

Returns all rounds associated with a player. Player lookup is case-insensitive and trims surrounding spaces.

Request body: none.

Success: `200 OK`

```json
{
  "playerName": "Gio",
  "totalGames": 2,
  "games": [
    {
      "roomCode": "H9LL",
      "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
      "symbol": "X",
      "won": true
    },
    {
      "roomCode": "H9LL",
      "gameId": "7291b377-d29d-4d69-82b0-05d20a851b3d",
      "symbol": "X",
      "won": false
    }
  ]
}
```

For a draw, both players have `won: false`.

## Error responses

Domain errors use this format:

```json
{
  "status": 404,
  "message": "Game with room code 'NONE' was not found."
}
```

Validation errors use this format:

```json
{
  "status": 400,
  "message": "Validation failed.",
  "errors": [
    "playerName is required."
  ]
}
```

Malformed JSON or an invalid symbol returns:

```json
{
  "status": 400,
  "message": "symbol must be either X or O."
}
```

Common errors:

| HTTP status | Situation |
|---|---|
| `400 Bad Request` | Missing or malformed body, invalid symbol, or coordinates outside `0`–`2` |
| `404 Not Found` | Room, game UUID, or player does not exist; a move uses a game UUID that is no longer the room's active round |
| `409 Conflict` | Duplicate player name, game has not started, round is already complete, wrong symbol's turn, or board position is occupied |

## Typical gameplay sequence

1. Create a room with `POST /api/v1/rooms` and retain both `roomCode` and `gameId`.
2. Join player O with `POST /api/v1/rooms/{roomCode}/join`.
3. Alternate moves with `POST /api/v1/games/{gameId}/move`, beginning with X.
4. Inspect the room or board with the corresponding `GET` route.
5. After completion, start another round with `POST /api/v1/rooms/{roomCode}/play-again` and use the newly returned `gameId`.
