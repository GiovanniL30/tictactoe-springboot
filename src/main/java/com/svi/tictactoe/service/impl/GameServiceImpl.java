package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.*;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.response.game.*;
import com.svi.tictactoe.engine.GameEngine;
import com.svi.tictactoe.entity.*;
import com.svi.tictactoe.exception.*;
import com.svi.tictactoe.mapper.GameMapper;
import com.svi.tictactoe.mapper.MoveMapper;
import com.svi.tictactoe.mapper.PlayerMapper;
import com.svi.tictactoe.realtime.event.RealtimeEvent;
import com.svi.tictactoe.repository.cassandra.*;
import com.svi.tictactoe.service.GameService;
import com.svi.tictactoe.service.support.GameLookup;
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
    private  final GameLookup gameLookup;
    private final ApplicationEventPublisher eventPublisher;

    public GameServiceImpl(
            RoomRepository roomRepository,
            GameRepository gameRepository,
            GameRoundRepository gameRoundRepository,
            RoomPlayerRepository roomPlayerRepository,
            GameMoveRepository moveRepository,
            PlayerGameSynchronizer playerGameSynchronizer,
            GameEngine gameEngine,
            GameLookup gameLookup,
            ApplicationEventPublisher eventPublisher) {
        this.roomRepository = roomRepository;
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.moveRepository = moveRepository;
        this.playerGameSynchronizer = playerGameSynchronizer;
        this.gameEngine = gameEngine;
        this.gameLookup = gameLookup;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public GameInfoResponse getGame(UUID gameId) {
        GameEntity game = gameLookup.requireGame(gameId);
        GameRoundEntity round = gameLookup.requireRound(game);
        List<RoomPlayerEntity> players = roomPlayerRepository.findAllByRoomCode(game.getRoomCode());
        PlayerMapper.PlayerSummary summary = PlayerMapper.summarize(players);

        return GameMapper.toGameInfoResponse(
                game,
                summary.players(),
                summary.spectatorCount(),
                round.getCreatedAt(),
                round.getEndedAt(),
                SuccessMessage.GAME_INFO_RETRIEVED.getMessage()
        );
    }

    @Override
    public BoardResponse getBoard(UUID gameId) {
        GameEntity game = gameLookup.requireGame(gameId);
        return GameMapper.toBoardResponse(game, SuccessMessage.BOARD_STATUS_RETRIEVED.getMessage());
    }

    @Override
    public GameMovesResponse getMoves(UUID gameId) {
        GameEntity game = gameLookup.requireGame(gameId);
        GameRoundEntity round = gameLookup.requireRound(game);
        List<MoveResponse> moves = moveRepository.findAllByGameId(gameId).stream()
                .sorted(Comparator.comparing(GameMoveEntity::getMoveNo))
                .map(MoveMapper::toMoveResponse)
                .toList();

        return new GameMovesResponse(
                game.getGameId(),
                game.getRoomCode(),
                game.getRoundNo(),
                moves,
                new GameResultResponse(
                        GameStatus.valueOf(game.getStatus()),
                        game.getWinner(),
                        round.getEndedAt()
                )
        );
    }

    @Override
    public BoardResponse placeMove(UUID gameId, AddMoveRequest requestBody) {
        GameEntity game = gameLookup.requireActiveGame(gameId);
        RoomEntity room = gameLookup.requireRoom(game.getRoomCode());
        List<RoomPlayerEntity> players = gameLookup.requireActiveRoomPlayers(game.getRoomCode());

        validateGameInProgress(game);
        Symbol currentTurn = Symbol.fromString(game.getCurrentTurn());
        List<String> board = BoardUtil.mutableBoard(game.getBoard());

        int x = requestBody.x();
        int y = requestBody.y();
        int boardIndex = x * BoardUtil.BOARD_SIZE + y;

        validateMove(requestBody.symbol(), currentTurn, x, y, board, boardIndex);

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

        boolean roundCompleted = false;

        if (gameEngine.hasWinner(board, currentTurn)) {
            awardWin(game, room, movingPlayer);
            roundCompleted = true;
        } else if (isBoardFull(board)) {
            completeRound(game, room, "DRAW");
            roundCompleted = true;
        } else {
            switchTurn(game, currentTurn);
        }

        gameRepository.save(game);
        roomRepository.save(room);

        if (roundCompleted) {
            playerGameSynchronizer.sync(game, players);
        }

        BoardResponse response = GameMapper.toBoardResponse(game, SuccessMessage.MOVE_PLACED.getMessage());
        publishRealtime(game.getGameId().toString(), MessageTopic.MOVE_PLACED, response);

        if (roundCompleted) {
            GameRoundEntity round = gameLookup.requireRound(game);
            PlayerMapper.PlayerSummary summary = PlayerMapper.summarize(players);
            GameInfoResponse completedGame = GameMapper.toGameInfoResponse(
                    game,
                    summary.players(),
                    summary.spectatorCount(),
                    round.getCreatedAt(),
                    round.getEndedAt(),
                    SuccessMessage.GAME_COMPLETED.getMessage()
            );
            publishRealtime(game.getRoomCode(), MessageTopic.GAME_COMPLETED, completedGame);
        }

        return response;
    }

    private void awardWin(GameEntity game, RoomEntity room, RoomPlayerEntity movingPlayer) {
        completeRound(game, room, movingPlayer.getPlayerName());
        movingPlayer.setScore((movingPlayer.getScore() == null ? 0 : movingPlayer.getScore()) + 1);
        roomPlayerRepository.save(movingPlayer);
    }

    private void completeRound(GameEntity game, RoomEntity room, String winner) {
        game.setStatus(GameStatus.COMPLETED.name());
        game.setWinner(winner);
        game.setCurrentTurn(null);
        room.setStatus(GameStatus.COMPLETED.name());
        finishRound(room);
    }

    private boolean isBoardFull(List<String> board) {
        return board.stream().noneMatch(String::isBlank);
    }

    private void switchTurn(GameEntity game, Symbol currentTurn) {
        game.setCurrentTurn(currentTurn == Symbol.X ? Symbol.O.name() : Symbol.X.name());
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
                .orElseThrow(() -> new PlayerNotFoundException(ErrorMessage.MOVING_PLAYER_SYMBOL_NOT_FOUND.format(symbol.name())));
    }

    private void validateMove(Symbol requestedSymbol, Symbol currentTurn, int x, int y, List<String> board, int boardIndex) {
        validatePosition(x, y);
        validateTurn(requestedSymbol, currentTurn);
        validatePositionAvailable(board, boardIndex, x, y);
    }

    private void validateTurn(Symbol requestedSymbol, Symbol currentTurn) {
        if (requestedSymbol != currentTurn) {
            throw new InvalidTurnException(ErrorMessage.INVALID_TURN.format(currentTurn));
        }
    }

    private void validateGameInProgress(GameEntity game) {
        if (GameStatus.WAITING_FOR_PLAYERS.name().equals(game.getStatus())) {
            throw new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage());
        }
        if (GameStatus.COMPLETED.name().equals(game.getStatus())) {
            throw new GameAlreadyFinishedException(ErrorMessage.GAME_ALREADY_FINISHED.getMessage());
        }
    }

    private void validatePosition(int x, int y) {
        if (!gameEngine.isValidPosition(x, y)) {
            throw new InvalidPositionException(ErrorMessage.INVALID_POSITION.format(x, y));
        }
    }

    private void validatePositionAvailable(List<String> board, int boardIndex, int x, int y) {
        if (!board.get(boardIndex).isBlank()) {
            throw new PositionAlreadyTakenException(ErrorMessage.POSITION_ALREADY_TAKEN.format(x, y));
        }
    }

    private <T> void publishRealtime(String destinationId, MessageTopic topic, T payload) {
        eventPublisher.publishEvent(new RealtimeEvent<>(destinationId, topic, payload));
    }
}
