package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.Symbol;

public class BoardResponse {

    private final String message;
    private final Symbol[][] grid;

    public BoardResponse(String message, Symbol[][] grid) {
        this.message = message;
        this.grid = grid;
    }

    public String getMessage() {
        return message;
    }

    public Symbol[][] getGrid() {
        return grid;
    }
}
