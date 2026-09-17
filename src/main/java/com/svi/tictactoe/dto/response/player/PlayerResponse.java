package com.svi.tictactoe.dto.response.player;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;

import java.time.Instant;

public record PlayerResponse(
        String playerName,
        int score,
        Symbol symbol,
        PlayerType type,
        Instant joinedAt) {
}
