package com.svi.tictactoe.engine;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.util.BoardUtil;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class GameEngine {

    public boolean hasWinner(List<String> board, Symbol symbol) {
        String value = symbol.name();

        for (int index = 0; index < BoardUtil.BOARD_SIZE; index++) {
            int rowStart = index * BoardUtil.BOARD_SIZE;

            boolean hasWinningRow = value.equals(board.get(rowStart))
                    && value.equals(board.get(rowStart + 1))
                    && value.equals(board.get(rowStart + 2));

            if (hasWinningRow) {
                return true;
            }

            boolean hasWinningColumn = value.equals(board.get(index))
                    && value.equals(board.get(BoardUtil.BOARD_SIZE + index))
                    && value.equals(board.get(2 * BoardUtil.BOARD_SIZE + index));

            if (hasWinningColumn) {
                return true;
            }
        }

        boolean hasWinningLeftDiagonal = value.equals(board.get(0))
                && value.equals(board.get(4))
                && value.equals(board.get(8));

        boolean hasWinningRightDiagonal = value.equals(board.get(2))
                && value.equals(board.get(4))
                && value.equals(board.get(6));

        return hasWinningLeftDiagonal || hasWinningRightDiagonal;
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < BoardUtil.BOARD_SIZE && y >= 0 && y < BoardUtil.BOARD_SIZE;
    }
}
