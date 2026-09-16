package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.dto.response.player.ParticipantResponse;

import java.util.UUID;

public record JoinGameResponse(
        String message,
        UUID gameId,
        ParticipantResponse participant) {

}
