package com.svi.tictactoe.service;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.BoardResponse;
import com.svi.tictactoe.dto.response.CreateGameResponse;
import com.svi.tictactoe.dto.response.GameInfoResponse;
import com.svi.tictactoe.dto.response.JoinGameResponse;
import com.svi.tictactoe.dto.response.PlayAgainResponse;
import com.svi.tictactoe.entity.GameByIdEntity;
import com.svi.tictactoe.entity.GameByRoomEntity;
import com.svi.tictactoe.entity.MoveByGameEntity;
import com.svi.tictactoe.entity.ParticipantByRoomEntity;
import com.svi.tictactoe.entity.RoomByCodeEntity;
import com.svi.tictactoe.repository.cassandra.GameByIdRepository;
import com.svi.tictactoe.repository.cassandra.GameByRoomRepository;
import com.svi.tictactoe.repository.cassandra.MoveByGameRepository;
import com.svi.tictactoe.repository.cassandra.ParticipantByRoomRepository;
import com.svi.tictactoe.repository.cassandra.RoomByCodeRepository;
import com.svi.tictactoe.service.impl.GameServiceImpl;
import org.junit.jupiter.api.Test;

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
        GameService service = harness.service();

        CreateGameResponse game = service.createGame(new CreateGameRequest("Alice"));
        JoinGameResponse secondPlayer = service.joinGame(game.roomCode(), joinRequest("Bob"));
        JoinGameResponse spectator = service.joinGame(game.roomCode(), joinRequest("Charlie"));

        assertNotNull(game.gameId());
        assertEquals(Symbol.X, game.player().symbol());
        assertEquals(Symbol.O, secondPlayer.participant().symbol());
        assertEquals(PlayerType.SPECTATOR, spectator.participant().type());
        assertNull(spectator.participant().symbol());
        assertEquals(1, service.getGameInfo(game.roomCode()).spectatorCount());
        assertEquals(GameStatus.IN_PROGRESS, service.getGameInfo(game.roomCode()).status());
        assertEquals(GameStatus.IN_PROGRESS, service.getBoardStatus(game.roomCode()).status());
    }

    @Test
    void coordinatesMovePlacementAndNextRound() {
        RepositoryHarness harness = new RepositoryHarness();
        GameService service = harness.service();
        CreateGameResponse game = service.createGame(new CreateGameRequest("Alice"));
        service.joinGame(game.roomCode(), joinRequest("Bob"));

        BoardResponse board = service.placeMove(game.gameId(), new AddMoveRequest(0, 0, Symbol.X));
        assertEquals(Symbol.X, board.grid()[0][0]);
        assertEquals(game.gameId(), board.gameId());

        PlayAgainResponse nextRound = service.playAgain(game.roomCode());
        assertEquals(2, nextRound.currentRound());
        assertEquals(game.roomCode(), nextRound.roomCode());
        assertNotEquals(game.gameId(), nextRound.gameId());
        assertNull(service.getBoardStatus(game.roomCode()).grid()[0][0]);
    }

    @Test
    void keepsScoresAndSpectatorsWithTheRoomAcrossRounds() {
        RepositoryHarness harness = new RepositoryHarness();
        GameService service = harness.service();
        CreateGameResponse game = service.createGame(new CreateGameRequest("Alice"));
        service.joinGame(game.roomCode(), joinRequest("Bob"));
        service.joinGame(game.roomCode(), joinRequest("Charlie"));

        service.placeMove(game.gameId(), new AddMoveRequest(0, 0, Symbol.X));
        service.placeMove(game.gameId(), new AddMoveRequest(1, 0, Symbol.O));
        service.placeMove(game.gameId(), new AddMoveRequest(0, 1, Symbol.X));
        service.placeMove(game.gameId(), new AddMoveRequest(1, 1, Symbol.O));
        service.placeMove(game.gameId(), new AddMoveRequest(0, 2, Symbol.X));

        GameInfoResponse completedRound = service.getGameInfo(game.roomCode());
        assertEquals(1, completedRound.players().getFirst().score());
        assertEquals(1, completedRound.spectatorCount());
        assertEquals(GameStatus.COMPLETED, completedRound.status());
        assertEquals("Alice", completedRound.winner());

        service.playAgain(game.roomCode());
        GameInfoResponse nextRound = service.getGameInfo(game.roomCode());
        assertEquals(1, nextRound.players().getFirst().score());
        assertEquals(1, nextRound.spectatorCount());
        assertNull(nextRound.winner());
    }

    private JoinGameRequest joinRequest(String playerName) {
        return new JoinGameRequest(playerName);
    }

    private static final class RepositoryHarness {

        private final RoomByCodeRepository roomRepository = mock(RoomByCodeRepository.class);
        private final GameByIdRepository gameByIdRepository = mock(GameByIdRepository.class);
        private final GameByRoomRepository gameByRoomRepository = mock(GameByRoomRepository.class);
        private final ParticipantByRoomRepository participantRepository = mock(ParticipantByRoomRepository.class);
        private final MoveByGameRepository moveRepository = mock(MoveByGameRepository.class);

        private final Map<String, RoomByCodeEntity> rooms = new HashMap<>();
        private final Map<UUID, GameByIdEntity> games = new HashMap<>();
        private final Map<String, List<GameByRoomEntity>> rounds = new HashMap<>();
        private final Map<String, List<ParticipantByRoomEntity>> participants = new HashMap<>();
        private final Map<UUID, List<MoveByGameEntity>> moves = new HashMap<>();

        private RepositoryHarness() {
            when(roomRepository.existsById(anyString()))
                    .thenAnswer(invocation -> rooms.containsKey(invocation.getArgument(0)));
            when(roomRepository.findById(anyString()))
                    .thenAnswer(invocation -> Optional.ofNullable(rooms.get(invocation.getArgument(0))));
            when(roomRepository.save(any(RoomByCodeEntity.class))).thenAnswer(invocation -> {
                RoomByCodeEntity entity = invocation.getArgument(0);
                rooms.put(entity.getRoomCode(), entity);
                return entity;
            });

            when(gameByIdRepository.findById(any(UUID.class)))
                    .thenAnswer(invocation -> Optional.ofNullable(games.get(invocation.getArgument(0))));
            when(gameByIdRepository.save(any(GameByIdEntity.class))).thenAnswer(invocation -> {
                GameByIdEntity entity = invocation.getArgument(0);
                games.put(entity.getGameId(), entity);
                return entity;
            });

            when(gameByRoomRepository.findAllByRoomCode(anyString()))
                    .thenAnswer(invocation -> new ArrayList<>(
                            rounds.getOrDefault(invocation.getArgument(0), List.of())));
            when(gameByRoomRepository.findByRoomCodeAndRoundNo(anyString(), anyInt()))
                    .thenAnswer(invocation -> rounds
                            .getOrDefault(invocation.getArgument(0), List.of())
                            .stream()
                            .filter(round -> round.getRoundNo().equals(invocation.getArgument(1)))
                            .findFirst());
            when(gameByRoomRepository.save(any(GameByRoomEntity.class))).thenAnswer(invocation -> {
                GameByRoomEntity entity = invocation.getArgument(0);
                List<GameByRoomEntity> roomRounds = rounds.computeIfAbsent(
                        entity.getRoomCode(), ignored -> new ArrayList<>());
                roomRounds.removeIf(round -> round.getRoundNo().equals(entity.getRoundNo()));
                roomRounds.add(entity);
                return entity;
            });

            when(participantRepository.findAllByRoomCode(anyString()))
                    .thenAnswer(invocation -> new ArrayList<>(
                            participants.getOrDefault(invocation.getArgument(0), List.of())));
            when(participantRepository.save(any(ParticipantByRoomEntity.class))).thenAnswer(invocation -> {
                ParticipantByRoomEntity entity = invocation.getArgument(0);
                List<ParticipantByRoomEntity> roomParticipants = participants.computeIfAbsent(
                        entity.getRoomCode(), ignored -> new ArrayList<>());
                roomParticipants.removeIf(participant -> participant.getNormalizedPlayerName()
                        .equals(entity.getNormalizedPlayerName()));
                roomParticipants.add(entity);
                return entity;
            });

            when(moveRepository.save(any(MoveByGameEntity.class))).thenAnswer(invocation -> {
                MoveByGameEntity entity = invocation.getArgument(0);
                moves.computeIfAbsent(entity.getGameId(), ignored -> new ArrayList<>()).add(entity);
                return entity;
            });
        }

        private GameService service() {
            return new GameServiceImpl(
                    roomRepository,
                    gameByIdRepository,
                    gameByRoomRepository,
                    participantRepository,
                    moveRepository
            );
        }
    }
}
