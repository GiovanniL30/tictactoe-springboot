package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.model.Player;

import java.util.UUID;

public record CreateGameResponse(
        String message,
        String roomCode,
        UUID gameId,
        Player player) {

}
