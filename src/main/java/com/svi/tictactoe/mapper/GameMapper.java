package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.dto.response.game.BoardResponse;
import com.svi.tictactoe.dto.response.game.GameInfoResponse;
import com.svi.tictactoe.dto.response.player.PlayerResponse;
import com.svi.tictactoe.dto.response.room.GameSummaryResponse;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.entity.GameRoundEntity;
import com.svi.tictactoe.util.BoardUtil;

import java.time.Instant;
import java.util.List;

public final class GameMapper {

    private GameMapper() {
    }

    public static BoardResponse toBoardResponse(GameEntity entity, String message) {
        return new BoardResponse(
                message,
                entity.getGameId(),
                BoardUtil.toGrid(entity.getBoard()),
                BoardUtil.toSymbol(entity.getCurrentTurn()),
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
                BoardUtil.toSymbol(entity.getCurrentTurn()),
                spectatorCount,
                toGameStatus(entity.getStatus()),
                entity.getWinner(),
                createdAt,
                endedAt,
                message
        );
    }

    public static GameSummaryResponse toGameSummaryResponse(GameEntity game, GameRoundEntity round) {
        return new GameSummaryResponse(
                game.getGameId(),
                GameStatus.valueOf(game.getStatus()),
                game.getWinner(),
                round.getCreatedAt(),
                round.getEndedAt()
        );
    }

    private static GameStatus toGameStatus(String value) {
        return GameStatus.valueOf(value);
    }
}
