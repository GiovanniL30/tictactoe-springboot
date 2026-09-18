package com.svi.tictactoe.dto.response.room;

import com.svi.tictactoe.dto.response.player.PlayerResponse;

import java.time.Instant;
import java.util.UUID;

public record CreateRoomResponse(
        String message,
        String roomCode,
        UUID gameId,
        Instant createdAt,
        PlayerResponse player) {

}
