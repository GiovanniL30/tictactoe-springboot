package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class InvalidPositionException extends ApiException {

    public InvalidPositionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
