package com.svi.tictactoe.service;


import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.response.game.BoardResponse;
import com.svi.tictactoe.dto.response.game.GameInfoResponse;
import com.svi.tictactoe.dto.response.game.GameMovesResponse;

import java.util.UUID;

public interface GameService {

    GameInfoResponse getGame(UUID gameId);

    BoardResponse getBoard(UUID gameId);

    GameMovesResponse getMoves(UUID gameId);

    BoardResponse placeMove(UUID gameId, AddMoveRequest requestBody);

}
