package com.svi.tictactoe.model;

import com.svi.tictactoe.constants.Symbol;

public class Board {

    private static final int SIZE = 3;

    private final Symbol[][] board;

    public Board() {
        this.board = new Symbol[SIZE][SIZE];
    }

    public boolean placeSymbol(Symbol symbol, int x, int y) {
        if (!isValidPosition(x, y) || board[x][y] != null) {
            return false;
        }

        board[x][y] = symbol;
        return true;
    }

    public Symbol[][] getBoard() {
        Symbol[][] copy = new Symbol[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            copy[i] = board[i].clone();
        }

        return copy;
    }

    public boolean isEmpty(int x, int y) {
        return board[x][y] == null;
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }
}