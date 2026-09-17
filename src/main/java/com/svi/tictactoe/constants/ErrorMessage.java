package com.svi.tictactoe.constants;

public enum ErrorMessage {

    POSITION_ALREADY_TAKEN("Position (%d, %d) is already taken."),
    GAME_NOT_FOUND("Game with room code '%s' was not found."),
    GAME_ID_NOT_FOUND("Game with id '%s' was not found."),
    GAME_NOT_STARTED("Game requires two players before it can start."),
    GAME_ALREADY_FINISHED("This round is already finished. Start a new round to continue."),
    ROOM_INACTIVE("This room is no longer active because a player has left."),
    PLAYER_ALREADY_EXISTS("Player name '%s' is already in use."),
    PLAYER_NOT_FOUND("Player '%s' was not found."),
    OPPONENT_OF_PLAYER_NOT_FOUND("Opponent for player '%s' was not found."),
    INVALID_TURN("It is player '%s''s turn."),
    INVALID_POSITION("Position (%d, %d) is invalid."),
    VALIDATION_FAILED("Validation failed."),
    INVALID_SYMBOL("symbol must be either X or O."),
    INVALID_REQUEST_BODY("Request body is missing or invalid.");

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
