package com.svi.tictactoe.dto.response.room;

import java.util.List;

public record RoomsResponse(
        int totalRooms,
        int totalGames,
        List<RoomInfoResponse> rooms) {
}
