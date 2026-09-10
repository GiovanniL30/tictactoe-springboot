package com.svi.tictactoe.dto.response;

public class CreateGameResponse {

    private final String message;
    private final String roomCode;

    public CreateGameResponse(String message, String roomCode) {
        this.message = message;
        this.roomCode = roomCode;
    }

    public String getMessage() {
        return message;
    }

    public String getRoomCode() {
        return roomCode;
    }
}