package com.svi.tictactoe.model;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.exception.PositionAlreadyTakenException;

import java.util.List;

public class Game {

    private final String roomCode;
    private final List<Player> players;
    private final Board board;
    private int round;

    public Game(String roomCode, List<Player> players, Board board) {
        this.roomCode = roomCode;
        this.players = players;
        this.board = board;
        this.round = 0;
    }

    public synchronized boolean placeMove(Symbol symbol, int x, int y) {
        if (!board.isEmpty(x, y)) {
            throw new PositionAlreadyTakenException(ErrorMessage.POSITION_ALREADY_TAKEN.format(x, y));
        }

        return board.placeSymbol(symbol, x, y);
    }

    public String getRoomCode() {
        return roomCode;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }
}
