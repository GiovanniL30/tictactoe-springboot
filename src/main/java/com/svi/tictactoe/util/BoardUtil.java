package com.svi.tictactoe.util;

import com.svi.tictactoe.constants.Symbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BoardUtil {

    public static final int BOARD_SIZE = 3;

    private BoardUtil() {
    }

    public static List<String> mutableBoard(List<String> persistedBoard) {
        List<String> board = persistedBoard == null
                ? emptyBoard()
                : new ArrayList<>(persistedBoard);
        while (board.size() < BOARD_SIZE * BOARD_SIZE) {
            board.add("");
        }
        return board;
    }

    public static List<String> emptyBoard() {
        return new ArrayList<>(Collections.nCopies(BOARD_SIZE * BOARD_SIZE, ""));
    }

    public static Symbol[][] toGrid(List<String> board) {
        Symbol[][] grid = new Symbol[BOARD_SIZE][BOARD_SIZE];
        if (board == null) {
            return grid;
        }

        int cellCount = BOARD_SIZE * BOARD_SIZE;

        for (int index = 0; index < Math.min(board.size(), cellCount); index++) {
            String cell = board.get(index);
            if (cell != null && !cell.isBlank()) {
                grid[index / BOARD_SIZE][index % BOARD_SIZE] = Symbol.fromString(cell);
            }
        }

        return grid;
    }

    public static Symbol toSymbol(String value) {
        return value == null || value.isBlank() ? null : Symbol.fromString(value);
    }
}
