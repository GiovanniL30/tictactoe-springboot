package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.PlayAgainResponse;
import com.svi.tictactoe.entity.GameByRoomEntity;
import com.svi.tictactoe.model.Game;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static PlayAgainResponse toPlayAgainResponse(Game game, String message) {
        return new PlayAgainResponse(
                message,
                game.getRoomCode(),
                game.getActiveGameId(),
                game.getRound(),
                game.getCurrentTurn()
        );
    }

    public static PlayAgainResponse toPlayAgainResponse(GameByRoomEntity entity, Symbol currentTurn, String message) {
        return new PlayAgainResponse(
                message,
                entity.getRoomCode(),
                entity.getGameId(),
                entity.getRoundNo(),
                currentTurn
        );
    }
}
