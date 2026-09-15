package com.svi.tictactoe.dto.response.history;

import com.svi.tictactoe.constants.GameStatus;

import java.util.UUID;

public record GameHistorySummaryResponse(
        UUID gameId,
        GameStatus status,
        String winner) {
}
