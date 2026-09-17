package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.dto.response.player.PlayerResponse;

import java.time.Instant;
import java.util.UUID;

public record CreateGameResponse(
        String message,
        String roomCode,
        UUID gameId,
        Instant createdAt,
        PlayerResponse player) {

}
