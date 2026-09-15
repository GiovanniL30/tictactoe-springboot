package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.Symbol;

import java.time.Instant;

public record MoveResponse(
        int moveNumber,
        String playerName,
        Symbol symbol,
        int x,
        int y,
        Instant playedAt) {
}
