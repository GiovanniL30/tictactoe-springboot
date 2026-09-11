package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class InvalidTurnException extends ApiException {

    public InvalidTurnException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
