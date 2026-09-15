package com.svi.tictactoe.dto.response.error;

public record ErrorResponse(
        int status,
        String message
) {
}
