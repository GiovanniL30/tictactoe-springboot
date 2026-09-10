package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.Symbol;

public class AddMoveResponse {

    private final String message;
    private final Symbol[][] board;

    public AddMoveResponse(String message, Symbol[][] board) {
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
