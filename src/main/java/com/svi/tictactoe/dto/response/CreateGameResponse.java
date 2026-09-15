package com.svi.tictactoe.dto.response;

import java.util.UUID;

public record CreateGameResponse(
        String message,
        String roomCode,
        UUID gameId,
        ParticipantResponse player) {

}
