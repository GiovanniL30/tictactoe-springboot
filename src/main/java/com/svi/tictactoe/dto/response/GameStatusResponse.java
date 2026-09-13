package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.model.Player;

import java.util.List;
import java.util.UUID;

public record GameStatusResponse(
        Symbol[][] grid,
        List<Player> players,
        String roomCode,
        UUID gameId,
        int round,
        Symbol currentTurn,
        int spectatorCount,
        String message) {

}
