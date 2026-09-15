package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.dto.response.player.ParticipantResponse;

import java.util.UUID;

public record CreateGameResponse(
        String message,
        String roomCode,
        UUID gameId,
        ParticipantResponse player) {

}
