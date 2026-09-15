package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.Symbol;

import java.util.UUID;

public record BoardResponse(
        String message,
        UUID gameId,
        Symbol[][] grid,
        Symbol currentTurn,
        GameStatus status) {
}
