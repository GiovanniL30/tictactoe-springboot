package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.*;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.game.*;
import com.svi.tictactoe.engine.GameEngine;
import com.svi.tictactoe.entity.*;
import com.svi.tictactoe.exception.*;
import com.svi.tictactoe.mapper.GameMapper;
import com.svi.tictactoe.mapper.PlayerMapper;
import com.svi.tictactoe.mapper.RoomMapper;
import com.svi.tictactoe.realtime.event.RealtimeEvent;
import com.svi.tictactoe.repository.cassandra.*;
import com.svi.tictactoe.service.GameService;
import com.svi.tictactoe.util.BoardUtil;
import com.svi.tictactoe.util.CodeGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.svi.tictactoe.util.PlayerNameUtil.normalize;

@Service
public class GameServiceImpl implements GameService {

    private static final int REQUIRED_PLAYER_COUNT = 2;

    private final RoomRepository roomRepository;
    private final RoomCatalogRepository roomCatalogRepository;
    private final GameRepository gameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final ParticipantRepository participantRepository;
    private final GameMoveRepository moveRepository;
    private final PlayerCatalogRepository playerCatalogRepository;
    private final PlayerGameRepository playerGameRepository;
    private final GameEngine gameEngine;
    private final ApplicationEventPublisher eventPublisher;

    public GameServiceImpl(
            RoomRepository roomRepository,
            RoomCatalogRepository roomCatalogRepository,
            GameRepository gameRepository,
            GameRoundRepository gameRoundRepository,
            ParticipantRepository participantRepository,
            GameMoveRepository moveRepository,
            PlayerCatalogRepository playerCatalogRepository,
            PlayerGameRepository playerGameRepository,
            GameEngine gameEngine,
            ApplicationEventPublisher eventPublisher) {
        this.roomRepository = roomRepository;
        this.roomCatalogRepository = roomCatalogRepository;
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.participantRepository = participantRepository;
        this.moveRepository = moveRepository;
        this.playerCatalogRepository = playerCatalogRepository;
        this.playerGameRepository = playerGameRepository;
        this.gameEngine = gameEngine;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CreateGameResponse createGame(CreateGameRequest requestBody) {
        String roomCode = generateUniqueRoomCode();
        UUID gameId = UUID.randomUUID();
        Instant now = Instant.now();

        RoomEntity room = new RoomEntity(roomCode, gameId, 1, GameStatus.WAITING_FOR_PLAYERS.name(), now);

        GameEntity game = new GameEntity(
                gameId,
                roomCode,
                1,
                GameStatus.WAITING_FOR_PLAYERS.name(),
                Symbol.X.name(),
                null,
                0,
                GameMapper.emptyBoard()
        );

        GameRoundEntity gameRound = new GameRoundEntity(roomCode, 1, gameId, GameStatus.WAITING_FOR_PLAYERS.name(), now, null);

        ParticipantEntity creator = new ParticipantEntity(
                roomCode,
                normalize(requestBody.playerName()),
                requestBody.playerName(),
                PlayerType.PLAYER.name(),
                Symbol.X.name(),
                0,
                now
        );

        roomRepository.save(room);
        roomCatalogRepository.save(new RoomCatalogEntity(RoomCatalogEntity.ALL_ROOMS, roomCode));
        gameRepository.save(game);
        gameRoundRepository.save(gameRound);
        participantRepository.save(creator);
        syncGameForPlayers(game, List.of(creator));

        return new CreateGameResponse(
                SuccessMessage.GAME_CREATED.getMessage(),
                roomCode,
                gameId,
                PlayerMapper.toParticipantResponse(creator)
        );
    }

    @Override
    public BoardResponse placeMove(UUID gameId, AddMoveRequest requestBody) {
        GameEntity game = requireActiveGame(gameId);

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

        List<ParticipantEntity> participants = participantRepository.findAllByRoomCode(game.getRoomCode());
        ParticipantEntity movingPlayer = findPlayerBySymbol(participants, currentTurn);

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
            participantRepository.save(movingPlayer);
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
            syncGameForPlayers(game, participants);
        }

        BoardResponse response = GameMapper.toBoardResponse(game, SuccessMessage.MOVE_PLACED.getMessage());
        publishRealtime(game.getGameId().toString(), MessageTopic.MOVE_PLACED, response);

        if (roundCompleted) {
            GameInfoResponse completedGame = toGameInfoResponse(
                    game,
                    participants,
                    SuccessMessage.GAME_COMPLETED.getMessage()
            );
            publishRealtime(game.getRoomCode(), MessageTopic.GAME_COMPLETED, completedGame);
        }

        return response;
    }

    @Override
    public PlayAgainResponse playAgain(String roomCode) {
        RoomEntity room = requireRoom(roomCode);
        List<ParticipantEntity> participants = participantRepository.findAllByRoomCode(roomCode);
        if (playerCount(participants) < REQUIRED_PLAYER_COUNT) {
            throw new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage());
        }

        Instant now = Instant.now();
        GameEntity previousGame = requireGame(room.getActiveGameId());
        if (!GameStatus.COMPLETED.name().equals(previousGame.getStatus())) {
            previousGame.setStatus(GameStatus.COMPLETED.name());
            previousGame.setCurrentTurn(null);
            gameRepository.save(previousGame);
        }

        finishRound(room);
        syncGameForPlayers(previousGame, participants);

        UUID nextGameId = UUID.randomUUID();
        int nextRound = room.getCurrentRound() + 1;
        room.setActiveGameId(nextGameId);
        room.setCurrentRound(nextRound);
        room.setStatus(GameStatus.IN_PROGRESS.name());

        GameEntity nextGame = new GameEntity(
                nextGameId,
                roomCode,
                nextRound,
                GameStatus.IN_PROGRESS.name(),
                Symbol.X.name(),
                null,
                0,
                GameMapper.emptyBoard()
        );

        GameRoundEntity nextGameRound = new GameRoundEntity(roomCode, nextRound, nextGameId, GameStatus.IN_PROGRESS.name(), now, null);

        roomRepository.save(room);
        gameRepository.save(nextGame);
        gameRoundRepository.save(nextGameRound);
        syncGameForPlayers(nextGame, participants);

        PlayAgainResponse response = RoomMapper.toPlayAgainResponse(
                room,
                SuccessMessage.NEW_ROUND_STARTED.getMessage()
        );

        publishRealtime(roomCode, MessageTopic.NEW_ROUND_STARTED, response);
        return response;
    }

    @Override
    public JoinGameResponse joinGame(String roomCode, JoinGameRequest requestBody) {
        RoomEntity room = requireRoom(roomCode);
        List<ParticipantEntity> participants = participantRepository.findAllByRoomCode(roomCode);

        String normalizedName = normalize(requestBody.playerName());
        if (participants.stream().anyMatch(participant -> participant.getNormalizedPlayerName().equals(normalizedName))) {
            throw new PlayerAlreadyExistsException(ErrorMessage.PLAYER_ALREADY_EXISTS.format(requestBody.playerName()));
        }

        long playerCount = playerCount(participants);
        PlayerType type = playerCount < REQUIRED_PLAYER_COUNT ? PlayerType.PLAYER : PlayerType.SPECTATOR;
        Symbol symbol = type == PlayerType.SPECTATOR ? null : (playerCount == 0 ? Symbol.X : Symbol.O);

        ParticipantEntity participant = new ParticipantEntity(
                roomCode,
                normalizedName,
                requestBody.playerName(),
                type.name(),
                symbol == null ? null : symbol.name(),
                0,
                Instant.now()
        );

        participantRepository.save(participant);
        participants.add(participant);

        String message = type == PlayerType.PLAYER
                ? SuccessMessage.PLAYER_JOINED.getMessage()
                : SuccessMessage.SPECTATOR_JOINED.getMessage();
        JoinGameResponse response = PlayerMapper.toJoinGameResponse(participant, message);
        GameEntity game = requireGame(room.getActiveGameId());

        if (type == PlayerType.PLAYER) {
            room.setStatus(GameStatus.IN_PROGRESS.name());
            game.setStatus(GameStatus.IN_PROGRESS.name());

            roomRepository.save(room);
            gameRepository.save(game);
            markRoundInProgress(roomCode, room.getCurrentRound());
            syncGameForPlayers(game, participants);
        }

        publishRealtime(
                roomCode,
                MessageTopic.PLAYER_JOINED,
                toGameInfoResponse(game, participants, message)
        );
        return response;
    }

    @Override
    public GameInfoResponse getGameInfo(String roomCode) {
        RoomEntity room = requireRoom(roomCode);
        GameEntity game = requireGame(room.getActiveGameId());
        List<ParticipantEntity> participants = participantRepository.findAllByRoomCode(roomCode);
        return toGameInfoResponse(game, participants, SuccessMessage.GAME_INFO_RETRIEVED.getMessage());
    }

    @Override
    public BoardResponse getBoardStatus(String roomCode) {
        RoomEntity room = requireRoom(roomCode);
        GameEntity game = requireGame(room.getActiveGameId());
        return GameMapper.toBoardResponse(game, SuccessMessage.BOARD_STATUS_RETRIEVED.getMessage());
    }

    @Override
    public GameInfoResponse deleteGame(String roomCode) {
        RoomEntity room = requireRoom(roomCode);
        GameEntity activeGame = requireGame(room.getActiveGameId());
        List<ParticipantEntity> participants = participantRepository.findAllByRoomCode(roomCode);

        GameInfoResponse response = toGameInfoResponse(activeGame, participants, SuccessMessage.GAME_DELETED.getMessage());

        List<GameRoundEntity> rounds = gameRoundRepository.findAllByRoomCode(roomCode);
        for (GameRoundEntity round : rounds) {
            moveRepository.deleteAllByGameId(round.getGameId());
            gameRepository.deleteById(round.getGameId());
        }

        participants.stream()
                .filter(participant -> PlayerType.PLAYER.name().equals(participant.getPlayerType()))
                .forEach(participant -> removeRoomFromPlayerHistory(participant, rounds));

        gameRoundRepository.deleteAllByRoomCode(roomCode);
        participantRepository.deleteAllByRoomCode(roomCode);
        roomCatalogRepository.delete(new RoomCatalogEntity(RoomCatalogEntity.ALL_ROOMS, roomCode));
        roomRepository.deleteById(roomCode);

        publishRealtime(roomCode, MessageTopic.GAME_DELETED, response);
        return response;
    }

    private GameInfoResponse toGameInfoResponse(GameEntity game, List<ParticipantEntity> participants, String message) {
        PlayerMapper.ParticipantSummary summary = PlayerMapper.summarize(participants);

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

    private void markRoundInProgress(String roomCode, int roundNumber) {
        gameRoundRepository.findByRoomCodeAndRoundNo(roomCode, roundNumber)
                .ifPresent(round -> {
                    round.setStatus(GameStatus.IN_PROGRESS.name());
                    gameRoundRepository.save(round);
                });
    }

    private void syncGameForPlayers(GameEntity game, List<ParticipantEntity> participants) {
        participants.stream()
                .filter(participant -> PlayerType.PLAYER.name().equals(participant.getPlayerType()))
                .forEach(participant -> {
                    playerCatalogRepository.save(new PlayerCatalogEntity(
                            PlayerCatalogEntity.ALL_PLAYERS,
                            participant.getNormalizedPlayerName(),
                            participant.getPlayerName()
                    ));

                    playerGameRepository.save(new PlayerGameEntity(
                            participant.getNormalizedPlayerName(),
                            game.getGameId(),
                            game.getRoomCode(),
                            participant.getSymbol(),
                            game.getWinner() != null && normalize(game.getWinner()).equals(participant.getNormalizedPlayerName())
                    ));
                });
    }

    private void removeRoomFromPlayerHistory(ParticipantEntity player, List<GameRoundEntity> rounds) {
        rounds.forEach(round -> playerGameRepository.deleteByNormalizedPlayerNameAndGameId(
                player.getNormalizedPlayerName(),
                round.getGameId()
        ));

        if (playerGameRepository.findAllByNormalizedPlayerName(player.getNormalizedPlayerName()).isEmpty()) {
            playerCatalogRepository.delete(new PlayerCatalogEntity(
                    PlayerCatalogEntity.ALL_PLAYERS,
                    player.getNormalizedPlayerName(),
                    player.getPlayerName()
            ));
        }
    }

    private ParticipantEntity findPlayerBySymbol(List<ParticipantEntity> participants, Symbol symbol) {
        return participants.stream()
                .filter(participant -> symbol.name().equals(participant.getSymbol()))
                .findFirst()
                .orElseThrow(() -> new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage()));
    }

    private long playerCount(List<ParticipantEntity> participants) {
        return participants.stream()
                .filter(participant -> PlayerType.PLAYER.name().equals(participant.getPlayerType()))
                .count();
    }

    private <T> void publishRealtime(String destinationId, MessageTopic topic, T payload) {
        eventPublisher.publishEvent(new RealtimeEvent<>(destinationId, topic, payload));
    }

    private String generateUniqueRoomCode() {
        String roomCode;
        do {
            roomCode = CodeGenerator.generate();
        } while (roomRepository.existsById(roomCode));
        return roomCode;
    }
}
