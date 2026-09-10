package com.svi.tictactoe.dto.request;

import com.svi.tictactoe.constants.Symbol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public class AddMoveRequest {

    @NotNull(message = "x is required.")
    @Range(min = 0, max = 2, message = "x must be between 0 and 2.")
    private Integer x;

    @NotNull(message = "y is required.")
    @Range(min = 0, max = 2, message = "y must be between 0 and 2.")
    private Integer y;

    @NotNull(message = "symbol is required.")
    private Symbol symbol;

    @NotBlank(message = "playerName is required.")
    private String playerName;

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
