package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.model.Player;

public class CreateGameResponse {

    private final String message;
    private final String roomCode;
    private final Player player;

    public CreateGameResponse(String message, String roomCode, Player player) {
        this.message = message;
        this.roomCode = roomCode;
        this.player = player;
    }

    public String getMessage() {
        return message;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public Player getPlayer() {
        return player;
    }
}
