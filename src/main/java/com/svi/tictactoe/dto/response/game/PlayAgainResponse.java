package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.RealtimeResponse;

import java.time.Instant;
import java.util.UUID;

public record PlayAgainResponse(
        String message,
        String roomCode,
        UUID gameId,
        int currentRound,
        Symbol currentTurn,
        Instant createdAt) implements RealtimeResponse {

}
