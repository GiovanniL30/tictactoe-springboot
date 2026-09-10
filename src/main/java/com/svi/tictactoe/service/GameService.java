package com.svi.tictactoe.service;


import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.constants.Symbol;

import java.util.Optional;

public interface GameService {

    String createGame();
    Optional<Symbol[][]> placeMove(String roomCode, AddMoveRequest requestBody);

}
