package com.svi.tictactoe.service;


import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.model.Board;
import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.model.Player;

import java.util.Optional;

public interface GameService {

    Game createGame(CreateGameRequest requestBody);

    Optional<Board> placeMove(String roomCode, AddMoveRequest requestBody);

    Game playAgain(String roomCode);

    Player joinGame(String roomCode, JoinGameRequest requestBody);

    Board getBoard(String roomCode);

    Game getGame(String roomCode);

}
