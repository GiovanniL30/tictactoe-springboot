# Tic-Tac-Toe API Routes

All routes are relative to `http://localhost:8080`. Requests and responses use JSON unless a route has no request body.

## Route summary

| Method | Route | Use case |
|---|---|---|
| `GET` | `/api/v1/rooms` | List rooms and their game summaries |
| `POST` | `/api/v1/rooms` | Create a room and its first game round |
| `POST` | `/api/v1/rooms/{roomCode}/join` | Join a room as player O or as a spectator |
| `POST` | `/api/v1/rooms/{roomCode}/players/{playerName}/leave` | Leave a room and end the active round |
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
- Timestamps are returned as ISO-8601 UTC values, for example `2026-09-16T01:30:00Z`.
- A winning player's score increases by one. A completed draw has `winner: null`; otherwise `winner` is the winning player's display name.
- Empty board positions and unavailable values such as `currentTurn`, `winner`, and a spectator's `symbol` are returned as `null`.

Timestamp use cases:

- Room `createdAt` shows how long a lobby has existed.
- Player `joinedAt` records arrival order and waiting time.
- Game `createdAt` and `endedAt` allow clients to calculate round duration; `endedAt` is `null` while a round is active.
- Move `playedAt` provides move ordering, pacing, and an audit trail.

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
  "createdAt": "2026-09-16T01:29:00Z",
  "player": {
    "playerName": "Gio",
    "score": 0,
    "symbol": "X",
    "type": "PLAYER",
    "joinedAt": "2026-09-16T01:29:00Z"
  }
}
```

### Join a room

`POST /api/v1/rooms/{roomCode}/join`

Adds the second player as player O and changes the game to `IN_PROGRESS`. If X and O already exist, the player joins as a spectator instead.

Joining is rejected with `409 Conflict` if a player previously left and closed the room.

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
    "type": "PLAYER",
    "joinedAt": "2026-09-16T01:29:30Z"
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
    "type": "SPECTATOR",
    "joinedAt": "2026-09-16T01:29:45Z"
  }
}
```

### Leave a room

`POST /api/v1/rooms/{roomCode}/players/{playerName}/leave`

Removes the named member from the room. Player-name lookup is case-insensitive and ignores surrounding spaces.

When a player leaves:

- The room, game, and round statuses become `COMPLETED`.
- `currentTurn` becomes `null`.
- If the leaving player already placed a move, the opponent becomes the winner and receives one point.
- If the leaving player has not placed a move, the game has no winner and neither player's score changes.
- Both player rows are marked inactive, and all spectator rows are deleted.
- Once closed this way, the room no longer accepts joins, moves, or requests to start another round.
- The result is recorded in both players' game history and published through the game-completed realtime topic.

When a spectator leaves, only that spectator's `room_players` row is deleted. The active game continues, both players remain active, and another spectator can still join.

Request body: none.

Success: `200 OK`

```json
{
  "message": "Player left the room."
}
```

An unknown member returns `404 Not Found`. A player cannot leave a round that is already complete and receives `409 Conflict`; a spectator can still leave and be deleted after the round completes.

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
      "createdAt": "2026-09-16T01:29:00Z",
      "games": [
        {
          "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
          "status": "COMPLETED",
          "winner": "Gio",
          "createdAt": "2026-09-16T01:29:00Z",
          "endedAt": "2026-09-16T01:31:00Z"
        },
        {
          "gameId": "7291b377-d29d-4d69-82b0-05d20a851b3d",
          "status": "IN_PROGRESS",
          "createdAt": "2026-09-16T01:32:00Z"
        }
      ]
    }
  ]
}
```

### Get room information

`GET /api/v1/rooms/{roomCode}`

Returns the room creation time and the ID, status, winner, creation time, and completion time of each game in the room.

Request body: none.

Success: `200 OK`

```json
{
  "roomCode": "H9LL",
  "createdAt": "2026-09-16T01:29:00Z",
  "games": [
    {
      "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
      "status": "COMPLETED",
      "winner": "Gio",
      "createdAt": "2026-09-16T01:29:00Z",
      "endedAt": "2026-09-16T01:31:00Z"
    },
    {
      "gameId": "7291b377-d29d-4d69-82b0-05d20a851b3d",
      "status": "IN_PROGRESS",
      "createdAt": "2026-09-16T01:32:00Z"
    }
  ]
}
```

### Start another round

`POST /api/v1/rooms/{roomCode}/play-again`

Completes the previous round if needed and starts the next round with an empty board. Both players must already be present. Scores and spectators remain attached to the room.

If a player previously left, the room is inactive and this endpoint returns `409 Conflict` instead of creating another round.

Request body: none.

Success: `200 OK`

```json
{
  "message": "New round started.",
  "roomCode": "H9LL",
  "gameId": "7291b377-d29d-4d69-82b0-05d20a851b3d",
  "currentRound": 2,
  "currentTurn": "X",
  "createdAt": "2026-09-16T01:32:00Z"
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
  "createdAt": "2026-09-16T01:29:00Z",
  "games": [
    {
      "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
      "status": "COMPLETED",
      "winner": "Gio",
      "createdAt": "2026-09-16T01:29:00Z",
      "endedAt": "2026-09-16T01:31:00Z"
    },
    {
      "gameId": "7291b377-d29d-4d69-82b0-05d20a851b3d",
      "status": "IN_PROGRESS",
      "createdAt": "2026-09-16T01:32:00Z"
    }
  ]
}
```

## Game routes

### Get game information

`GET /api/v1/games/{gameId}`

Returns the selected game's players, join times, board state metadata, status, winner, and round start/end times.

Request body: none.

Success: `200 OK`

```json
{
  "players": [
    {
      "playerName": "Gio",
      "score": 1,
      "symbol": "X",
      "type": "PLAYER",
      "joinedAt": "2026-09-16T01:29:00Z"
    },
    {
      "playerName": "Vanni",
      "score": 0,
      "symbol": "O",
      "type": "PLAYER",
      "joinedAt": "2026-09-16T01:29:30Z"
    }
  ],
  "roomCode": "H9LL",
  "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
  "round": 1,
  "currentTurn": null,
  "spectatorCount": 1,
  "status": "COMPLETED",
  "winner": "Gio",
  "createdAt": "2026-09-16T01:29:00Z",
  "endedAt": "2026-09-16T01:31:00Z",
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

If a player previously left, the room is inactive and this endpoint returns `409 Conflict` without changing the board.

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

Returns all moves with their persisted play times in move-number order, followed by the round result and completion time.

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
    "winner": "Gio",
    "endedAt": "2026-09-16T01:31:00Z"
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
| `400 Bad Request` | Missing or malformed body, invalid symbol or UUID path value, or coordinates outside `0`–`2` |
| `404 Not Found` | Room, game UUID, or player does not exist; a move uses a game UUID that is no longer the room's active round |
| `409 Conflict` | Duplicate player name, inactive room, game has not started, round is already complete, wrong symbol's turn, or board position is occupied |

## Typical gameplay sequence

1. Create a room with `POST /api/v1/rooms` and retain both `roomCode` and `gameId`.
2. Join player O with `POST /api/v1/rooms/{roomCode}/join`.
3. Alternate moves with `POST /api/v1/games/{gameId}/move`, beginning with X.
4. Finish normally, or call `POST /api/v1/rooms/{roomCode}/players/{playerName}/leave` to complete the round when a player leaves.
5. Inspect the room or board with the corresponding `GET` route.
6. After completion, start another round with `POST /api/v1/rooms/{roomCode}/play-again` and use the newly returned `gameId`.

## WebSocket realtime API

The application publishes realtime updates using STOMP over a native WebSocket connection.

- Development endpoint: `ws://localhost:8080/ws`
- Broker subscription prefix: `/topic`
- Application destination prefix: `/app`
- Allowed origins: all origins

Clients only need to subscribe to topics. Gameplay commands are performed through the REST endpoints; the application currently has no client-to-server STOMP message handlers under `/app`.

### Topics

| Topic | Identifier | Published when | Payload |
|---|---|---|---|
| `/topic/rooms/{roomCode}/player-joined` | Room code | A player or spectator joins | `GameInfoResponse` |
| `/topic/rooms/{roomCode}/game-completed` | Room code | A player wins, the board ends in a draw, or a player leaves | `GameInfoResponse` |
| `/topic/rooms/{roomCode}/new-round-started` | Room code | `play-again` creates the next round | `PlayAgainResponse` |
| `/topic/rooms/{roomCode}/game-deleted` | Room code | The room and its games are deleted | `RoomInfoResponse` |
| `/topic/games/{gameId}/move-placed` | Active game UUID | A valid move is placed | `BoardResponse` |

Subscribe to the room topics using the `roomCode` returned by create-room. Subscribe to the move topic using the current `gameId`. After a `new-round-started` event, unsubscribe from the old game topic and subscribe using the new `gameId` from the event payload.

### Payload shapes

`GameInfoResponse`, used by `player-joined` and `game-completed`:

```json
{
  "players": [
    {
      "playerName": "Gio",
      "score": 0,
      "symbol": "X",
      "type": "PLAYER",
      "joinedAt": "2026-09-16T01:29:00Z"
    }
  ],
  "roomCode": "H9LL",
  "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
  "round": 1,
  "currentTurn": "X",
  "spectatorCount": 0,
  "status": "IN_PROGRESS",
  "winner": null,
  "createdAt": "2026-09-16T01:29:00Z",
  "endedAt": null,
  "message": "Player joined successfully."
}
```

`BoardResponse`, used by `move-placed`:

```json
{
  "message": "Move placed successfully.",
  "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
  "grid": [
    ["X", null, null],
    [null, null, null],
    [null, null, null]
  ],
  "currentTurn": "O",
  "status": "IN_PROGRESS"
}
```

`PlayAgainResponse`, used by `new-round-started`:

```json
{
  "message": "New round started.",
  "roomCode": "H9LL",
  "gameId": "7291b377-d29d-4d69-82b0-05d20a851b3d",
  "currentRound": 2,
  "currentTurn": "X",
  "createdAt": "2026-09-16T01:32:00Z"
}
```

`RoomInfoResponse`, used by `game-deleted`:

```json
{
  "roomCode": "H9LL",
  "createdAt": "2026-09-16T01:29:00Z",
  "games": [
    {
      "gameId": "1f0c5258-219a-4f76-adc0-38b08300317b",
      "status": "COMPLETED",
      "winner": "Gio",
      "createdAt": "2026-09-16T01:29:00Z",
      "endedAt": "2026-09-16T01:31:00Z"
    }
  ]
}
```

### JavaScript STOMP example

This example uses `@stomp/stompjs` and subscribes to both room-level events and moves for the active game:

```javascript
import { Client } from '@stomp/stompjs';

const roomCode = 'H9LL';
let gameId = '1f0c5258-219a-4f76-adc0-38b08300317b';
let moveSubscription;

const client = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  reconnectDelay: 5000
});

const readMessage = frame => JSON.parse(frame.body);

client.onConnect = () => {
  client.subscribe(`/topic/rooms/${roomCode}/player-joined`, frame => {
    console.log('Player joined:', readMessage(frame));
  });

  client.subscribe(`/topic/rooms/${roomCode}/game-completed`, frame => {
    console.log('Game completed:', readMessage(frame));
  });

  client.subscribe(`/topic/rooms/${roomCode}/game-deleted`, frame => {
    console.log('Game deleted:', readMessage(frame));
  });

  const subscribeToMoves = () => {
    moveSubscription?.unsubscribe();
    moveSubscription = client.subscribe(`/topic/games/${gameId}/move-placed`, frame => {
      console.log('Move placed:', readMessage(frame));
    });
  };

  subscribeToMoves();

  client.subscribe(`/topic/rooms/${roomCode}/new-round-started`, frame => {
    const nextRound = readMessage(frame);
    gameId = nextRound.gameId;
    subscribeToMoves();
  });
};

client.activate();
```

Use `wss://` instead of `ws://` when the API is served over HTTPS.
