package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.constants.GameStatus;

public record GameResultResponse(
        GameStatus status,
        String winner) {
}
