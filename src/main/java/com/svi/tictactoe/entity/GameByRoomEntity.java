package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("games_by_room")
public class GameByRoomEntity {

    @PrimaryKeyColumn(name = "room_code", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String roomCode;

    @PrimaryKeyColumn(name = "round_no", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Integer roundNo;

    @Column("game_id")
    private UUID gameId;

    @Column("status")
    private String status;

    @Column("created_at")
    private Instant createdAt;

    @Column("ended_at")
    private Instant endedAt;

    public GameByRoomEntity() {
    }

    public GameByRoomEntity(String roomCode, Integer roundNo, UUID gameId, String status, Instant createdAt, Instant endedAt) {
        this.roomCode = roomCode;
        this.roundNo = roundNo;
        this.gameId = gameId;
        this.status = status;
        this.createdAt = createdAt;
        this.endedAt = endedAt;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }
}
