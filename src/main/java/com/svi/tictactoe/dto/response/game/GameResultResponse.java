package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.constants.GameStatus;

import java.time.Instant;

public record GameResultResponse(
        GameStatus status,
        String winner,
        Instant endedAt) {
}
