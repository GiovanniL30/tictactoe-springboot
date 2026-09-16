package com.svi.tictactoe.service;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.MessageTopic;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.game.BoardResponse;
import com.svi.tictactoe.dto.response.game.CreateGameResponse;
import com.svi.tictactoe.dto.response.game.GameInfoResponse;
import com.svi.tictactoe.dto.response.game.JoinGameResponse;
import com.svi.tictactoe.dto.response.game.PlayAgainResponse;
import com.svi.tictactoe.engine.GameEngine;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.entity.PlayerGameEntity;
import com.svi.tictactoe.entity.GameRoundEntity;
import com.svi.tictactoe.entity.GameMoveEntity;
import com.svi.tictactoe.entity.ParticipantEntity;
import com.svi.tictactoe.entity.RoomEntity;
import com.svi.tictactoe.repository.cassandra.GameRepository;
import com.svi.tictactoe.repository.cassandra.PlayerGameRepository;
import com.svi.tictactoe.repository.cassandra.GameRoundRepository;
import com.svi.tictactoe.repository.cassandra.GameMoveRepository;
import com.svi.tictactoe.repository.cassandra.ParticipantRepository;
import com.svi.tictactoe.repository.cassandra.PlayerCatalogRepository;
import com.svi.tictactoe.repository.cassandra.RoomRepository;
import com.svi.tictactoe.repository.cassandra.RoomCatalogRepository;
import com.svi.tictactoe.realtime.event.RealtimeEvent;
import com.svi.tictactoe.service.impl.GameServiceImpl;
import com.svi.tictactoe.service.impl.RoomServiceImpl;
import com.svi.tictactoe.service.support.PlayerGameSynchronizer;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameServiceImplTest {

    @Test
    void coordinatesCreationJoiningAndSpectatorAssignment() {
        RepositoryHarness harness = new RepositoryHarness();
        RoomService roomService = harness.roomService();
        GameService gameService = harness.gameService();

        CreateGameResponse game = roomService.createRoom(new CreateGameRequest("Alice"));
        JoinGameResponse secondPlayer = roomService.joinRoom(game.roomCode(), joinRequest("Bob"));
        JoinGameResponse spectator = roomService.joinRoom(game.roomCode(), joinRequest("Charlie"));

        assertNotNull(game.gameId());
        assertEquals(Symbol.X, game.player().symbol());
        assertEquals(game.gameId(), secondPlayer.gameId());
        assertEquals(game.gameId(), spectator.gameId());
        assertEquals(Symbol.O, secondPlayer.participant().symbol());
        assertEquals(PlayerType.SPECTATOR, spectator.participant().type());
        assertNull(spectator.participant().symbol());
        assertEquals(1, gameService.getGame(game.gameId()).spectatorCount());
        assertEquals(game.gameId(), roomService.getRoom(game.roomCode()).games().getFirst().gameId());
        assertEquals(GameStatus.IN_PROGRESS, roomService.getRoom(game.roomCode()).games().getFirst().status());
        assertEquals(GameStatus.IN_PROGRESS, gameService.getBoard(game.gameId()).status());
        assertEquals(
                List.of(
                        MessageTopic.PLAYER_JOINED,
                        MessageTopic.PLAYER_JOINED
                ),
                harness.publishedEvents.stream()
                        .map(RealtimeEvent.class::cast)
                        .map(RealtimeEvent::topic)
                        .toList()
        );
    }

    @Test
    void coordinatesMovePlacementAndNextRound() {
        RepositoryHarness harness = new RepositoryHarness();
        RoomService roomService = harness.roomService();
        GameService gameService = harness.gameService();
        CreateGameResponse game = roomService.createRoom(new CreateGameRequest("Alice"));
        roomService.joinRoom(game.roomCode(), joinRequest("Bob"));

        BoardResponse board = gameService.placeMove(game.gameId(), new AddMoveRequest(0, 0, Symbol.X));
        assertEquals(Symbol.X, board.grid()[0][0]);
        assertEquals(game.gameId(), board.gameId());

        PlayAgainResponse nextRound = roomService.playAgain(game.roomCode());
        assertEquals(2, nextRound.currentRound());
        assertEquals(game.roomCode(), nextRound.roomCode());
        assertNotEquals(game.gameId(), nextRound.gameId());
        assertNull(gameService.getBoard(nextRound.gameId()).grid()[0][0]);
    }

    @Test
    void keepsScoresAndSpectatorsWithTheRoomAcrossRounds() {
        RepositoryHarness harness = new RepositoryHarness();
        RoomService roomService = harness.roomService();
        GameService gameService = harness.gameService();
        CreateGameResponse game = roomService.createRoom(new CreateGameRequest("Alice"));
        roomService.joinRoom(game.roomCode(), joinRequest("Bob"));
        roomService.joinRoom(game.roomCode(), joinRequest("Charlie"));

        gameService.placeMove(game.gameId(), new AddMoveRequest(0, 0, Symbol.X));
        gameService.placeMove(game.gameId(), new AddMoveRequest(1, 0, Symbol.O));
        gameService.placeMove(game.gameId(), new AddMoveRequest(0, 1, Symbol.X));
        gameService.placeMove(game.gameId(), new AddMoveRequest(1, 1, Symbol.O));
        gameService.placeMove(game.gameId(), new AddMoveRequest(0, 2, Symbol.X));

        GameInfoResponse completedRound = gameService.getGame(game.gameId());
        assertEquals(1, completedRound.players().getFirst().score());
        assertEquals(1, completedRound.spectatorCount());
        assertEquals(GameStatus.COMPLETED, completedRound.status());
        assertEquals("Alice", completedRound.winner());

        PlayAgainResponse nextRoundResponse = roomService.playAgain(game.roomCode());
        GameInfoResponse nextRound = gameService.getGame(nextRoundResponse.gameId());
        assertEquals(1, nextRound.players().getFirst().score());
        assertEquals(1, nextRound.spectatorCount());
        assertNull(nextRound.winner());
    }

    @Test
    void recordsTheWinnerAndLoserInPlayerGameHistory() {
        RepositoryHarness harness = new RepositoryHarness();
        RoomService roomService = harness.roomService();
        GameService gameService = harness.gameService();
        CreateGameResponse game = roomService.createRoom(new CreateGameRequest("Alice"));
        roomService.joinRoom(game.roomCode(), joinRequest("Bob"));

        gameService.placeMove(game.gameId(), new AddMoveRequest(0, 0, Symbol.X));
        gameService.placeMove(game.gameId(), new AddMoveRequest(1, 0, Symbol.O));
        gameService.placeMove(game.gameId(), new AddMoveRequest(0, 1, Symbol.X));
        gameService.placeMove(game.gameId(), new AddMoveRequest(1, 1, Symbol.O));
        gameService.placeMove(game.gameId(), new AddMoveRequest(0, 2, Symbol.X));

        assertEquals(Boolean.TRUE, harness.playerGames.get("alice").get(game.gameId()).getWon());
        assertEquals(Boolean.FALSE, harness.playerGames.get("bob").get(game.gameId()).getWon());
    }

    private JoinGameRequest joinRequest(String playerName) {
        return new JoinGameRequest(playerName);
    }

    private static final class RepositoryHarness {

        private final RoomRepository roomRepository = mock(RoomRepository.class);
        private final RoomCatalogRepository roomCatalogRepository = mock(RoomCatalogRepository.class);
        private final GameRepository gameRepository = mock(GameRepository.class);
        private final GameRoundRepository gameRoundRepository = mock(GameRoundRepository.class);
        private final ParticipantRepository participantRepository = mock(ParticipantRepository.class);
        private final GameMoveRepository moveRepository = mock(GameMoveRepository.class);
        private final PlayerCatalogRepository playerCatalogRepository = mock(PlayerCatalogRepository.class);
        private final PlayerGameRepository playerGameRepository = mock(PlayerGameRepository.class);
        private final GameEngine gameEngine = new GameEngine();
        private final List<Object> publishedEvents = new ArrayList<>();
        private final ApplicationEventPublisher eventPublisher = publishedEvents::add;

        private final Map<String, RoomEntity> rooms = new HashMap<>();
        private final Map<UUID, GameEntity> games = new HashMap<>();
        private final Map<String, List<GameRoundEntity>> rounds = new HashMap<>();
        private final Map<String, List<ParticipantEntity>> participants = new HashMap<>();
        private final Map<UUID, List<GameMoveEntity>> moves = new HashMap<>();
        private final Map<String, Map<UUID, PlayerGameEntity>> playerGames = new HashMap<>();

        private RepositoryHarness() {
            when(roomRepository.existsById(anyString()))
                    .thenAnswer(invocation -> rooms.containsKey(invocation.getArgument(0)));
            when(roomRepository.findById(anyString()))
                    .thenAnswer(invocation -> Optional.ofNullable(rooms.get(invocation.getArgument(0))));
            when(roomRepository.save(any(RoomEntity.class))).thenAnswer(invocation -> {
                RoomEntity entity = invocation.getArgument(0);
                rooms.put(entity.getRoomCode(), entity);
                return entity;
            });

            when(gameRepository.findById(any(UUID.class)))
                    .thenAnswer(invocation -> Optional.ofNullable(games.get(invocation.getArgument(0))));
            when(gameRepository.save(any(GameEntity.class))).thenAnswer(invocation -> {
                GameEntity entity = invocation.getArgument(0);
                games.put(entity.getGameId(), entity);
                return entity;
            });

            when(gameRoundRepository.findAllByRoomCode(anyString()))
                    .thenAnswer(invocation -> new ArrayList<>(
                            rounds.getOrDefault(invocation.getArgument(0), List.of())));
            when(gameRoundRepository.findByRoomCodeAndRoundNo(anyString(), anyInt()))
                    .thenAnswer(invocation -> rounds
                            .getOrDefault(invocation.getArgument(0), List.of())
                            .stream()
                            .filter(round -> round.getRoundNo().equals(invocation.getArgument(1)))
                            .findFirst());
            when(gameRoundRepository.save(any(GameRoundEntity.class))).thenAnswer(invocation -> {
                GameRoundEntity entity = invocation.getArgument(0);
                List<GameRoundEntity> roomRounds = rounds.computeIfAbsent(
                        entity.getRoomCode(), ignored -> new ArrayList<>());
                roomRounds.removeIf(round -> round.getRoundNo().equals(entity.getRoundNo()));
                roomRounds.add(entity);
                return entity;
            });

            when(participantRepository.findAllByRoomCode(anyString()))
                    .thenAnswer(invocation -> new ArrayList<>(
                            participants.getOrDefault(invocation.getArgument(0), List.of())));
            when(participantRepository.save(any(ParticipantEntity.class))).thenAnswer(invocation -> {
                ParticipantEntity entity = invocation.getArgument(0);
                List<ParticipantEntity> roomParticipants = participants.computeIfAbsent(
                        entity.getRoomCode(), ignored -> new ArrayList<>());
                roomParticipants.removeIf(participant -> participant.getNormalizedPlayerName()
                        .equals(entity.getNormalizedPlayerName()));
                roomParticipants.add(entity);
                return entity;
            });

            when(moveRepository.save(any(GameMoveEntity.class))).thenAnswer(invocation -> {
                GameMoveEntity entity = invocation.getArgument(0);
                moves.computeIfAbsent(entity.getGameId(), ignored -> new ArrayList<>()).add(entity);
                return entity;
            });

            when(playerGameRepository.save(any(PlayerGameEntity.class))).thenAnswer(invocation -> {
                PlayerGameEntity entity = invocation.getArgument(0);
                playerGames.computeIfAbsent(entity.getNormalizedPlayerName(), ignored -> new HashMap<>())
                        .put(entity.getGameId(), entity);
                return entity;
            });
        }

        private GameService gameService() {
            return new GameServiceImpl(
                    roomRepository,
                    gameRepository,
                    gameRoundRepository,
                    participantRepository,
                    moveRepository,
                    playerGameSynchronizer(),
                    gameEngine,
                    eventPublisher
            );
        }

        private RoomService roomService() {
            return new RoomServiceImpl(
                    roomRepository,
                    roomCatalogRepository,
                    gameRepository,
                    gameRoundRepository,
                    participantRepository,
                    moveRepository,
                    playerCatalogRepository,
                    playerGameRepository,
                    playerGameSynchronizer(),
                    eventPublisher
            );
        }

        private PlayerGameSynchronizer playerGameSynchronizer() {
            return new PlayerGameSynchronizer(playerCatalogRepository, playerGameRepository);
        }
    }
}
