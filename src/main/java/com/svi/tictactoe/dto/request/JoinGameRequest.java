package com.svi.tictactoe.dto.request;

import jakarta.validation.constraints.NotBlank;

public record JoinGameRequest(
        @NotBlank(message = "playerName is required.")
        String playerName
) {
}
