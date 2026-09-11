package com.svi.tictactoe.dto.response;

public record ErrorResponse(
        int status,
        String message
) {
}
