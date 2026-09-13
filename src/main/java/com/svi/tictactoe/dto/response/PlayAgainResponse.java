package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.model.Player;

import java.util.List;
import java.util.UUID;

public record PlayAgainResponse(
        String message,
        String roomCode,
        UUID gameId,
        int currentRound,
        Symbol currentTurn,
        List<Player> players) {

}
