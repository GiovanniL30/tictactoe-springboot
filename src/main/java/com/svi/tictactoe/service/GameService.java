package com.svi.tictactoe.service;


import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.BoardResponse;
import com.svi.tictactoe.dto.response.CreateGameResponse;
import com.svi.tictactoe.dto.response.GameStatusResponse;
import com.svi.tictactoe.dto.response.JoinGameResponse;
import com.svi.tictactoe.dto.response.PlayAgainResponse;

public interface GameService {

    CreateGameResponse createGame(CreateGameRequest requestBody);

    BoardResponse placeMove(String roomCode, AddMoveRequest requestBody);

    PlayAgainResponse playAgain(String roomCode);

    JoinGameResponse joinGame(String roomCode, JoinGameRequest requestBody);

    GameStatusResponse getGameStatus(String roomCode);

    BoardResponse getBoardStatus(String roomCode);

    GameStatusResponse deleteGame(String roomCode);

}
