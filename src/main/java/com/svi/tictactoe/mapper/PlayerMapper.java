package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.JoinGameResponse;
import com.svi.tictactoe.dto.response.ParticipantResponse;
import com.svi.tictactoe.entity.ParticipantByRoomEntity;

public final class PlayerMapper {

    private PlayerMapper() {
    }

    public static JoinGameResponse toJoinGameResponse(ParticipantByRoomEntity participant, String message) {
        return new JoinGameResponse(message, toParticipantResponse(participant));
    }

    public static ParticipantResponse toParticipantResponse(ParticipantByRoomEntity entity) {
        return new ParticipantResponse(
                entity.getPlayerName(),
                entity.getScore() == null ? 0 : entity.getScore(),
                toSymbol(entity.getSymbol()),
                PlayerType.valueOf(entity.getPlayerType())
        );
    }

    private static Symbol toSymbol(String value) {
        return value == null || value.isBlank() ? null : Symbol.fromString(value);
    }
}
