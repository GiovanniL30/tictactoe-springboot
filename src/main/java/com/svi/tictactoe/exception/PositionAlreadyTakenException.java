package com.svi.tictactoe.exception;

public class PositionAlreadyTakenException extends RuntimeException {

    public PositionAlreadyTakenException(String message) {
        super(message);
    }

}
