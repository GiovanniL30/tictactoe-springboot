package com.svi.tictactoe.model;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.exception.GameNotStartedException;
import com.svi.tictactoe.exception.PositionAlreadyTakenException;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private static final int REQUIRED_PLAYER_COUNT = 2;

    private final String roomCode;
    private final List<Player> players;
    private final List<Player> spectators;
    private final Board board;
    private int round;

    public Game(String roomCode, List<Player> players, Board board) {
        this.roomCode = roomCode;
        this.players = players;
        this.spectators = new ArrayList<>();
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
        return List.copyOf(players);
    }

    public List<Player> getSpectators() {
        return List.copyOf(spectators);
    }

    public Board getBoard() {
        return board;
    }

    public synchronized void startNextRound() {
        if (!isStarted()) {
            throw new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage());
        }

        board.reset();
        round++;
    }

    public synchronized Player join(String playerName) {
        if (isStarted()) {
            Player spectator = new Player(playerName, null, PlayerType.SPECTATOR);
            spectators.add(spectator);
            return spectator;
        }

        Symbol symbol = players.isEmpty() ? Symbol.X : Symbol.O;
        Player player = new Player(playerName, symbol, PlayerType.PLAYER);
        players.add(player);
        return player;
    }

    public synchronized boolean isStarted() {
        return players.size() == REQUIRED_PLAYER_COUNT;
    }

    public int getRound() {
        return round;
    }

}
