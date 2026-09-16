package com.svi.tictactoe.dto.response.room;

import java.util.List;

public record RoomInfoResponse(
        String roomCode,
        List<GameSummaryResponse> games) {
}
