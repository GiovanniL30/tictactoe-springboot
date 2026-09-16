package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("rooms")
public class RoomEntity {

    @PrimaryKey("room_code")
    private String roomCode;

    @Column("active_game_id")
    private UUID activeGameId;

    @Column("current_round")
    private Integer currentRound;

    @Column("status")
    private String status;

    @Column("created_at")
    private Instant createdAt;

    public RoomEntity() {
    }

    public RoomEntity(String roomCode, UUID activeGameId, Integer currentRound, String status, Instant createdAt) {
        this.roomCode = roomCode;
        this.activeGameId = activeGameId;
        this.currentRound = currentRound;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public UUID getActiveGameId() {
        return activeGameId;
    }

    public void setActiveGameId(UUID activeGameId) {
        this.activeGameId = activeGameId;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
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
}
