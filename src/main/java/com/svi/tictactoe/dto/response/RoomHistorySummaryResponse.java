package com.svi.tictactoe.dto.response;

import java.util.List;

public record RoomHistorySummaryResponse(
        String roomCode,
        List<GameHistorySummaryResponse> games) {
}
