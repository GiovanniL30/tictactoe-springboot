package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class PlayerNotFoundException extends ApiException {

    public PlayerNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
