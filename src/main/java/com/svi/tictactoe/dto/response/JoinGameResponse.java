package com.svi.tictactoe.dto.response;

public record JoinGameResponse(
        String message,
        ParticipantResponse participant) {

}
