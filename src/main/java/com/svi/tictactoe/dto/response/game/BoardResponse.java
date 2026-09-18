package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.realtime.event.RealtimePayload;

import java.util.UUID;

public record BoardResponse(
        String message,
        UUID gameId,
        Symbol[][] grid,
        Symbol currentTurn,
        GameStatus status) implements RealtimePayload {
}
