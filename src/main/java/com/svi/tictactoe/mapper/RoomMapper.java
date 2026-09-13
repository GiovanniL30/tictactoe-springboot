package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.PlayAgainResponse;
import com.svi.tictactoe.entity.GameByRoomEntity;

public final class RoomMapper {

    private RoomMapper() {
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
