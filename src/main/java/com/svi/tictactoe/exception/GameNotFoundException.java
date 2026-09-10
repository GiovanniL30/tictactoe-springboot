package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class GameNotFoundException extends ApiException {

    public GameNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

}
