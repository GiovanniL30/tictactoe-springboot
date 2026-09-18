package com.svi.tictactoe.dto.response.room;

import com.svi.tictactoe.dto.response.player.PlayerResponse;

import java.util.UUID;

public record JoinRoomResponse(
        String message,
        UUID gameId,
        PlayerResponse player) {

}
