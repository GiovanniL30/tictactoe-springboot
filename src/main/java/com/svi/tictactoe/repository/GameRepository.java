package com.svi.tictactoe.repository;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.model.Board;
import com.svi.tictactoe.model.Game;

import java.util.Optional;

public interface GameRepository {

    String createGame();
    Optional<Board> placeMove(String roomCode, int x, int y, Symbol symbol);
    Board getBoard(String roomCode);
    Optional<Game> findGame(String roomCode);

}
