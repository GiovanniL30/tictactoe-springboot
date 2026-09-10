package com.svi.tictactoe.model;

import com.svi.tictactoe.constants.Symbol;

public class Board {

    private static final int SIZE = 3;

    private final Symbol[][] grid;

    public Board() {
        this.grid = new Symbol[SIZE][SIZE];
    }

    public boolean placeSymbol(Symbol symbol, int x, int y) {
        if (!isValidPosition(x, y) || grid[x][y] != null) {
            return false;
        }

        grid[x][y] = symbol;
        return true;
    }

    public Symbol[][] getGrid() {
        Symbol[][] copy = new Symbol[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            copy[i] = grid[i].clone();
        }

        return copy;
    }

    public boolean isEmpty(int x, int y) {
        return grid[x][y] == null;
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }
}
