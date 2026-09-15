package com.svi.tictactoe.dto.response;

import java.util.List;

public record RoomHistoriesResponse(
        int totalRooms,
        int totalGames,
        List<RoomHistorySummaryResponse> rooms) {
}
