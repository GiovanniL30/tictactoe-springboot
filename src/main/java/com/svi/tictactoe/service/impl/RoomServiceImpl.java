package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.*;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.game.CreateGameResponse;
import com.svi.tictactoe.dto.response.game.JoinGameResponse;
import com.svi.tictactoe.dto.response.game.LeaveGameResponse;
import com.svi.tictactoe.dto.response.game.PlayAgainResponse;
import com.svi.tictactoe.dto.response.room.RoomInfoResponse;
import com.svi.tictactoe.dto.response.room.RoomsResponse;
import com.svi.tictactoe.entity.*;
import com.svi.tictactoe.exception.GameAlreadyFinishedException;
import com.svi.tictactoe.exception.GameNotCompleted;
import com.svi.tictactoe.exception.GameNotStartedException;
import com.svi.tictactoe.exception.PlayerAlreadyExistsException;
import com.svi.tictactoe.mapper.GameMapper;
import com.svi.tictactoe.mapper.PlayerMapper;
import com.svi.tictactoe.mapper.RoomMapper;
import com.svi.tictactoe.realtime.event.RealtimeEvent;
import com.svi.tictactoe.realtime.event.RealtimePayload;
import com.svi.tictactoe.repository.cassandra.*;
import com.svi.tictactoe.service.RoomService;
import com.svi.tictactoe.service.support.GameLookup;
import com.svi.tictactoe.service.support.PlayerGameSynchronizer;
import com.svi.tictactoe.util.BoardUtil;
import com.svi.tictactoe.util.CodeGenerator;
import com.svi.tictactoe.util.PlayerNameUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final GameLookup gameLookup;
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
            GameLookup gameLookup,
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
        this.gameLookup = gameLookup;
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
                BoardUtil.emptyBoard()
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
                PlayerNameUtil.normalize(requestBody.playerName()),
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

        return RoomMapper.toCreateGameResponse(
                room,
                game,
                creator,
                SuccessMessage.GAME_CREATED.getMessage()
        );
    }

    @Override
    public JoinGameResponse joinRoom(String roomCode, JoinGameRequest requestBody) {
        RoomEntity room = gameLookup.requireRoom(roomCode);
        List<RoomPlayerEntity> players = gameLookup.requireActiveRoomPlayers(roomCode);

        String normalizedName = PlayerNameUtil.normalize(requestBody.playerName());
        validatePlayerNotExists(players, requestBody.playerName());

        //if there are already 2 players, make other spectator
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

        String message = type == PlayerType.PLAYER ? SuccessMessage.PLAYER_JOINED.getMessage() : SuccessMessage.SPECTATOR_JOINED.getMessage();
        GameEntity game = gameLookup.requireGame(room.getActiveGameId());
        JoinGameResponse response = PlayerMapper.toJoinGameResponse(
                player,
                game.getGameId(),
                message
        );

        // the second player joining will always be a type of "Player" so we can now start the game
        if (type == PlayerType.PLAYER) {
            room.setStatus(GameStatus.IN_PROGRESS.name());
            game.setStatus(GameStatus.IN_PROGRESS.name());

            roomRepository.save(room);
            gameRepository.save(game);
            markRoundInProgress(roomCode, room.getCurrentRound());
            playerGameSynchronizer.sync(game, players);
        }

        GameRoundEntity round = gameLookup.requireRound(game);
        PlayerMapper.PlayerSummary summary = PlayerMapper.summarize(players);
        publishRealtime(
                roomCode,
                MessageTopic.PLAYER_JOINED,
                GameMapper.toGameInfoResponse(
                        game,
                        summary.players(),
                        summary.spectatorCount(),
                        round.getCreatedAt(),
                        round.getEndedAt(),
                        message
                )
        );
        return response;
    }

    @Override
    public LeaveGameResponse leaveRoom(String roomCode, String playerName) {
        RoomEntity room = gameLookup.requireRoom(roomCode);
        List<RoomPlayerEntity> players = gameLookup.requireActiveRoomPlayers(roomCode);
        RoomPlayerEntity leavingMember = gameLookup.requireRoomPlayerEntity(players, playerName);

        // if player is spectator, delete the record on the database
        if (PlayerType.SPECTATOR.name().equals(leavingMember.getPlayerType())) {
            roomPlayerRepository.delete(leavingMember);
            return RoomMapper.toLeaveGameResponse(SuccessMessage.PLAYER_LEFT.getMessage());
        }

        GameEntity game = gameLookup.requireGame(room.getActiveGameId());
        if (GameStatus.COMPLETED.name().equals(game.getStatus())) {
            throw new GameAlreadyFinishedException(ErrorMessage.GAME_ALREADY_FINISHED.getMessage());
        }

        Symbol leavingPlayerSymbol = Symbol.fromString(leavingMember.getSymbol());
        boolean leavingPlayerPlacedAMove = isPlayerPlacedMove(game, leavingPlayerSymbol);
        game.setWinner(null);

        // on here, if the player that will be leaving already placed a move, make the opponent become the winner and increment the score
        if (leavingPlayerPlacedAMove) {
            RoomPlayerEntity enemyPlayer = gameLookup.requireOpponent(players, leavingMember);
            enemyPlayer.setScore((enemyPlayer.getScore() == null ? 0 : enemyPlayer.getScore()) + 1);
            game.setWinner(enemyPlayer.getPlayerName());
        }

        List<RoomPlayerEntity> gamePlayers = players.stream()
                .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                .toList();

        setPlayersToInactive(gamePlayers);
        deleteAllSpectator(players);
        game.setStatus(GameStatus.COMPLETED.name());
        game.setCurrentTurn(null);
        room.setStatus(GameStatus.COMPLETED.name());

        gameRepository.save(game);
        roomRepository.save(room);
        playerGameSynchronizer.sync(game, gamePlayers);
        finishRound(room);

        GameRoundEntity round = gameLookup.requireRound(game);
        PlayerMapper.PlayerSummary summary = PlayerMapper.summarize(gamePlayers);

        publishRealtime(
                roomCode,
                MessageTopic.GAME_COMPLETED,
                GameMapper.toGameInfoResponse(
                        game,
                        summary.players(),
                        summary.spectatorCount(),
                        round.getCreatedAt(),
                        round.getEndedAt(),
                        SuccessMessage.GAME_COMPLETED.getMessage()
                )
        );

        return RoomMapper.toLeaveGameResponse(SuccessMessage.PLAYER_LEFT.getMessage());
    }

    @Override
    public RoomsResponse getRooms() {
        List<String> roomCodes = roomCatalogRepository
                .findAllByCatalogKey(RoomCatalogEntity.ALL_ROOMS).stream()
                .sorted(Comparator.comparing(RoomCatalogEntity::getRoomCode))
                .map(RoomCatalogEntity::getRoomCode)
                .toList();

        if (roomCodes.isEmpty()) {
            return RoomMapper.toRoomsResponse(List.of());
        }

        List<GameRoundEntity> rounds = gameRoundRepository.findAllByRoomCodeIn(roomCodes);

        Map<String, List<GameRoundEntity>> roundsByRoomCode = rounds.stream().collect(Collectors.groupingBy(GameRoundEntity::getRoomCode));
        Map<UUID, GameEntity> gamesById = gameLookup.requireGames(rounds);

        List<RoomInfoResponse> rooms = roomRepository.findAllById(roomCodes).stream()
                .sorted(Comparator.comparing(RoomEntity::getRoomCode))
                .map(room -> RoomMapper.toRoomInfoResponse(
                        room,
                        roundsByRoomCode.getOrDefault(room.getRoomCode(), List.of()),
                        gamesById
                ))
                .toList();

        return RoomMapper.toRoomsResponse(rooms);
    }

    @Override
    public RoomInfoResponse getRoom(String roomCode) {
        RoomEntity room = gameLookup.requireRoom(roomCode);
        List<GameRoundEntity> rounds = gameRoundRepository.findAllByRoomCode(roomCode);
        return RoomMapper.toRoomInfoResponse(room, rounds, gameLookup.requireGames(rounds));
    }

    @Override
    public PlayAgainResponse playAgain(String roomCode) {
        RoomEntity room = gameLookup.requireRoom(roomCode);
        List<RoomPlayerEntity> players = gameLookup.requireActiveRoomPlayers(roomCode);

        validateRoomIsComplete(room, players);

        Instant now = Instant.now();
        GameEntity previousGame = gameLookup.requireGame(room.getActiveGameId());
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
                BoardUtil.emptyBoard()
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
        RoomEntity room = gameLookup.requireRoom(roomCode);
        List<RoomPlayerEntity> players = roomPlayerRepository.findAllByRoomCode(roomCode);
        List<GameRoundEntity> rounds = gameRoundRepository.findAllByRoomCode(roomCode);
        RoomInfoResponse response = RoomMapper.toRoomInfoResponse(room, rounds, gameLookup.requireGames(rounds));

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

    private boolean isPlayerPlacedMove(GameEntity game, Symbol leavingPlayerSymbol) {
        return game.getBoard().stream()
                .filter(cell -> cell != null && !cell.isBlank())
                .map(Symbol::fromString)
                .anyMatch(symbol -> symbol == leavingPlayerSymbol);
    }

    private void setPlayersToInactive(List<RoomPlayerEntity> players) {
        players.forEach(player -> {
            player.setActive(false);
            roomPlayerRepository.save(player);
        });
    }

    private void deleteAllSpectator(List<RoomPlayerEntity> players) {
        players.stream()
                .filter(player -> PlayerType.SPECTATOR.name().equals(player.getPlayerType()))
                .forEach(roomPlayerRepository::delete);
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

    private String generateUniqueRoomCode() {
        String roomCode;
        do {
            roomCode = CodeGenerator.generate();
        } while (roomRepository.existsById(roomCode));
        return roomCode;
    }

    private void validatePlayerNotExists(List<RoomPlayerEntity> players, String playerName) {
        if (players.stream().anyMatch(player -> player.getNormalizedPlayerName().equals(PlayerNameUtil.normalize(playerName)))) {
            throw new PlayerAlreadyExistsException(ErrorMessage.PLAYER_ALREADY_EXISTS.format(playerName));
        }
    }

    private void validateRoomIsComplete(RoomEntity room, List<RoomPlayerEntity> players) {
        if (!room.getStatus().equals(GameStatus.COMPLETED.name())) {
            throw new GameNotCompleted(ErrorMessage.CURRENT_GAME_NOT_COMPLETED.format(room.getActiveGameId()));
        }

        if (playerCount(players) < REQUIRED_PLAYER_COUNT) {
            throw new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage());
        }
    }

    private void publishRealtime(String destinationId, MessageTopic topic, RealtimePayload payload) {
        eventPublisher.publishEvent(new RealtimeEvent(destinationId, topic, payload));
    }
}
