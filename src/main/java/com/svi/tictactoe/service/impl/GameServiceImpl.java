package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.SuccessMessage;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.BoardResponse;
import com.svi.tictactoe.dto.response.CreateGameResponse;
import com.svi.tictactoe.dto.response.GameStatusResponse;
import com.svi.tictactoe.dto.response.JoinGameResponse;
import com.svi.tictactoe.dto.response.PlayAgainResponse;
import com.svi.tictactoe.exception.GameNotFoundException;
import com.svi.tictactoe.model.Board;
import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.model.Player;
import com.svi.tictactoe.repository.GameRepository;
import com.svi.tictactoe.service.GameService;
import com.svi.tictactoe.util.CodeGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public CreateGameResponse createGame(CreateGameRequest requestBody) {
        Game game = new Game(generateUniqueRoomCode(), new ArrayList<>(), new Board());
        game.join(requestBody.playerName());
        gameRepository.save(game);

        return new CreateGameResponse(
                SuccessMessage.GAME_CREATED.getMessage(),
                game.getRoomCode(),
                game.getActiveGameId(),
                game.getPlayers().getFirst()
        );
    }

    @Override
    public BoardResponse placeMove(UUID gameId, AddMoveRequest requestBody) {
        Game game = requireGame(gameId);
        game.placeMove(requestBody.symbol(), requestBody.x(), requestBody.y());
        gameRepository.save(game);

        return new BoardResponse(
                SuccessMessage.MOVE_PLACED.getMessage(),
                game.getActiveGameId(),
                game.getBoard().getGrid(),
                game.getCurrentTurn()
        );
    }

    @Override
    public PlayAgainResponse playAgain(String roomCode) {
        Game game = requireGame(roomCode);
        game.startNextRound();
        gameRepository.save(game);

        return new PlayAgainResponse(
                SuccessMessage.NEW_ROUND_STARTED.getMessage(),
                game.getRoomCode(),
                game.getActiveGameId(),
                game.getRound(),
                game.getCurrentTurn()
        );
    }

    @Override
    public JoinGameResponse joinGame(String roomCode, JoinGameRequest requestBody) {
        Game game = requireGame(roomCode);
        Player participant = game.join(requestBody.playerName());
        gameRepository.save(game);

        String message = participant.getType() == PlayerType.PLAYER
                ? SuccessMessage.PLAYER_JOINED.getMessage()
                : SuccessMessage.SPECTATOR_JOINED.getMessage();

        return new JoinGameResponse(message, participant);
    }

    @Override
    public GameStatusResponse getGameStatus(String roomCode) {
        return toGameStatusResponse(
                requireGame(roomCode),
                SuccessMessage.GAME_STATUS_RETRIEVED.getMessage()
        );
    }

    @Override
    public BoardResponse getBoardStatus(String roomCode) {
        Game game = requireGame(roomCode);

        return new BoardResponse(
                SuccessMessage.BOARD_STATUS_RETRIEVED.getMessage(),
                game.getActiveGameId(),
                game.getBoard().getGrid(),
                game.getCurrentTurn()
        );
    }

    @Override
    public GameStatusResponse deleteGame(String roomCode) {
        requireGame(roomCode);
        Game deletedGame = gameRepository.delete(roomCode);

        return toGameStatusResponse(deletedGame, SuccessMessage.GAME_DELETED.getMessage());
    }

    private Game requireGame(String roomCode) {
        return gameRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new GameNotFoundException(
                        ErrorMessage.GAME_NOT_FOUND.format(roomCode)
                ));
    }

    private Game requireGame(UUID gameId) {
        return gameRepository.findByActiveGameId(gameId)
                .orElseThrow(() -> new GameNotFoundException(
                        ErrorMessage.GAME_ID_NOT_FOUND.format(gameId)
                ));
    }

    private String generateUniqueRoomCode() {
        String roomCode;

        do {
            roomCode = CodeGenerator.generate();
        } while (gameRepository.findByRoomCode(roomCode).isPresent());

        return roomCode;
    }

    private GameStatusResponse toGameStatusResponse(Game game, String message) {
        return new GameStatusResponse(
                game.getBoard().getGrid(),
                game.getPlayers(),
                game.getRoomCode(),
                game.getActiveGameId(),
                game.getRound(),
                game.getCurrentTurn(),
                game.getSpectators().size(),
                message
        );
    }
}
