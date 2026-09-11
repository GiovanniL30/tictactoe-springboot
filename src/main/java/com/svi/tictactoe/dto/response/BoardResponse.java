package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.Symbol;

public class BoardResponse {

    private final String message;
    private final Symbol[][] grid;
    private final Symbol currentTurn;

    public BoardResponse(String message, Symbol[][] grid, Symbol currentTurn) {
        this.message = message;
        this.grid = grid;
        this.currentTurn = currentTurn;
    }

    public String getMessage() {
        return message;
    }

    public Symbol[][] getGrid() {
        return grid;
    }

    public Symbol getCurrentTurn() {
        return currentTurn;
    }
}
