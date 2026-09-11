package com.svi.tictactoe.model;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.exception.GameNotStartedException;
import com.svi.tictactoe.exception.InvalidPositionException;
import com.svi.tictactoe.exception.InvalidTurnException;
import com.svi.tictactoe.exception.PlayerAlreadyExistsException;
import com.svi.tictactoe.exception.PositionAlreadyTakenException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Game {

    private static final int REQUIRED_PLAYER_COUNT = 2;

    private final String roomCode;
    private final List<Player> players;
    private final List<Player> spectators;
    private final Board board;
    private int round;
    private Symbol currentTurn;

    public Game(String roomCode, List<Player> players, Board board) {
        this.roomCode = roomCode;
        this.players = players;
        this.spectators = new ArrayList<>();
        this.board = board;
        this.round = 1;
        this.currentTurn = Symbol.X;
    }

    public synchronized void placeMove(Symbol symbol, int x, int y) {
        if (!isStarted()) {
            throw new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage());
        }

        if (symbol != currentTurn) {
            throw new InvalidTurnException(ErrorMessage.INVALID_TURN.format(currentTurn));
        }

        if (!board.isValidPosition(x, y)) {
            throw new InvalidPositionException(ErrorMessage.INVALID_POSITION.format(x, y));
        }

        if (!board.isEmpty(x, y)) {
            throw new PositionAlreadyTakenException(ErrorMessage.POSITION_ALREADY_TAKEN.format(x, y));
        }

        board.placeSymbol(symbol, x, y);
        currentTurn = currentTurn == Symbol.X ? Symbol.O : Symbol.X;
    }

    public synchronized void startNextRound() {
        if (!isStarted()) {
            throw new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage());
        }

        board.reset();
        round++;
        currentTurn = Symbol.X;
    }

    public synchronized Player join(String playerName) {
        if (isNameTaken(playerName)) {
            throw new PlayerAlreadyExistsException(ErrorMessage.PLAYER_ALREADY_EXISTS.format(playerName));
        }

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

    public Symbol getCurrentTurn() {
        return currentTurn;
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

    private boolean isNameTaken(String playerName) {
        String normalizedName = normalizeName(playerName);

        return players.stream()
                .anyMatch(player -> normalizeName(player.getPlayerName()).equals(normalizedName))
                || spectators.stream().anyMatch(spectator -> normalizeName(spectator.getPlayerName()).equals(normalizedName));
    }

    private String normalizeName(String playerName) {
        return playerName.trim().toLowerCase(Locale.ROOT);
    }


}
