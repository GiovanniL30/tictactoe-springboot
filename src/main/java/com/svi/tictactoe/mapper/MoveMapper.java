package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.entity.GameMoveEntity;

public final class MoveMapper {

    private MoveMapper() {
    }

    public static AddMoveRequest toAddMoveRequest(GameMoveEntity entity) {
        return new AddMoveRequest(
                entity.getX(),
                entity.getY(),
                toSymbol(entity.getSymbol())
        );
    }

    private static Symbol toSymbol(String value) {
        return value == null || value.isBlank() ? null : Symbol.fromString(value);
    }
}
