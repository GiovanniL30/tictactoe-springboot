package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.CreateGameResponse;
import com.svi.tictactoe.entity.GameByPlayerEntity;
import com.svi.tictactoe.model.Player;

public final class PlayerMapper {

    private PlayerMapper() {
    }

    public static CreateGameResponse toCreateGameResponse(GameByPlayerEntity entity, String message) {
        return new CreateGameResponse(
                message,
                entity.getRoomCode(),
                entity.getGameId(),
                new Player(entity.getPlayerName(), toSymbol(entity.getSymbol()))
        );
    }

    private static Symbol toSymbol(String value) {
        return value == null || value.isBlank() ? null : Symbol.fromString(value);
    }
}
