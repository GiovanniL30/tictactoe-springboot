package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;

public class RoomInactiveException extends ApiException {

    public RoomInactiveException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
