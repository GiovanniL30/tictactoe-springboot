package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.model.Player;

public record CreateGameResponse(
        String message,
        String roomCode,
        Player player) {

}
