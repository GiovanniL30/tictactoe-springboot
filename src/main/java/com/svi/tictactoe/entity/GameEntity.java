package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table("games_by_id")
public class GameEntity {

    @PrimaryKey("game_id")
    private UUID gameId;

    @Column("room_code")
    private String roomCode;

    @Column("round_no")
    private Integer roundNo;

    @Column("status")
    private String status;

    @Column("current_turn")
    private String currentTurn;

    @Column("winner")
    private String winner;

    @Column("move_count")
    private Integer moveCount;

    @Column("board")
    private List<String> board;

    public GameEntity() {
    }

    public GameEntity(UUID gameId, String roomCode, Integer roundNo, String status, String currentTurn, String winner, Integer moveCount, List<String> board) {
        this.gameId = gameId;
        this.roomCode = roomCode;
        this.roundNo = roundNo;
        this.status = status;
        this.currentTurn = currentTurn;
        this.winner = winner;
        this.moveCount = moveCount;
        this.board = board;
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

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Integer getMoveCount() {
        return moveCount;
    }

    public void setMoveCount(Integer moveCount) {
        this.moveCount = moveCount;
    }

    public List<String> getBoard() {
        return board;
    }

    public void setBoard(List<String> board) {
        this.board = board;
    }
}
