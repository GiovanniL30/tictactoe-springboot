package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;

public record ParticipantResponse(
        String playerName,
        int score,
        Symbol symbol,
        PlayerType type) {
}
