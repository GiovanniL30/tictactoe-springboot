package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("moves_by_game")
public class MoveByGameEntity {

    @PrimaryKeyColumn(name = "game_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID gameId;

    @PrimaryKeyColumn(name = "move_no", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Integer moveNo;

    @Column("room_code")
    private String roomCode;

    @Column("player_name")
    private String playerName;

    @Column("symbol")
    private String symbol;

    @Column("x")
    private Integer x;

    @Column("y")
    private Integer y;

    @Column("played_at")
    private Instant playedAt;

    public MoveByGameEntity() {
    }

    public MoveByGameEntity(UUID gameId, Integer moveNo, String roomCode, String playerName, String symbol, Integer x, Integer y, Instant playedAt) {
        this.gameId = gameId;
        this.moveNo = moveNo;
        this.roomCode = roomCode;
        this.playerName = playerName;
        this.symbol = symbol;
        this.x = x;
        this.y = y;
        this.playedAt = playedAt;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public Integer getMoveNo() {
        return moveNo;
    }

    public void setMoveNo(Integer moveNo) {
        this.moveNo = moveNo;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Instant getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(Instant playedAt) {
        this.playedAt = playedAt;
    }
}
