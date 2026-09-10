package com.svi.tictactoe.repository.impl;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.exception.GameNotFoundException;
import com.svi.tictactoe.model.Board;
import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.repository.GameRepository;
import com.svi.tictactoe.util.CodeGenerator;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class GameRepositoryImpl implements GameRepository {

    private final List<Game> games = new ArrayList<>();

    @Override
    public String createGame() {
        String roomCode = CodeGenerator.generate();

        games.add(new Game(roomCode, new ArrayList<>(), new Board()));

        return roomCode;
    }

    @Override
    public Optional<Symbol[][]> placeMove(String roomCode, int x, int y, Symbol symbol) {
        Optional<Game> gameOptional = findGame(roomCode);

        if (gameOptional.isEmpty()) {
            throw new GameNotFoundException(ErrorMessage.GAME_NOT_FOUND.format(roomCode));
        }

        Game game = gameOptional.get();

        if (!game.placeMove(symbol, x, y)) {
            return Optional.empty();
        }

        return Optional.of(game.getBoard().getGrid());
    }

    @Override
    public Optional<Game> findGame(String roomCode) {
        return games.stream().filter(game -> game.getRoomCode().equals(roomCode)).findAny();
    }
}
