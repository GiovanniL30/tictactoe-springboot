package com.svi.tictactoe.dto.response;

import com.svi.tictactoe.model.Player;

public class JoinGameResponse {

    private final String message;
    private final Player participant;

    public JoinGameResponse(String message, Player participant) {
        this.message = message;
        this.participant = participant;
    }

    public String getMessage() {
        return message;
    }

    public Player getParticipant() {
        return participant;
    }
}
