package com.svi.tictactoe.dto.response.room;

import com.svi.tictactoe.dto.response.RealtimeResponse;

import java.time.Instant;
import java.util.List;

public record RoomInfoResponse(
        String roomCode,
        Instant createdAt,
        List<GameSummaryResponse> games) implements RealtimeResponse {
}
