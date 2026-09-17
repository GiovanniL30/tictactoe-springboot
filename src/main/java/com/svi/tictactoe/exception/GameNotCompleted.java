package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class GameNotCompleted extends ApiException {

    public GameNotCompleted(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}
