package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.model.Board;
import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.model.Player;
import com.svi.tictactoe.repository.GameRepository;
import com.svi.tictactoe.service.GameService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game createGame(CreateGameRequest requestBody) {
        return gameRepository.createGame(requestBody.getPlayerName());
    }

    @Override
    public Optional<Board> placeMove(String roomCode, AddMoveRequest requestBody) {
        return gameRepository.placeMove(roomCode, requestBody.getX(), requestBody.getY(), requestBody.getSymbol());
    }

    @Override
    public Game playAgain(String roomCode) {
        return gameRepository.playAgain(roomCode);
    }

    @Override
    public Player joinGame(String roomCode, JoinGameRequest requestBody) {
        return gameRepository.joinGame(roomCode, requestBody.getPlayerName());
    }

    @Override
    public Board getBoard(String roomCode) {
        return gameRepository.getBoard(roomCode);
    }

    @Override
    public Game getGame(String roomCode) {
        return gameRepository.getGame(roomCode);
    }
}
