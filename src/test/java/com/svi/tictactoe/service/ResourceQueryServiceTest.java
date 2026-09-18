package com.svi.tictactoe.service;

import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.game.GameInfoResponse;
import com.svi.tictactoe.dto.response.game.GameMovesResponse;
import com.svi.tictactoe.dto.response.room.RoomInfoResponse;
import com.svi.tictactoe.dto.response.room.RoomsResponse;
import com.svi.tictactoe.engine.GameEngine;
import com.svi.tictactoe.entity.*;
import com.svi.tictactoe.repository.cassandra.*;
import com.svi.tictactoe.service.impl.GameServiceImpl;
import com.svi.tictactoe.service.impl.RoomServiceImpl;
import com.svi.tictactoe.service.support.GameLookup;
import com.svi.tictactoe.service.support.PlayerGameSynchronizer;
import com.svi.tictactoe.util.BoardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ResourceQueryServiceTest {

    private static final String ROOM_CODE = "ROOM";
    private static final UUID FIRST_GAME_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_GAME_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final Instant CREATED_AT = Instant.parse("2026-09-15T00:00:00Z");

    private RoomService roomService;
    private GameService gameService;
    private GameRepository gameRepository;
    private GameRoundRepository gameRoundRepository;
    private RoomRepository roomRepository;

    @BeforeEach
    void setUp() {
        RoomCatalogRepository catalogRepository = mock(RoomCatalogRepository.class);
        roomRepository = mock(RoomRepository.class);
        gameRoundRepository = mock(GameRoundRepository.class);
        gameRepository = mock(GameRepository.class);
        GameMoveRepository moveRepository = mock(GameMoveRepository.class);
        RoomPlayerRepository roomPlayerRepository = mock(RoomPlayerRepository.class);
        PlayerCatalogRepository playerCatalogRepository = mock(PlayerCatalogRepository.class);
        PlayerGameRepository playerGameRepository = mock(PlayerGameRepository.class);
        ApplicationEventPublisher eventPublisher = event -> {
        };
        PlayerGameSynchronizer playerGameSynchronizer = new PlayerGameSynchronizer(
                playerCatalogRepository,
                playerGameRepository);
        GameLookup gameLookup = new GameLookup(roomRepository, gameRepository, gameRoundRepository, roomPlayerRepository);

        RoomEntity room = new RoomEntity(
                ROOM_CODE, SECOND_GAME_ID, 2, GameStatus.IN_PROGRESS.name(), CREATED_AT);
        GameEntity firstGame = new GameEntity(
                FIRST_GAME_ID,
                ROOM_CODE,
                1,
                GameStatus.COMPLETED.name(),
                null,
                "Alice",
                5,
                List.of("X", "X", "X", "O", "O", "", "", "", ""));
        GameEntity secondGame = new GameEntity(
                SECOND_GAME_ID,
                ROOM_CODE,
                2,
                GameStatus.IN_PROGRESS.name(),
                Symbol.X.name(),
                null,
                0,
                BoardUtil.emptyBoard());

        when(catalogRepository.findAllByCatalogKey(RoomCatalogEntity.ALL_ROOMS))
                .thenReturn(List.of(new RoomCatalogEntity(RoomCatalogEntity.ALL_ROOMS, ROOM_CODE)));
        when(roomRepository.findById(ROOM_CODE)).thenReturn(Optional.of(room));
        when(roomRepository.findAllById(any())).thenReturn(List.of(room));
        GameRoundEntity secondRound = new GameRoundEntity(
                ROOM_CODE, 2, SECOND_GAME_ID, GameStatus.IN_PROGRESS.name(), CREATED_AT.plusSeconds(10), null);
        GameRoundEntity firstRound = new GameRoundEntity(
                ROOM_CODE, 1, FIRST_GAME_ID, GameStatus.COMPLETED.name(), CREATED_AT, CREATED_AT.plusSeconds(5));
        when(gameRoundRepository.findAllByRoomCode(ROOM_CODE)).thenReturn(List.of(secondRound, firstRound));
        when(gameRoundRepository.findAllByRoomCodeIn(any())).thenReturn(List.of(secondRound, firstRound));
        when(gameRoundRepository.findByRoomCodeAndRoundNo(ROOM_CODE, 1))
                .thenReturn(Optional.of(firstRound));
        when(gameRoundRepository.findByRoomCodeAndRoundNo(ROOM_CODE, 2))
                .thenReturn(Optional.of(secondRound));
        when(gameRepository.findById(FIRST_GAME_ID)).thenReturn(Optional.of(firstGame));
        when(gameRepository.findById(SECOND_GAME_ID)).thenReturn(Optional.of(secondGame));
        when(gameRepository.findAllById(any())).thenReturn(List.of(firstGame, secondGame));
        when(moveRepository.findAllByGameId(FIRST_GAME_ID)).thenReturn(List.of(
                new GameMoveEntity(FIRST_GAME_ID, 2, ROOM_CODE, "Bob", "O", 1, 0, CREATED_AT.plusSeconds(2)),
                new GameMoveEntity(FIRST_GAME_ID, 1, ROOM_CODE, "Alice", "X", 0, 0, CREATED_AT.plusSeconds(1))
        ));
        when(roomPlayerRepository.findAllByRoomCode(ROOM_CODE)).thenReturn(List.of());

        roomService = new RoomServiceImpl(
                roomRepository,
                catalogRepository,
                gameRepository,
                gameRoundRepository,
                roomPlayerRepository,
                moveRepository,
                playerCatalogRepository,
                playerGameRepository,
                playerGameSynchronizer,
                gameLookup,
                eventPublisher);
        gameService = new GameServiceImpl(
                roomRepository,
                gameRepository,
                gameRoundRepository,
                roomPlayerRepository,
                moveRepository,
                playerGameSynchronizer,
                new GameEngine(),
                gameLookup,
                eventPublisher);
    }

    @Test
    void returnsRoomInformationWithOrderedGameSummaries() {
        RoomInfoResponse room = roomService.getRoom(ROOM_CODE);

        assertEquals(ROOM_CODE, room.roomCode());
        assertEquals(CREATED_AT, room.createdAt());
        assertEquals(2, room.games().size());
        assertEquals(FIRST_GAME_ID, room.games().getFirst().gameId());
        assertEquals(GameStatus.COMPLETED, room.games().getFirst().status());
        assertEquals("Alice", room.games().getFirst().winner());
        assertEquals(CREATED_AT, room.games().getFirst().createdAt());
        assertEquals(CREATED_AT.plusSeconds(5), room.games().getFirst().endedAt());
        assertEquals(SECOND_GAME_ID, room.games().getLast().gameId());
        assertEquals(GameStatus.IN_PROGRESS, room.games().getLast().status());
        assertEquals(CREATED_AT.plusSeconds(10), room.games().getLast().createdAt());
        assertNull(room.games().getLast().endedAt());
        assertNull(room.games().getLast().winner());
        verify(gameRepository, times(1)).findAllById(any());
        verify(gameRepository, never()).findById(any(UUID.class));
    }

    @Test
    void returnsAllRoomsWithoutAHistoryResource() {
        RoomsResponse rooms = roomService.getRooms();

        assertEquals(1, rooms.totalRooms());
        assertEquals(2, rooms.totalGames());
        assertEquals(ROOM_CODE, rooms.rooms().getFirst().roomCode());
        verify(roomRepository, times(1)).findAllById(any());
        verify(roomRepository, never()).findById(anyString());
        verify(gameRoundRepository, times(1)).findAllByRoomCodeIn(any());
        verify(gameRoundRepository, never()).findAllByRoomCode(anyString());
        verify(gameRepository, times(1)).findAllById(any());
        verify(gameRepository, never()).findById(any(UUID.class));
    }

    @Test
    void returnsGameInformationAndOrderedMovesByGameId() {
        GameInfoResponse game = gameService.getGame(FIRST_GAME_ID);
        GameMovesResponse moves = gameService.getMoves(FIRST_GAME_ID);

        assertEquals(FIRST_GAME_ID, game.gameId());
        assertEquals(GameStatus.COMPLETED, game.status());
        assertEquals("Alice", game.winner());
        assertEquals(CREATED_AT, game.createdAt());
        assertEquals(CREATED_AT.plusSeconds(5), game.endedAt());
        assertEquals(1, moves.moves().getFirst().moveNumber());
        assertEquals(CREATED_AT.plusSeconds(1), moves.moves().getFirst().playedAt());
        assertEquals(2, moves.moves().getLast().moveNumber());
        assertEquals(CREATED_AT.plusSeconds(2), moves.moves().getLast().playedAt());
        assertEquals(GameStatus.COMPLETED, moves.result().status());
        assertEquals("Alice", moves.result().winner());
        assertEquals(CREATED_AT.plusSeconds(5), moves.result().endedAt());
    }
}
