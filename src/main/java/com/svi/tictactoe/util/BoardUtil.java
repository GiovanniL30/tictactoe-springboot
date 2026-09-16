package com.svi.tictactoe.util;

import com.svi.tictactoe.mapper.GameMapper;

import java.util.ArrayList;
import java.util.List;

public final class BoardUtil {

    public static final int BOARD_SIZE = 3;

    private BoardUtil(){
    }

    public static List<String> mutableBoard(List<String> persistedBoard) {
        List<String> board = persistedBoard == null
                ? GameMapper.emptyBoard()
                : new ArrayList<>(persistedBoard);
        while (board.size() < BOARD_SIZE * BOARD_SIZE) {
            board.add("");
        }
        return board;
    }

}
