package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.Symbol;

import java.util.List;
import java.util.UUID;

public record GameInfoResponse(
        List<ParticipantResponse> players,
        String roomCode,
        UUID gameId,
        int round,
        Symbol currentTurn,
        int spectatorCount,
        GameStatus status,
        String winner,
        String message) {
}
