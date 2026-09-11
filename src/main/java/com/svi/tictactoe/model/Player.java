package com.svi.tictactoe.model;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;

public class Player {

    private final String playerName;
    private final Symbol symbol;
    private final PlayerType type;
    private int score;

    public Player(String playerName, Symbol symbol) {
        this(playerName, symbol, PlayerType.PLAYER);
    }

    public Player(String playerName, Symbol symbol, PlayerType type) {
        this.playerName = playerName;
        this.score = 0;
        this.symbol = symbol;
        this.type = type;
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

    public PlayerType getType() {
        return type;
    }
}
