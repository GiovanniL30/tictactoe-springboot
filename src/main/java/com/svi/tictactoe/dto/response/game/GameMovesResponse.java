package com.svi.tictactoe.dto.response.game;

import java.util.List;
import java.util.UUID;

public record GameMovesResponse(
        UUID gameId,
        String roomCode,
        int round,
        List<MoveResponse> moves,
        GameResultResponse result) {
}
