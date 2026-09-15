package com.svi.tictactoe.dto.response.history;

import java.util.List;
import java.util.UUID;

public record GameMoveHistoryResponse(
        UUID gameId,
        String roomCode,
        int round,
        List<MoveResponse> moves,
        GameResultResponse result) {
}
