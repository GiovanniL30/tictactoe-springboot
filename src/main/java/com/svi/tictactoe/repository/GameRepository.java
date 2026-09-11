package com.svi.tictactoe.repository;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.model.Board;
import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.model.Player;

import java.util.Optional;

public interface GameRepository {

    Game createGame(String playerName);

    Optional<Board> placeMove(String roomCode, int x, int y, Symbol symbol);

    Board getBoard(String roomCode);

    Game getGame(String roomCode);

    Game playAgain(String roomCode);

    Player joinGame(String roomCode, String playerName);

    Optional<Game> findGame(String roomCode);

}
