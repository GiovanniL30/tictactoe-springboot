package com.svi.tictactoe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRoomRequest(
        @NotBlank(message = "playerName is required.")
        @Size(max = 20, message = "playerName must be at most 20 characters.")
        String playerName
) {
}
