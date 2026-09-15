package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class GameAlreadyFinishedException extends ApiException {

    public GameAlreadyFinishedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
