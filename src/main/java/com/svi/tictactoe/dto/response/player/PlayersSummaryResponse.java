package com.svi.tictactoe.dto.response.player;

import java.util.List;

public record PlayersSummaryResponse(
        List<PlayerResponse> players,
        int spectatorCount) {
}
