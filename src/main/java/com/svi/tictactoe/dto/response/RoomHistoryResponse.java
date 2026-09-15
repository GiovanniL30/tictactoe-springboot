package com.svi.tictactoe.dto.response;

import java.util.List;

public record RoomHistoryResponse(
        String roomCode,
        List<GameInfoResponse> games) {
}
