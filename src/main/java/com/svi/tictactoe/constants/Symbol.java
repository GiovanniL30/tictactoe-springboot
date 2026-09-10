package com.svi.tictactoe.constants;

public enum Symbol {
    X, O;

    public static Symbol fromString(String value) {
        return switch (value.toUpperCase()) {
            case "X" -> X;
            case "O" -> O;
            default -> throw new IllegalArgumentException("Invalid symbol: " + value);
        };
    }
}