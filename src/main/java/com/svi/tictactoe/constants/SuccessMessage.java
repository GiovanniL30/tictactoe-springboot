package com.svi.tictactoe.constants;

public enum SuccessMessage {

    GAME_CREATED("Game created successfully."),
    GAME_COMPLETED("Game completed."),
    MOVE_PLACED("Move placed successfully."),
    NEW_ROUND_STARTED("New round started."),
    PLAYER_JOINED("Player joined successfully."),
    SPECTATOR_JOINED("Game already has two players. Joined as spectator."),
    GAME_INFO_RETRIEVED("Game information retrieved successfully."),
    BOARD_STATUS_RETRIEVED("Latest Board Grid");

    private final String message;

    SuccessMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
