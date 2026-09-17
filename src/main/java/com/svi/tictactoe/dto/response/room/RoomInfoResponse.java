package com.svi.tictactoe.dto.response.room;

import java.time.Instant;
import java.util.List;

public record RoomInfoResponse(
        String roomCode,
        Instant createdAt,
        List<GameSummaryResponse> games) {
}
