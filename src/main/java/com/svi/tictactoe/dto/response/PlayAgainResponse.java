package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.model.Player;

import java.util.List;

public class PlayAgainResponse {

    private final String message;
    private final int currentRound;
    private final List<Player> players;

    public PlayAgainResponse(String message, int currentRound, List<Player> players) {
        this.message = message;
        this.currentRound = currentRound;
        this.players = List.copyOf(players);
    }

    public String getMessage() {
        return message;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
