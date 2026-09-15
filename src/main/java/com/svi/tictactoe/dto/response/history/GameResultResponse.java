package com.svi.tictactoe.dto.response.history;

import com.svi.tictactoe.constants.GameStatus;

public record GameResultResponse(
        GameStatus status,
        String winner) {
}
