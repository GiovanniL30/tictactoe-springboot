package com.svi.tictactoe.constants;

public enum ErrorMessage {

    POSITION_ALREADY_TAKEN("Position (%d, %d) is already taken."),
    GAME_NOT_FOUND("Game with room code '%s' was not found."),
    GAME_NOT_STARTED("Game requires two players before another round can start."),
    PLAYER_ALREADY_EXISTS("Participant name '%s' is already in use."),
    PLAYER_NOT_FOUND("Player '%s' was not found."),
    INVALID_TURN("It is not player '%s''s turn."),
    INVALID_POSITION("Position (%d, %d) is invalid.");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }
}
