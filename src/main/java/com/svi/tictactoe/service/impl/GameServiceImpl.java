package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.*;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.game.*;
import com.svi.tictactoe.entity.*;
import com.svi.tictactoe.exception.*;
import com.svi.tictactoe.mapper.GameMapper;
import com.svi.tictactoe.mapper.PlayerMapper;
import com.svi.tictactoe.mapper.RoomMapper;
import com.svi.tictactoe.repository.cassandra.*;
import com.svi.tictactoe.service.GameService;
import com.svi.tictactoe.util.CodeGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class GameServiceImpl implements GameService {

    private static final int BOARD_SIZE = 3;
    private static final int REQUIRED_PLAYER_COUNT = 2;

    private final RoomByCodeRepository roomRepository;
    private final RoomCatalogRepository roomCatalogRepository;
    private final GameByIdRepository gameByIdRepository;
    private final GameByRoomRepository gameByRoomRepository;
    private final ParticipantByRoomRepository participantRepository;
    private final MoveByGameRepository moveRepository;
    private final PlayerCatalogRepository playerCatalogRepository;
    private final GameByPlayerRepository gameByPlayerRepository;

    public GameServiceImpl(
            RoomByCodeRepository roomRepository,
            RoomCatalogRepository roomCatalogRepository,
            GameByIdRepository gameByIdRepository,
            GameByRoomRepository gameByRoomRepository,
            ParticipantByRoomRepository participantRepository,
            MoveByGameRepository moveRepository,
            PlayerCatalogRepository playerCatalogRepository,
            GameByPlayerRepository gameByPlayerRepository) {
        this.roomRepository = roomRepository;
        this.roomCatalogRepository = roomCatalogRepository;
        this.gameByIdRepository = gameByIdRepository;
        this.gameByRoomRepository = gameByRoomRepository;
        this.participantRepository = participantRepository;
        this.moveRepository = moveRepository;
        this.playerCatalogRepository = playerCatalogRepository;
        this.gameByPlayerRepository = gameByPlayerRepository;
    }

    @Override
    public CreateGameResponse createGame(CreateGameRequest requestBody) {
        String roomCode = generateUniqueRoomCode();
        UUID gameId = UUID.randomUUID();
        Instant now = Instant.now();

        RoomByCodeEntity room = new RoomByCodeEntity(roomCode, gameId, 1, GameStatus.WAITING_FOR_PLAYERS.name(), now);

        GameByIdEntity game = new GameByIdEntity(
                gameId,
                roomCode,
                1,
                GameStatus.WAITING_FOR_PLAYERS.name(),
                Symbol.X.name(),
                null,
                0,
                GameMapper.emptyBoard()
        );

        GameByRoomEntity gameByRoom = new GameByRoomEntity(roomCode, 1, gameId, GameStatus.WAITING_FOR_PLAYERS.name(), now, null);

        ParticipantByRoomEntity creator = new ParticipantByRoomEntity(
                roomCode,
                normalizeName(requestBody.playerName()),
                requestBody.playerName(),
                PlayerType.PLAYER.name(),
                Symbol.X.name(),
                0,
                now
        );

        roomRepository.save(room);
        roomCatalogRepository.save(new RoomCatalogEntity(RoomCatalogEntity.ALL_ROOMS, roomCode));
        gameByIdRepository.save(game);
        gameByRoomRepository.save(gameByRoom);
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
        GameByIdEntity game = requireActiveGame(gameId);

        if (GameStatus.WAITING_FOR_PLAYERS.name().equals(game.getStatus())) {
            throw new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage());
        }

        if (GameStatus.COMPLETED.name().equals(game.getStatus())) {
            throw new GameAlreadyFinishedException(ErrorMessage.GAME_ALREADY_FINISHED.getMessage());
        }

        int x = requestBody.x();
        int y = requestBody.y();

        if (!isValidPosition(x, y)) {
            throw new InvalidPositionException(ErrorMessage.INVALID_POSITION.format(x, y));
        }

        Symbol currentTurn = Symbol.fromString(game.getCurrentTurn());
        if (requestBody.symbol() != currentTurn) {
            throw new InvalidTurnException(ErrorMessage.INVALID_TURN.format(currentTurn));
        }

        List<String> board = mutableBoard(game.getBoard());
        int boardIndex = x * BOARD_SIZE + y;
        if (!board.get(boardIndex).isBlank()) {
            throw new PositionAlreadyTakenException(ErrorMessage.POSITION_ALREADY_TAKEN.format(x, y));
        }

        board.set(boardIndex, currentTurn.name());
        game.setBoard(board);
        int moveNumber = game.getMoveCount() == null ? 1 : game.getMoveCount() + 1;
        game.setMoveCount(moveNumber);

        List<ParticipantByRoomEntity> participants = participantRepository.findAllByRoomCode(game.getRoomCode());
        ParticipantByRoomEntity movingPlayer = findPlayerBySymbol(participants, currentTurn);

        moveRepository.save(new MoveByGameEntity(
                gameId,
                moveNumber,
                game.getRoomCode(),
                movingPlayer.getPlayerName(),
                currentTurn.name(),
                x,
                y,
                Instant.now()
        ));

        RoomByCodeEntity room = requireRoom(game.getRoomCode());
        boolean roundCompleted = false;

        if (hasWinner(board, currentTurn)) {
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

        gameByIdRepository.save(game);
        roomRepository.save(room);

        if (roundCompleted) {
            syncGameForPlayers(game, participants);
        }

        return GameMapper.toBoardResponse(game, SuccessMessage.MOVE_PLACED.getMessage());
    }

    @Override
    public PlayAgainResponse playAgain(String roomCode) {
        RoomByCodeEntity room = requireRoom(roomCode);
        List<ParticipantByRoomEntity> participants = participantRepository.findAllByRoomCode(roomCode);
        if (playerCount(participants) < REQUIRED_PLAYER_COUNT) {
            throw new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage());
        }

        Instant now = Instant.now();
        GameByIdEntity previousGame = requireGame(room.getActiveGameId());
        if (!GameStatus.COMPLETED.name().equals(previousGame.getStatus())) {
            previousGame.setStatus(GameStatus.COMPLETED.name());
            previousGame.setCurrentTurn(null);
            gameByIdRepository.save(previousGame);
        }

        finishRound(room);
        syncGameForPlayers(previousGame, participants);

        UUID nextGameId = UUID.randomUUID();
        int nextRound = room.getCurrentRound() + 1;
        room.setActiveGameId(nextGameId);
        room.setCurrentRound(nextRound);
        room.setStatus(GameStatus.IN_PROGRESS.name());

        GameByIdEntity nextGame = new GameByIdEntity(
                nextGameId,
                roomCode,
                nextRound,
                GameStatus.IN_PROGRESS.name(),
                Symbol.X.name(),
                null,
                0,
                GameMapper.emptyBoard()
        );

        GameByRoomEntity nextGameByRoom = new GameByRoomEntity(roomCode, nextRound, nextGameId, GameStatus.IN_PROGRESS.name(), now, null);

        roomRepository.save(room);
        gameByIdRepository.save(nextGame);
        gameByRoomRepository.save(nextGameByRoom);
        syncGameForPlayers(nextGame, participants);

        return RoomMapper.toPlayAgainResponse(room, SuccessMessage.NEW_ROUND_STARTED.getMessage());
    }

    @Override
    public JoinGameResponse joinGame(String roomCode, JoinGameRequest requestBody) {
        RoomByCodeEntity room = requireRoom(roomCode);
        List<ParticipantByRoomEntity> participants = new ArrayList<>(
                participantRepository.findAllByRoomCode(roomCode)
        );

        String normalizedName = normalizeName(requestBody.playerName());
        if (participants.stream().anyMatch(participant -> participant.getNormalizedPlayerName().equals(normalizedName))) {
            throw new PlayerAlreadyExistsException(ErrorMessage.PLAYER_ALREADY_EXISTS.format(requestBody.playerName()));
        }

        long playerCount = playerCount(participants);
        PlayerType type = playerCount < REQUIRED_PLAYER_COUNT ? PlayerType.PLAYER : PlayerType.SPECTATOR;
        Symbol symbol = type == PlayerType.SPECTATOR ? null : (playerCount == 0 ? Symbol.X : Symbol.O);

        ParticipantByRoomEntity participant = new ParticipantByRoomEntity(
                roomCode,
                normalizedName,
                requestBody.playerName(),
                type.name(),
                symbol == null ? null : symbol.name(),
                0,
                Instant.now()
        );

        participantRepository.save(participant);

        if (type == PlayerType.PLAYER) {
            GameByIdEntity game = requireGame(room.getActiveGameId());
            room.setStatus(GameStatus.IN_PROGRESS.name());
            game.setStatus(GameStatus.IN_PROGRESS.name());
            participants.add(participant);

            roomRepository.save(room);
            gameByIdRepository.save(game);
            markRoundInProgress(roomCode, room.getCurrentRound());
            syncGameForPlayers(game, participants);
        }

        String message = type == PlayerType.PLAYER ? SuccessMessage.PLAYER_JOINED.getMessage() : SuccessMessage.SPECTATOR_JOINED.getMessage();

        return PlayerMapper.toJoinGameResponse(participant, message);
    }

    @Override
    public GameInfoResponse getGameInfo(String roomCode) {
        RoomByCodeEntity room = requireRoom(roomCode);
        GameByIdEntity game = requireGame(room.getActiveGameId());
        List<ParticipantByRoomEntity> participants = participantRepository.findAllByRoomCode(roomCode);
        return toGameInfoResponse(game, participants, SuccessMessage.GAME_INFO_RETRIEVED.getMessage());
    }

    @Override
    public BoardResponse getBoardStatus(String roomCode) {
        RoomByCodeEntity room = requireRoom(roomCode);
        GameByIdEntity game = requireGame(room.getActiveGameId());
        return GameMapper.toBoardResponse(game, SuccessMessage.BOARD_STATUS_RETRIEVED.getMessage());
    }

    @Override
    public GameInfoResponse deleteGame(String roomCode) {
        RoomByCodeEntity room = requireRoom(roomCode);
        GameByIdEntity activeGame = requireGame(room.getActiveGameId());
        List<ParticipantByRoomEntity> participants = participantRepository.findAllByRoomCode(roomCode);

        GameInfoResponse response = toGameInfoResponse(activeGame, participants, SuccessMessage.GAME_DELETED.getMessage());

        List<GameByRoomEntity> rounds = gameByRoomRepository.findAllByRoomCode(roomCode);
        for (GameByRoomEntity round : rounds) {
            moveRepository.deleteAllByGameId(round.getGameId());
            gameByIdRepository.deleteById(round.getGameId());
        }

        participants.stream()
                .filter(participant -> PlayerType.PLAYER.name().equals(participant.getPlayerType()))
                .forEach(participant -> removeRoomFromPlayerHistory(participant, rounds));

        gameByRoomRepository.deleteAllByRoomCode(roomCode);
        participantRepository.deleteAllByRoomCode(roomCode);
        roomCatalogRepository.delete(new RoomCatalogEntity(RoomCatalogEntity.ALL_ROOMS, roomCode));
        roomRepository.deleteById(roomCode);

        return response;
    }

    private GameInfoResponse toGameInfoResponse(GameByIdEntity game, List<ParticipantByRoomEntity> participants, String message) {
        PlayerMapper.ParticipantSummary summary = PlayerMapper.summarize(participants);

        return GameMapper.toGameInfoResponse(game, summary.players(), summary.spectatorCount(), message);
    }

    private RoomByCodeEntity requireRoom(String roomCode) {
        return roomRepository.findById(roomCode)
                .orElseThrow(() -> new GameNotFoundException(ErrorMessage.GAME_NOT_FOUND.format(roomCode)));
    }

    private GameByIdEntity requireGame(UUID gameId) {
        return gameByIdRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(ErrorMessage.GAME_ID_NOT_FOUND.format(gameId)));
    }

    private GameByIdEntity requireActiveGame(UUID gameId) {
        GameByIdEntity game = requireGame(gameId);
        RoomByCodeEntity room = requireRoom(game.getRoomCode());

        if (!room.getActiveGameId().equals(gameId)) {
            throw new GameNotFoundException(ErrorMessage.GAME_ID_NOT_FOUND.format(gameId));
        }

        return game;
    }

    private void finishRound(RoomByCodeEntity room) {
        gameByRoomRepository.findByRoomCodeAndRoundNo(room.getRoomCode(), room.getCurrentRound())
                .ifPresent(round -> {
                    round.setStatus(GameStatus.COMPLETED.name());
                    round.setEndedAt(Instant.now());
                    gameByRoomRepository.save(round);
                });
    }

    private void markRoundInProgress(String roomCode, int roundNumber) {
        gameByRoomRepository.findByRoomCodeAndRoundNo(roomCode, roundNumber)
                .ifPresent(round -> {
                    round.setStatus(GameStatus.IN_PROGRESS.name());
                    gameByRoomRepository.save(round);
                });
    }

    private void syncGameForPlayers(GameByIdEntity game, List<ParticipantByRoomEntity> participants) {
        participants.stream()
                .filter(participant -> PlayerType.PLAYER.name().equals(participant.getPlayerType()))
                .forEach(participant -> {
                    playerCatalogRepository.save(new PlayerCatalogEntity(
                            PlayerCatalogEntity.ALL_PLAYERS,
                            participant.getNormalizedPlayerName(),
                            participant.getPlayerName()
                    ));

                    gameByPlayerRepository.save(new GameByPlayerEntity(
                            participant.getNormalizedPlayerName(),
                            game.getGameId(),
                            game.getRoomCode(),
                            participant.getSymbol(),
                            game.getWinner() != null && normalizeName(game.getWinner()).equals(participant.getNormalizedPlayerName())
                    ));
                });
    }

    private void removeRoomFromPlayerHistory(ParticipantByRoomEntity player, List<GameByRoomEntity> rounds) {
        rounds.forEach(round -> gameByPlayerRepository.deleteByNormalizedPlayerNameAndGameId(
                player.getNormalizedPlayerName(),
                round.getGameId()
        ));

        if (gameByPlayerRepository.findAllByNormalizedPlayerName(player.getNormalizedPlayerName()).isEmpty()) {
            playerCatalogRepository.delete(new PlayerCatalogEntity(
                    PlayerCatalogEntity.ALL_PLAYERS,
                    player.getNormalizedPlayerName(),
                    player.getPlayerName()
            ));
        }
    }

    private ParticipantByRoomEntity findPlayerBySymbol(List<ParticipantByRoomEntity> participants, Symbol symbol) {
        return participants.stream()
                .filter(participant -> symbol.name().equals(participant.getSymbol()))
                .findFirst()
                .orElseThrow(() -> new GameNotStartedException(ErrorMessage.GAME_NOT_STARTED.getMessage()));
    }

    private long playerCount(List<ParticipantByRoomEntity> participants) {
        return participants.stream()
                .filter(participant -> PlayerType.PLAYER.name().equals(participant.getPlayerType()))
                .count();
    }

    private boolean hasWinner(List<String> board, Symbol symbol) {
        String value = symbol.name();

        for (int index = 0; index < BOARD_SIZE; index++) {
            int rowStart = index * BOARD_SIZE;

            boolean hasWinningRow = value.equals(board.get(rowStart))
                    && value.equals(board.get(rowStart + 1))
                    && value.equals(board.get(rowStart + 2));

            if (hasWinningRow) {
                return true;
            }

            boolean hasWinningColumn = value.equals(board.get(index))
                    && value.equals(board.get(BOARD_SIZE + index))
                    && value.equals(board.get(2 * BOARD_SIZE + index));

            if (hasWinningColumn) {
                return true;
            }
        }

        boolean hasWinningLeftDiagonal = value.equals(board.get(0))
                && value.equals(board.get(4))
                && value.equals(board.get(8));

        boolean hasWinningRightDiagonal = value.equals(board.get(2))
                && value.equals(board.get(4))
                && value.equals(board.get(6));

        return hasWinningLeftDiagonal || hasWinningRightDiagonal;
    }

    private List<String> mutableBoard(List<String> persistedBoard) {
        List<String> board = persistedBoard == null
                ? GameMapper.emptyBoard()
                : new ArrayList<>(persistedBoard);
        while (board.size() < BOARD_SIZE * BOARD_SIZE) {
            board.add("");
        }
        return board;
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    private String normalizeName(String playerName) {
        return playerName.trim().toLowerCase(Locale.ROOT);
    }

    private String generateUniqueRoomCode() {
        String roomCode;
        do {
            roomCode = CodeGenerator.generate();
        } while (roomRepository.existsById(roomCode));
        return roomCode;
    }
}
