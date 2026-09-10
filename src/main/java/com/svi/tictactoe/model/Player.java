package com.svi.tictactoe.model;

import com.svi.tictactoe.constants.Symbol;

public class Player {

    private final String playerName;
    private final Symbol symbol;
    private int score;

    public Player(String playerName, Symbol symbol) {
        this.playerName = playerName;
        this.score = 0;
        this.symbol = symbol;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Symbol getSymbol() {
        return symbol;
    }
}
