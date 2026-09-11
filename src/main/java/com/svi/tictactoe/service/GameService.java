package com.svi.tictactoe.service;


import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.model.Board;

import java.util.Optional;

public interface GameService {

    String createGame();

    Optional<Board> placeMove(String roomCode, AddMoveRequest requestBody);

    void restartGame(String roomCode);

    Board getBoard(String roomCode);

}
