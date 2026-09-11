package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.model.Player;

import java.util.List;

public record PlayAgainResponse(
        String message,
        int currentRound,
        Symbol currentTurn,
        List<Player> players) {

}
