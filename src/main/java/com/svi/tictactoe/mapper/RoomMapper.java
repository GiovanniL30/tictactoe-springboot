package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.game.PlayAgainResponse;
import com.svi.tictactoe.entity.RoomEntity;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static PlayAgainResponse toPlayAgainResponse(RoomEntity entity, String message) {
        return new PlayAgainResponse(
                message,
                entity.getRoomCode(),
                entity.getActiveGameId(),
                entity.getCurrentRound(),
                Symbol.X
        );
    }
}
