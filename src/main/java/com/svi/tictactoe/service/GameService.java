package com.svi.tictactoe.service;


import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.game.BoardResponse;
import com.svi.tictactoe.dto.response.game.CreateGameResponse;
import com.svi.tictactoe.dto.response.game.GameInfoResponse;
import com.svi.tictactoe.dto.response.game.JoinGameResponse;
import com.svi.tictactoe.dto.response.game.PlayAgainResponse;

import java.util.UUID;

public interface GameService {

    CreateGameResponse createGame(CreateGameRequest requestBody);

    BoardResponse placeMove(UUID gameId, AddMoveRequest requestBody);

    PlayAgainResponse playAgain(String roomCode);

    JoinGameResponse joinGame(String roomCode, JoinGameRequest requestBody);

    GameInfoResponse getGameInfo(String roomCode);

    BoardResponse getBoardStatus(String roomCode);

    GameInfoResponse deleteGame(String roomCode);

}
