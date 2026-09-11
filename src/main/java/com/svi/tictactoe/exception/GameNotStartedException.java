package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class GameNotStartedException extends ApiException {

    public GameNotStartedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
