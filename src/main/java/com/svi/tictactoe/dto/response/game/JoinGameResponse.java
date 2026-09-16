package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.dto.response.player.PlayerResponse;

import java.util.UUID;

public record JoinGameResponse(
        String message,
        UUID gameId,
        PlayerResponse player) {

}
