package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.game.MoveResponse;
import com.svi.tictactoe.entity.GameMoveEntity;

public final class MoveMapper {

    private MoveMapper() {
    }

    public static MoveResponse toMoveResponse(GameMoveEntity move) {
        return new MoveResponse(
                move.getMoveNo(),
                move.getPlayerName(),
                Symbol.fromString(move.getSymbol()),
                move.getX(),
                move.getY(),
                move.getPlayedAt()
        );
    }
}
