package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.model.Board;
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
    public String createGame() {
        return gameRepository.createGame();
    }

    @Override
    public Optional<Board> placeMove(String roomCode, AddMoveRequest requestBody) {
        return gameRepository.placeMove(roomCode, requestBody.getX(), requestBody.getY(), requestBody.getSymbol());
    }

    @Override
    public void restartGame(String roomCode) {
        gameRepository.restartGame(roomCode);
    }

    @Override
    public Board getBoard(String roomCode) {
        return gameRepository.getBoard(roomCode);
    }
}
