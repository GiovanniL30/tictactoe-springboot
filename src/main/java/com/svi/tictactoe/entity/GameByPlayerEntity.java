package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("games_by_player")
public class GameByPlayerEntity {

    @PrimaryKeyColumn(name = "normalized_player_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String normalizedPlayerName;

    @PrimaryKeyColumn(name = "joined_at", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Instant joinedAt;

    @PrimaryKeyColumn(name = "game_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID gameId;

    @Column("room_code")
    private String roomCode;

    @Column("player_name")
    private String playerName;

    @Column("symbol")
    private String symbol;

    @Column("result")
    private String result;

    public GameByPlayerEntity() {
    }

    public GameByPlayerEntity(String normalizedPlayerName, Instant joinedAt, UUID gameId, String roomCode, String playerName, String symbol, String result) {
        this.normalizedPlayerName = normalizedPlayerName;
        this.joinedAt = joinedAt;
        this.gameId = gameId;
        this.roomCode = roomCode;
        this.playerName = playerName;
        this.symbol = symbol;
        this.result = result;
    }

    public String getNormalizedPlayerName() {
        return normalizedPlayerName;
    }

    public void setNormalizedPlayerName(String normalizedPlayerName) {
        this.normalizedPlayerName = normalizedPlayerName;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
