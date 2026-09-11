package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class PlayerAlreadyExistsException extends ApiException {

    public PlayerAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
