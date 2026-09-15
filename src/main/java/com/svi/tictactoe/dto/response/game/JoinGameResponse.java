package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.dto.response.player.ParticipantResponse;

public record JoinGameResponse(
        String message,
        ParticipantResponse participant) {

}
