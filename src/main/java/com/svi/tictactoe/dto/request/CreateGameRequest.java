package com.svi.tictactoe.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateGameRequest {

    @NotBlank(message = "playerName is required.")
    private String playerName;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
