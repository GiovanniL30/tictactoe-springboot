package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("games_by_player")
public class PlayerGameEntity {

    @PrimaryKeyColumn(name = "normalized_player_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String normalizedPlayerName;

    @PrimaryKeyColumn(name = "game_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID gameId;

    @Column("room_code")
    private String roomCode;

    @Column("symbol")
    private String symbol;

    @Column("won")
    private Boolean won;

    public PlayerGameEntity() {
    }

    public PlayerGameEntity(
            String normalizedPlayerName,
            UUID gameId,
            String roomCode,
            String symbol,
            Boolean won) {
        this.normalizedPlayerName = normalizedPlayerName;
        this.gameId = gameId;
        this.roomCode = roomCode;
        this.symbol = symbol;
        this.won = won;
    }

    public String getNormalizedPlayerName() {
        return normalizedPlayerName;
    }

    public void setNormalizedPlayerName(String normalizedPlayerName) {
        this.normalizedPlayerName = normalizedPlayerName;
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Boolean getWon() {
        return won;
    }

    public void setWon(Boolean won) {
        this.won = won;
    }
}
