package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.*;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.game.*;
import com.svi.tictactoe.dto.response.room.GameSummaryResponse;
import com.svi.tictactoe.dto.response.room.RoomInfoResponse;
import com.svi.tictactoe.dto.response.room.RoomsResponse;
import com.svi.tictactoe.entity.*;
import com.svi.tictactoe.exception.*;
import com.svi.tictactoe.mapper.GameMapper;
import com.svi.tictactoe.mapper.PlayerMapper;
import com.svi.tictactoe.mapper.RoomMapper;
import com.svi.tictactoe.realtime.event.RealtimeEvent;
import com.svi.tictactoe.repository.cassandra.*;
import com.svi.tictactoe.service.RoomService;
import com.svi.tictactoe.service.support.PlayerGameSynchronizer;
import com.svi.tictactoe.util.CodeGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.svi.tictactoe.util.PlayerNameUtil.normalize;

@Service
public class RoomServiceImpl implements RoomService {

    private static final int REQUIRED_PLAYER_COUNT = 2;

    private final RoomRepository roomRepository;
    private final RoomCatalogRepository roomCatalogRepository;
    private final GameRepository gameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final GameMoveRepository moveRepository;
    private final PlayerCatalogRepository playerCatalogRepository;
    private final PlayerGameRepository playerGameRepository;
    private final PlayerGameSynchronizer playerGameSynchronizer;
    private final ApplicationEventPublisher eventPublisher;

    public RoomServiceImpl(
            RoomRepository roomRepository,
            RoomCatalogRepository roomCatalogRepository,
            GameRepository gameRepository,
            GameRoundRepository gameRoundRepository,
            RoomPlayerRepository roomPlayerRepository,
            GameMoveRepository moveRepository,
            PlayerCatalogRepository playerCatalogRepository,
            PlayerGameRepository playerGameRepository,
            PlayerGameSynchronizer playerGameSynchronizer,
            ApplicationEventPublisher eventPublisher) {
        this.roomRepository = roomRepository;
        this.roomCatalogRepository = roomCatalogRepository;
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.moveRepository = moveRepository;
        this.playerCatalogRepository = playerCatalogRepository;
        this.playerGameRepository = playerGameRepository;
        this.playerGameSynchronizer = playerGameSynchronizer;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CreateGameResponse createRoom(CreateGameRequest requestBody) {
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
        GameRoundEntity gameRound = new GameRoundEntity(
                roomCode,
                1,
                gameId,
                GameStatus.WAITING_FOR_PLAYERS.name(),
                now,
                null
        );
        RoomPlayerEntity creator = new RoomPlayerEntity(
                roomCode,
                normalize(requestBody.playerName()),
                requestBody.playerName(),
                PlayerType.PLAYER.name(),
                Symbol.X.name(),
                0,
                now,
                true
        );

        roomRepository.save(room);
        roomCatalogRepository.save(new RoomCatalogEntity(RoomCatalogEntity.ALL_ROOMS, roomCode));
        gameRepository.save(game);
        gameRoundRepository.save(gameRound);
        roomPlayerRepository.save(creator);
        playerGameSynchronizer.sync(game, List.of(creator));

        return new CreateGameResponse(
                SuccessMessage.GAME_CREATED.getMessage(),
                roomCode,
                gameId,
                room.getCreatedAt(),
                PlayerMapper.toPlayerResponse(creator)
        );
    }

    @Override
    public JoinGameResponse joinRoom(String roomCode, JoinGameRequest requestBody) {
        RoomEntity room = requireRoom(roomCode);
        List<RoomPlayerEntity> players = roomPlayerRepository.findAllByRoomCode(roomCode);
        requireActiveRoom(players);

        String normalizedName = normalize(requestBody.playerName());
        if (players.stream().anyMatch(player -> player.getNormalizedPlayerName().equals(normalizedName))) {
            throw new PlayerAlreadyExistsException(ErrorMessage.PLAYER_ALREADY_EXISTS.format(requestBody.playerName()));
        }

        long playerCount = playerCount(players);
        PlayerType type = playerCount < REQUIRED_PLAYER_COUNT ? PlayerType.PLAYER : PlayerType.SPECTATOR;
        Symbol symbol = type == PlayerType.SPECTATOR ? null : (playerCount == 0 ? Symbol.X : Symbol.O);

        RoomPlayerEntity player = new RoomPlayerEntity(
                roomCode,
                normalizedName,
                requestBody.playerName(),
                type.name(),
                symbol == null ? null : symbol.name(),
                0,
                Instant.now(),
                true
        );

        roomPlayerRepository.save(player);
        players.add(player);

        String message = type == PlayerType.PLAYER
                ? SuccessMessage.PLAYER_JOINED.getMessage()
                : SuccessMessage.SPECTATOR_JOINED.getMessage();
        GameEntity game = requireGame(room.getActiveGameId());
        JoinGameResponse response = PlayerMapper.toJoinGameResponse(
                player,
                game.getGameId(),
                message
        );

        if (type == PlayerType.PLAYER) {
            room.setStatus(GameStatus.IN_PROGRESS.name());
            game.setStatus(GameStatus.IN_PROGRESS.name());

            roomRepository.save(room);
            gameRepository.save(game);
            markRoundInProgress(roomCode, room.getCurrentRound());
            playerGameSynchronizer.sync(game, players);
        }

        publishRealtime(
                roomCode,
                MessageTopic.PLAYER_JOINED,
                toGameInfoResponse(game, players, message)
        );
        return response;
    }

    @Override
    public LeaveGameResponse leaveRoom(String roomCode, String playerName) {
        RoomEntity room = requireRoom(roomCode);
        List<RoomPlayerEntity> players = roomPlayerRepository.findAllByRoomCode(roomCode);
        String normalizedPlayerName = normalize(playerName);

        RoomPlayerEntity leavingMember = players.stream()
                .filter(player -> normalizedPlayerName.equals(player.getNormalizedPlayerName()))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException(ErrorMessage.PLAYER_NOT_FOUND.format(playerName)));

        if (PlayerType.SPECTATOR.name().equals(leavingMember.getPlayerType())) {
            roomPlayerRepository.delete(leavingMember);
            return new LeaveGameResponse(SuccessMessage.PLAYER_LEFT.getMessage());
        }

        GameEntity game = requireGame(room.getActiveGameId());
        if (GameStatus.COMPLETED.name().equals(game.getStatus())) {
            throw new GameAlreadyFinishedException(ErrorMessage.GAME_ALREADY_FINISHED.getMessage());
        }

        Symbol leavingPlayerSymbol = Symbol.fromString(leavingMember.getSymbol());
        boolean leavingPlayerPlacedAMove = game.getBoard().stream()
                .filter(cell -> cell != null && !cell.isBlank())
                .map(Symbol::fromString)
                .anyMatch(symbol -> symbol == leavingPlayerSymbol);

        game.setWinner(null);

        if (leavingPlayerPlacedAMove) {
            RoomPlayerEntity enemyPlayer = players.stream()
                    .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                    .filter(player -> !player.getNormalizedPlayerName().equals(leavingMember.getNormalizedPlayerName()))
                    .findFirst()
                    .orElseThrow(() -> new PlayerNotFoundException(ErrorMessage.OPPONENT_OF_PLAYER_NOT_FOUND.format(playerName)));

            enemyPlayer.setScore((enemyPlayer.getScore() == null ? 0 : enemyPlayer.getScore()) + 1);
            game.setWinner(enemyPlayer.getPlayerName());
        }

        List<RoomPlayerEntity> remainingPlayers = players.stream()
                .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                .toList();

        remainingPlayers.forEach(player -> {
            player.setActive(false);
            roomPlayerRepository.save(player);
        });

        players.stream()
                .filter(player -> PlayerType.SPECTATOR.name().equals(player.getPlayerType()))
                .forEach(roomPlayerRepository::delete);

        game.setStatus(GameStatus.COMPLETED.name());
        game.setCurrentTurn(null);
        room.setStatus(GameStatus.COMPLETED.name());

        gameRepository.save(game);
        roomRepository.save(room);
        finishRound(room);
        playerGameSynchronizer.sync(game, remainingPlayers);

        publishRealtime(
                roomCode,
                MessageTopic.GAME_COMPLETED,
                toGameInfoResponse(game, remainingPlayers, SuccessMessage.GAME_COMPLETED.getMessage())
        );

        return new LeaveGameResponse(SuccessMessage.PLAYER_LEFT.getMessage());
    }

    @Override
    public RoomsResponse getRooms() {
        List<RoomInfoResponse> rooms = roomCatalogRepository
                .findAllByCatalogKey(RoomCatalogEntity.ALL_ROOMS).stream()
                .sorted(Comparator.comparing(RoomCatalogEntity::getRoomCode))
                .map(RoomCatalogEntity::getRoomCode)
                .map(roomRepository::findById)
                .flatMap(Optional::stream)
                .map(this::toRoomInfoResponse)
                .toList();

        int totalGames = rooms.stream()
                .mapToInt(room -> room.games().size())
                .sum();

        return new RoomsResponse(rooms.size(), totalGames, rooms);
    }

    @Override
    public RoomInfoResponse getRoom(String roomCode) {
        RoomEntity room = requireRoom(roomCode);
        return toRoomInfoResponse(room);
    }

    @Override
    public PlayAgainResponse playAgain(String roomCode) {
        RoomEntity room = requireRoom(roomCode);
        List<RoomPlayerEntity> players = roomPlayerRepository.findAllByRoomCode(roomCode);
        requireActiveRoom(players);

        if (playerCount(players) < REQUIRED_PLAYER_COUNT) {
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
        playerGameSynchronizer.sync(previousGame, players);

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
        GameRoundEntity nextGameRound = new GameRoundEntity(
                roomCode,
                nextRound,
                nextGameId,
                GameStatus.IN_PROGRESS.name(),
                now,
                null
        );

        roomRepository.save(room);
        gameRepository.save(nextGame);
        gameRoundRepository.save(nextGameRound);
        playerGameSynchronizer.sync(nextGame, players);

        PlayAgainResponse response = RoomMapper.toPlayAgainResponse(
                room,
                nextGameRound,
                SuccessMessage.NEW_ROUND_STARTED.getMessage()
        );
        publishRealtime(roomCode, MessageTopic.NEW_ROUND_STARTED, response);
        return response;
    }

    @Override
    public RoomInfoResponse deleteRoom(String roomCode) {
        RoomEntity room = requireRoom(roomCode);
        List<RoomPlayerEntity> players = roomPlayerRepository.findAllByRoomCode(roomCode);
        RoomInfoResponse response = toRoomInfoResponse(room);

        List<GameRoundEntity> rounds = gameRoundRepository.findAllByRoomCode(roomCode);
        for (GameRoundEntity round : rounds) {
            moveRepository.deleteAllByGameId(round.getGameId());
            gameRepository.deleteById(round.getGameId());
        }

        players.stream()
                .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                .forEach(player -> removeRoomFromPlayerHistory(player, rounds));

        gameRoundRepository.deleteAllByRoomCode(roomCode);
        roomPlayerRepository.deleteAllByRoomCode(roomCode);
        roomCatalogRepository.delete(new RoomCatalogEntity(RoomCatalogEntity.ALL_ROOMS, roomCode));
        roomRepository.deleteById(roomCode);

        publishRealtime(roomCode, MessageTopic.GAME_DELETED, response);
        return response;
    }

    private GameInfoResponse toGameInfoResponse(
            GameEntity game,
            List<RoomPlayerEntity> players,
            String message) {
        PlayerMapper.PlayerSummary summary = PlayerMapper.summarize(players);
        GameRoundEntity round = requireRound(game);
        return GameMapper.toGameInfoResponse(
                game,
                summary.players(),
                summary.spectatorCount(),
                round.getCreatedAt(),
                round.getEndedAt(),
                message
        );
    }

    private RoomInfoResponse toRoomInfoResponse(RoomEntity room) {
        List<GameSummaryResponse> games = gameRoundRepository
                .findAllByRoomCode(room.getRoomCode()).stream()
                .sorted(Comparator.comparing(GameRoundEntity::getRoundNo))
                .map(round -> {
                    GameEntity game = requireGame(round.getGameId());
                    return new GameSummaryResponse(
                            game.getGameId(),
                            GameStatus.valueOf(game.getStatus()),
                            game.getWinner(),
                            round.getCreatedAt(),
                            round.getEndedAt()
                    );
                })
                .toList();

        return new RoomInfoResponse(
                room.getRoomCode(),
                room.getCreatedAt(),
                games
        );
    }

    private GameRoundEntity requireRound(GameEntity game) {
        return gameRoundRepository
                .findByRoomCodeAndRoundNo(game.getRoomCode(), game.getRoundNo())
                .orElseThrow(() -> new GameNotFoundException(
                        ErrorMessage.GAME_ID_NOT_FOUND.format(game.getGameId())
                ));
    }

    private RoomEntity requireRoom(String roomCode) {
        return roomRepository.findById(roomCode)
                .orElseThrow(() -> new GameNotFoundException(ErrorMessage.GAME_NOT_FOUND.format(roomCode)));
    }

    private GameEntity requireGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(ErrorMessage.GAME_ID_NOT_FOUND.format(gameId)));
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

    private void removeRoomFromPlayerHistory(RoomPlayerEntity player, List<GameRoundEntity> rounds) {
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

    private long playerCount(List<RoomPlayerEntity> players) {
        return players.stream()
                .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                .count();
    }

    private void requireActiveRoom(List<RoomPlayerEntity> players) {
        boolean hasInactivePlayer = players.stream()
                .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                .anyMatch(player -> !player.isActive());

        if (hasInactivePlayer) {
            throw new RoomInactiveException(ErrorMessage.ROOM_INACTIVE.getMessage());
        }
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
