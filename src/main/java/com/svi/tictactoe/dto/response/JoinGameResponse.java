package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.model.Player;

public record JoinGameResponse(
        String message,
        Player participant) {

}
