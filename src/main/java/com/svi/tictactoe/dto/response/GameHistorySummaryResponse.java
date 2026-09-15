package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.GameStatus;

import java.util.UUID;

public record GameHistorySummaryResponse(
        UUID gameId,
        GameStatus status,
        String winner) {
}
