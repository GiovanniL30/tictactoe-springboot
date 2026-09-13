package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.BoardResponse;
import com.svi.tictactoe.dto.response.GameStatusResponse;
import com.svi.tictactoe.entity.GameByIdEntity;
import com.svi.tictactoe.model.Player;

import java.util.List;

public final class GameMapper {

    private static final int BOARD_SIZE = 3;

    private GameMapper() {
    }

    public static BoardResponse toBoardResponse(GameByIdEntity entity, String message) {
        return new BoardResponse(
                message,
                entity.getGameId(),
                toGrid(entity.getBoard()),
                toSymbol(entity.getCurrentTurn())
        );
    }

    public static GameStatusResponse toGameStatusResponse(GameByIdEntity entity, List<Player> players, int spectatorCount, String message) {
        return new GameStatusResponse(
                toGrid(entity.getBoard()),
                players,
                entity.getRoomCode(),
                entity.getGameId(),
                entity.getRoundNo(),
                toSymbol(entity.getCurrentTurn()),
                spectatorCount,
                message
        );
    }

    private static Symbol[][] toGrid(List<String> board) {
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

    private static Symbol toSymbol(String value) {
        return value == null || value.isBlank() ? null : Symbol.fromString(value);
    }
}
