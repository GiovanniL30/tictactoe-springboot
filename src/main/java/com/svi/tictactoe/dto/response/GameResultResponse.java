package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.GameStatus;

public record GameResultResponse(
        GameStatus status,
        String winner) {
}
