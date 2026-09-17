CREATE KEYSPACE IF NOT EXISTS tictactoe
WITH replication = {
    'class': 'SimpleStrategy',
    'replication_factor': 1
};

USE tictactoe;

-- One row per room. Used by every room-code endpoint and room-code uniqueness checks.
CREATE TABLE IF NOT EXISTS rooms (
    room_code text PRIMARY KEY,
    active_game_id uuid,
    current_round int,
    status text,
    created_at timestamp
);

-- Fixed-partition catalog used by the history endpoint to list room codes.
CREATE TABLE IF NOT EXISTS room_catalog (
    catalog_key text,
    room_code text,
    PRIMARY KEY (catalog_key, room_code)
);

-- One row per round. Used by the move endpoint, which receives a game id.
CREATE TABLE IF NOT EXISTS games (
    game_id uuid PRIMARY KEY,
    room_code text,
    round_no int,
    status text,
    current_turn text,
    winner text,
    move_count int,
    board list<text>
);

-- Round history for a room. Used when advancing or deleting a room.
CREATE TABLE IF NOT EXISTS game_rounds (
    room_code text,
    round_no int,
    game_id uuid,
    status text,
    created_at timestamp,
    ended_at timestamp,
    PRIMARY KEY (room_code, round_no)
) WITH CLUSTERING ORDER BY (round_no ASC);

-- Room-lifetime membership. Scores and spectators survive game-id changes.
-- When either player leaves, both player rows become inactive and spectator rows are deleted.
CREATE TABLE IF NOT EXISTS room_players (
    room_code text,
    normalized_player_name text,
    player_name text,
    player_type text,
    symbol text,
    score int,
    joined_at timestamp,
    is_active boolean,
    PRIMARY KEY (room_code, normalized_player_name)
);

-- Immutable move history for one round.
CREATE TABLE IF NOT EXISTS game_moves (
    game_id uuid,
    move_no int,
    room_code text,
    player_name text,
    symbol text,
    x int,
    y int,
    played_at timestamp,
    PRIMARY KEY (game_id, move_no)
) WITH CLUSTERING ORDER BY (move_no ASC);

-- Fixed-partition catalog used to list distinct players by normalized name.
CREATE TABLE IF NOT EXISTS player_catalog (
    catalog_key text,
    normalized_player_name text,
    player_name text,
    PRIMARY KEY (catalog_key, normalized_player_name)
);

-- Query-first projection of games played by one player.
CREATE TABLE IF NOT EXISTS player_games (
    normalized_player_name text,
    game_id uuid,
    room_code text,
    symbol text,
    won boolean,
    PRIMARY KEY (normalized_player_name, game_id)
);
