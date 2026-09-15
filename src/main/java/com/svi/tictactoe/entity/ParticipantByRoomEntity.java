package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Table("participants_by_room")
public class ParticipantByRoomEntity {

    @PrimaryKeyColumn(name = "room_code", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String roomCode;

    @PrimaryKeyColumn(name = "normalized_player_name", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String normalizedPlayerName;

    @Column("player_name")
    private String playerName;

    @Column("player_type")
    private String playerType;

    @Column("symbol")
    private String symbol;

    @Column("score")
    private Integer score;

    @Column("joined_at")
    private Instant joinedAt;

    public ParticipantByRoomEntity() {
    }

    public ParticipantByRoomEntity(
            String roomCode,
            String normalizedPlayerName,
            String playerName,
            String playerType,
            String symbol,
            Integer score,
            Instant joinedAt) {
        this.roomCode = roomCode;
        this.normalizedPlayerName = normalizedPlayerName;
        this.playerName = playerName;
        this.playerType = playerType;
        this.symbol = symbol;
        this.score = score;
        this.joinedAt = joinedAt;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getNormalizedPlayerName() {
        return normalizedPlayerName;
    }

    public void setNormalizedPlayerName(String normalizedPlayerName) {
        this.normalizedPlayerName = normalizedPlayerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerType() {
        return playerType;
    }

    public void setPlayerType(String playerType) {
        this.playerType = playerType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
