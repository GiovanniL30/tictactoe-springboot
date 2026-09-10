package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class PositionAlreadyTakenException extends ApiException {

    public PositionAlreadyTakenException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}
