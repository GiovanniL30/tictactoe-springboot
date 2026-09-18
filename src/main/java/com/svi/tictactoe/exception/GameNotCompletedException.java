package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class GameNotCompletedException extends ApiException {

    public GameNotCompletedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}
