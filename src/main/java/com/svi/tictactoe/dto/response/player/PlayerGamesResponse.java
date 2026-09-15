package com.svi.tictactoe.dto.response.player;

import java.util.List;

public record PlayerGamesResponse(
        String playerName,
        int totalGames,
        List<PlayerGameSummaryResponse> games) {
}
