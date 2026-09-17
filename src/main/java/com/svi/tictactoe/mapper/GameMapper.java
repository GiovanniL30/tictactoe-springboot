package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.game.BoardResponse;
import com.svi.tictactoe.dto.response.game.GameInfoResponse;
import com.svi.tictactoe.dto.response.player.PlayerResponse;
import com.svi.tictactoe.entity.GameEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.svi.tictactoe.util.BoardUtil.BOARD_SIZE;

public final class GameMapper {

    private GameMapper() {
    }

    public static BoardResponse toBoardResponse(GameEntity entity, String message) {
        return new BoardResponse(
                message,
                entity.getGameId(),
                toGrid(entity.getBoard()),
                toSymbol(entity.getCurrentTurn()),
                toGameStatus(entity.getStatus())
        );
    }

    public static GameInfoResponse toGameInfoResponse(
            GameEntity entity,
            List<PlayerResponse> players,
            int spectatorCount,
            Instant createdAt,
            Instant endedAt,
            String message) {
        return new GameInfoResponse(
                players,
                entity.getRoomCode(),
                entity.getGameId(),
                entity.getRoundNo(),
                toSymbol(entity.getCurrentTurn()),
                spectatorCount,
                toGameStatus(entity.getStatus()),
                entity.getWinner(),
                createdAt,
                endedAt,
                message
        );
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

    private static Symbol toSymbol(String value) {
        return value == null || value.isBlank() ? null : Symbol.fromString(value);
    }

    private static GameStatus toGameStatus(String value) {
        return GameStatus.valueOf(value);
    }
}
