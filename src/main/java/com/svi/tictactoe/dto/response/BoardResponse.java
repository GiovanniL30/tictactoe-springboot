package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.Symbol;

public record BoardResponse(
        String message,
        Symbol[][] grid,
        Symbol currentTurn) {
}
