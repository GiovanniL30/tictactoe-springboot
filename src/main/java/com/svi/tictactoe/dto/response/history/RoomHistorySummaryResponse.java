package com.svi.tictactoe.dto.response.history;

import java.util.List;

public record RoomHistorySummaryResponse(
        String roomCode,
        List<GameHistorySummaryResponse> games) {
}
