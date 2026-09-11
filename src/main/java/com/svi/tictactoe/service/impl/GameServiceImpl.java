package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.exception.GameNotFoundException;
import com.svi.tictactoe.model.Board;
import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.model.Player;
import com.svi.tictactoe.repository.GameRepository;
import com.svi.tictactoe.service.GameService;
import com.svi.tictactoe.util.CodeGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game createGame(CreateGameRequest requestBody) {
        Game game = new Game(generateUniqueRoomCode(), new ArrayList<>(), new Board());
        game.join(requestBody.getPlayerName());
        gameRepository.save(game);
        return game;
    }

    @Override
    public Optional<Board> placeMove(String roomCode, AddMoveRequest requestBody) {
        Game game = requireGame(roomCode);

        if (!game.placeMove(requestBody.getSymbol(), requestBody.getX(), requestBody.getY())) {
            return Optional.empty();
        }

        gameRepository.save(game);
        return Optional.of(game.getBoard());
    }

    @Override
    public Game playAgain(String roomCode) {
        Game game = requireGame(roomCode);
        game.startNextRound();
        gameRepository.save(game);
        return game;
    }

    @Override
    public Player joinGame(String roomCode, JoinGameRequest requestBody) {
        Game game = requireGame(roomCode);
        Player participant = game.join(requestBody.getPlayerName());
        gameRepository.save(game);
        return participant;
    }

    @Override
    public Board getBoard(String roomCode) {
        return requireGame(roomCode).getBoard();
    }

    @Override
    public Game getGame(String roomCode) {
        return requireGame(roomCode);
    }

    @Override
    public Game deleteGame(String roomCode) {
        requireGame(roomCode);

        return gameRepository.delete(roomCode);
    }

    private Game requireGame(String roomCode) {
        return gameRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new GameNotFoundException(
                        ErrorMessage.GAME_NOT_FOUND.format(roomCode)
                ));
    }

    private String generateUniqueRoomCode() {
        String roomCode;

        do {
            roomCode = CodeGenerator.generate();
        } while (gameRepository.findByRoomCode(roomCode).isPresent());

        return roomCode;
    }
}
