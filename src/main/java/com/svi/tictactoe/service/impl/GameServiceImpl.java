package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.*;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.response.game.*;
import com.svi.tictactoe.engine.GameEngine;
import com.svi.tictactoe.entity.*;
import com.svi.tictactoe.exception.*;
import com.svi.tictactoe.mapper.GameMapper;
import com.svi.tictactoe.mapper.PlayerMapper;
import com.svi.tictactoe.realtime.event.RealtimeEvent;
import com.svi.tictactoe.repository.cassandra.*;
import com.svi.tictactoe.service.GameService;
import com.svi.tictactoe.service.support.PlayerGameSynchronizer;
import com.svi.tictactoe.util.BoardUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class GameServiceImpl implements GameService {

    private final RoomRepository roomRepository;
    private final GameRepository gameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final GameMoveRepository moveRepository;
    private final PlayerGameSynchronizer playerGameSynchronizer;
    private final GameEngine gameEngine;
    private final ApplicationEventPublisher eventPublisher;

    public GameServiceImpl(
            RoomRepository roomRepository,
            GameRepository gameRepository,
            GameRoundRepository gameRoundRepository,
            RoomPlayerRepository roomPlayerRepository,
            GameMoveRepository moveRepository,
            PlayerGameSynchronizer playerGameSynchronizer,
            GameEngine gameEngine,
            ApplicationEventPublisher eventPublisher) {
        this.roomRepository = roomRepository;
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.moveRepository = moveRepository;
        this.playerGameSynchronizer = playerGameSynchronizer;
        this.gameEngine = gameEngine;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public GameInfoResponse getGame(UUID gameId) {
        GameEntity game = requireGame(gameId);
        List<RoomPlayerEntity> players = roomPlayerRepository.findAllByRoomCode(game.getRoomCode());
        return toGameInfoResponse(game, players, SuccessMessage.GAME_INFO_RETRIEVED.getMessage());
    }

    @Override
    public BoardResponse getBoard(UUID gameId) {
        GameEntity game = requireGame(gameId);
        return GameMapper.toBoardResponse(game, SuccessMessage.BOARD_STATUS_RETRIEVED.getMessage());
    }

    @Override
    public GameMovesResponse getMoves(UUID gameId) {
        GameEntity game = requireGame(gameId);
        List<MoveResponse> moves = moveRepository.findAllByGameId(gameId).stream()
                .sorted(Comparator.comparing(GameMoveEntity::getMoveNo))
                .map(this::toMoveResponse)
                .toList();

        return new GameMovesResponse(
                game.getGameId(),
                game.getRoomCode(),
                game.getRoundNo(),
                moves,
                new GameResultResponse(GameStatus.valueOf(game.getStatus()), game.getWinner())
        );
    }

    @Override
    public BoardResponse placeMove(UUID gameId, AddMoveRequest requestBody) {
        GameEntity game = requireActiveGame(gameId);
        List<RoomPlayerEntity> players = roomPlayerRepository.findAllByRoomCode(game.getRoomCode());
        requireActiveRoom(players);

        if (GameStatus.WAITING_FOR_PLAYERS.name().equals(game.getStatus())) {
            throw new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage());
        }

        if (GameStatus.COMPLETED.name().equals(game.getStatus())) {
            throw new GameAlreadyFinishedException(ErrorMessage.GAME_ALREADY_FINISHED.getMessage());
        }

        int x = requestBody.x();
        int y = requestBody.y();

        if (!gameEngine.isValidPosition(x, y)) {
            throw new InvalidPositionException(ErrorMessage.INVALID_POSITION.format(x, y));
        }

        Symbol currentTurn = Symbol.fromString(game.getCurrentTurn());
        if (requestBody.symbol() != currentTurn) {
            throw new InvalidTurnException(ErrorMessage.INVALID_TURN.format(currentTurn));
        }

        List<String> board = BoardUtil.mutableBoard(game.getBoard());
        int boardIndex = x * BoardUtil.BOARD_SIZE + y;
        if (!board.get(boardIndex).isBlank()) {
            throw new PositionAlreadyTakenException(ErrorMessage.POSITION_ALREADY_TAKEN.format(x, y));
        }

        board.set(boardIndex, currentTurn.name());
        game.setBoard(board);
        int moveNumber = game.getMoveCount() == null ? 1 : game.getMoveCount() + 1;
        game.setMoveCount(moveNumber);

        RoomPlayerEntity movingPlayer = findPlayerBySymbol(players, currentTurn);

        moveRepository.save(new GameMoveEntity(
                gameId,
                moveNumber,
                game.getRoomCode(),
                movingPlayer.getPlayerName(),
                currentTurn.name(),
                x,
                y,
                Instant.now()
        ));

        RoomEntity room = requireRoom(game.getRoomCode());
        boolean roundCompleted = false;

        if (gameEngine.hasWinner(board, currentTurn)) {
            game.setStatus(GameStatus.COMPLETED.name());
            game.setWinner(movingPlayer.getPlayerName());
            game.setCurrentTurn(null);
            room.setStatus(GameStatus.COMPLETED.name());
            movingPlayer.setScore((movingPlayer.getScore() == null ? 0 : movingPlayer.getScore()) + 1);
            roomPlayerRepository.save(movingPlayer);
            finishRound(room);
            roundCompleted = true;
        } else if (board.stream().noneMatch(String::isBlank)) {
            game.setStatus(GameStatus.COMPLETED.name());
            game.setWinner("DRAW");
            game.setCurrentTurn(null);
            room.setStatus(GameStatus.COMPLETED.name());
            finishRound(room);
            roundCompleted = true;
        } else {
            game.setCurrentTurn(currentTurn == Symbol.X ? Symbol.O.name() : Symbol.X.name());
        }

        gameRepository.save(game);
        roomRepository.save(room);

        if (roundCompleted) {
            playerGameSynchronizer.sync(game, players);
        }

        BoardResponse response = GameMapper.toBoardResponse(game, SuccessMessage.MOVE_PLACED.getMessage());
        publishRealtime(game.getGameId().toString(), MessageTopic.MOVE_PLACED, response);

        if (roundCompleted) {
            GameInfoResponse completedGame = toGameInfoResponse(
                    game,
                    players,
                    SuccessMessage.GAME_COMPLETED.getMessage()
            );
            publishRealtime(game.getRoomCode(), MessageTopic.GAME_COMPLETED, completedGame);
        }

        return response;
    }

    private GameInfoResponse toGameInfoResponse(GameEntity game, List<RoomPlayerEntity> players, String message) {
        PlayerMapper.PlayerSummary summary = PlayerMapper.summarize(players);
        return GameMapper.toGameInfoResponse(game, summary.players(), summary.spectatorCount(), message);
    }

    private RoomEntity requireRoom(String roomCode) {
        return roomRepository.findById(roomCode)
                .orElseThrow(() -> new GameNotFoundException(ErrorMessage.GAME_NOT_FOUND.format(roomCode)));
    }

    private GameEntity requireGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(ErrorMessage.GAME_ID_NOT_FOUND.format(gameId)));
    }

    private GameEntity requireActiveGame(UUID gameId) {
        GameEntity game = requireGame(gameId);
        RoomEntity room = requireRoom(game.getRoomCode());

        if (!room.getActiveGameId().equals(gameId)) {
            throw new GameNotFoundException(ErrorMessage.GAME_ID_NOT_FOUND.format(gameId));
        }

        return game;
    }

    private void finishRound(RoomEntity room) {
        gameRoundRepository.findByRoomCodeAndRoundNo(room.getRoomCode(), room.getCurrentRound())
                .ifPresent(round -> {
                    round.setStatus(GameStatus.COMPLETED.name());
                    round.setEndedAt(Instant.now());
                    gameRoundRepository.save(round);
                });
    }

    private RoomPlayerEntity findPlayerBySymbol(List<RoomPlayerEntity> players, Symbol symbol) {
        return players.stream()
                .filter(player -> symbol.name().equals(player.getSymbol()))
                .findFirst()
                .orElseThrow(() -> new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage()));
    }

    private void requireActiveRoom(List<RoomPlayerEntity> players) {
        boolean hasInactivePlayer = players.stream()
                .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                .anyMatch(player -> !player.isActive());

        if (hasInactivePlayer) {
            throw new RoomInactiveException(ErrorMessage.ROOM_INACTIVE.getMessage());
        }
    }

    private MoveResponse toMoveResponse(GameMoveEntity move) {
        return new MoveResponse(
                move.getMoveNo(),
                move.getPlayerName(),
                Symbol.fromString(move.getSymbol()),
                move.getX(),
                move.getY(),
                move.getPlayedAt()
        );
    }

    private <T> void publishRealtime(String destinationId, MessageTopic topic, T payload) {
        eventPublisher.publishEvent(new RealtimeEvent<>(destinationId, topic, payload));
    }
}
