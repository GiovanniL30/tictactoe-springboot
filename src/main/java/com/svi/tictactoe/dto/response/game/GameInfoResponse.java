package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.player.PlayerResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GameInfoResponse(
        List<PlayerResponse> players,
        String roomCode,
        UUID gameId,
        int round,
        Symbol currentTurn,
        int spectatorCount,
        GameStatus status,
        String winner,
        Instant createdAt,
        Instant endedAt,
        String message) {
}
