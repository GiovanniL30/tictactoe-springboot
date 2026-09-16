package com.svi.tictactoe.dto.response.room;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.svi.tictactoe.constants.GameStatus;

import java.util.UUID;

public record GameSummaryResponse(
        UUID gameId,
        GameStatus status,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String winner) {
}
