package com.svi.tictactoe.dto.response;

import java.util.List;

public record PlayersResponse(
        int totalPlayers,
        List<PlayerSummaryResponse> players) {
}
