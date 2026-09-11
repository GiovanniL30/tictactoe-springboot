package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.model.Player;

import java.util.List;

public class GameStatusResponse {

    private final Symbol[][] grid;
    private final List<Player> players;
    private final String roomCode;
    private final int round;
    private final int spectatorCount;
    private final String message;

    public GameStatusResponse(Symbol[][] grid, List<Player> players, String roomCode, int round, int spectatorCount, String message) {
        this.grid = grid;
        this.players = players;
        this.roomCode = roomCode;
        this.round = round;
        this.spectatorCount = spectatorCount;
        this.message = message;
    }

    public Symbol[][] getGrid() {
        return grid;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getRound() {
        return round;
    }

    public int getSpectatorCount() {
        return spectatorCount;
    }

    public String getMessage() {
        return message;
    }
}
