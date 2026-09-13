CREATE KEYSPACE IF NOT EXISTS tictactoe
WITH replication = {
    'class': 'SimpleStrategy',
    'replication_factor': 1
};

USE tictactoe;

CREATE TABLE IF NOT EXISTS games_by_room (
    room_code text,
    round_no int,
    game_id uuid,
    status text,
    created_at timestamp,
    ended_at timestamp,
    PRIMARY KEY (room_code, round_no)
) WITH CLUSTERING ORDER BY (round_no ASC);

CREATE TABLE IF NOT EXISTS games_by_id (
    game_id uuid PRIMARY KEY,
    room_code text,
    round_no int,
    status text,
    current_turn text,
    winner text,
    board list<text>,
);

CREATE TABLE IF NOT EXISTS moves_by_game (
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

CREATE TABLE IF NOT EXISTS games_by_player (
    normalized_player_name text,
    joined_at timestamp,
    game_id uuid,
    room_code text,
    player_name text,
    symbol text,
    result text,
    PRIMARY KEY (normalized_player_name, joined_at, game_id)
) WITH CLUSTERING ORDER BY (joined_at DESC, game_id ASC);
