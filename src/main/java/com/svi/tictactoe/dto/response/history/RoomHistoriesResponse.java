package com.svi.tictactoe.dto.response.history;

import java.util.List;

public record RoomHistoriesResponse(
        int totalRooms,
        int totalGames,
        List<RoomHistorySummaryResponse> rooms) {
}
