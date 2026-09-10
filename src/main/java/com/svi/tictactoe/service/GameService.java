package com.svi.tictactoe.service;


import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.model.Game;

public interface GameService {

    String createGame();
    boolean placeMove(String roomCode, AddMoveRequest requestBody);

}
