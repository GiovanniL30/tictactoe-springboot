package com.svi.tictactoe.dto.response.player;

import com.svi.tictactoe.constants.Symbol;

import java.util.UUID;

public record PlayerGameSummaryResponse(
        String roomCode,
        UUID gameId,
        Symbol symbol,
        boolean won) {
}
