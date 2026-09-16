package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.dto.response.player.PlayerResponse;

import java.util.UUID;

public record CreateGameResponse(
        String message,
        String roomCode,
        UUID gameId,
        PlayerResponse player) {

}
