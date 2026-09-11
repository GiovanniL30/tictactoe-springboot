package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.Symbol;

public class BoardResponse {

    private final String message;
    private final Symbol[][] board;

    public BoardResponse(String message, Symbol[][] board) {
        this.message = message;
        this.board = board;
    }

    public String getMessage() {
        return message;
    }

    public Symbol[][] getBoard() {
        return board;
    }
}
