package com.svi.tictactoe.service;

import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.history.GameMoveHistoryResponse;
import com.svi.tictactoe.dto.response.history.RoomHistoriesResponse;
import com.svi.tictactoe.entity.GameByIdEntity;
import com.svi.tictactoe.entity.GameByRoomEntity;
import com.svi.tictactoe.entity.MoveByGameEntity;
import com.svi.tictactoe.entity.RoomByCodeEntity;
import com.svi.tictactoe.entity.RoomCatalogEntity;
import com.svi.tictactoe.mapper.GameMapper;
import com.svi.tictactoe.repository.cassandra.GameByIdRepository;
import com.svi.tictactoe.repository.cassandra.GameByRoomRepository;
import com.svi.tictactoe.repository.cassandra.MoveByGameRepository;
import com.svi.tictactoe.repository.cassandra.RoomByCodeRepository;
import com.svi.tictactoe.repository.cassandra.RoomCatalogRepository;
import com.svi.tictactoe.service.impl.HistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HistoryServiceImplTest {

    private static final String ROOM_CODE = "ROOM";
    private static final UUID FIRST_GAME_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_GAME_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        RoomCatalogRepository catalogRepository = mock(RoomCatalogRepository.class);
        RoomByCodeRepository roomRepository = mock(RoomByCodeRepository.class);
        GameByRoomRepository gameByRoomRepository = mock(GameByRoomRepository.class);
        GameByIdRepository gameByIdRepository = mock(GameByIdRepository.class);
        MoveByGameRepository moveRepository = mock(MoveByGameRepository.class);

        Instant createdAt = Instant.parse("2026-09-15T00:00:00Z");
        RoomByCodeEntity room = new RoomByCodeEntity(
                ROOM_CODE, SECOND_GAME_ID, 2, GameStatus.IN_PROGRESS.name(), createdAt);
        GameByIdEntity firstGame = new GameByIdEntity(
                FIRST_GAME_ID,
                ROOM_CODE,
                1,
                GameStatus.COMPLETED.name(),
                null,
                "Alice",
                5,
                List.of("X", "X", "X", "O", "O", "", "", "", ""));
        GameByIdEntity secondGame = new GameByIdEntity(
                SECOND_GAME_ID,
                ROOM_CODE,
                2,
                GameStatus.IN_PROGRESS.name(),
                Symbol.X.name(),
                null,
                0,
                GameMapper.emptyBoard());

        when(catalogRepository.findAllByCatalogKey(RoomCatalogEntity.ALL_ROOMS))
                .thenReturn(List.of(new RoomCatalogEntity(RoomCatalogEntity.ALL_ROOMS, ROOM_CODE)));
        when(roomRepository.findById(ROOM_CODE)).thenReturn(Optional.of(room));
        when(gameByRoomRepository.findAllByRoomCode(ROOM_CODE)).thenReturn(List.of(
                new GameByRoomEntity(
                        ROOM_CODE, 2, SECOND_GAME_ID, GameStatus.IN_PROGRESS.name(), createdAt.plusSeconds(10), null),
                new GameByRoomEntity(
                        ROOM_CODE, 1, FIRST_GAME_ID, GameStatus.COMPLETED.name(), createdAt, createdAt.plusSeconds(5))
        ));
        when(gameByIdRepository.findById(FIRST_GAME_ID)).thenReturn(Optional.of(firstGame));
        when(gameByIdRepository.findById(SECOND_GAME_ID)).thenReturn(Optional.of(secondGame));
        when(moveRepository.findAllByGameId(FIRST_GAME_ID)).thenReturn(List.of(
                new MoveByGameEntity(FIRST_GAME_ID, 2, ROOM_CODE, "Bob", "O", 1, 0, createdAt.plusSeconds(2)),
                new MoveByGameEntity(FIRST_GAME_ID, 1, ROOM_CODE, "Alice", "X", 0, 0, createdAt.plusSeconds(1))
        ));

        historyService = new HistoryServiceImpl(
                catalogRepository,
                roomRepository,
                gameByRoomRepository,
                gameByIdRepository,
                moveRepository);
    }

    @Test
    void returnsOnlySummariesForAllRooms() {
        RoomHistoriesResponse histories = historyService.getAllRoomHistories();

        assertEquals(1, histories.totalRooms());
        assertEquals(2, histories.totalGames());
        assertEquals(ROOM_CODE, histories.rooms().getFirst().roomCode());
        assertEquals(2, histories.rooms().getFirst().games().size());
        assertEquals(FIRST_GAME_ID, histories.rooms().getFirst().games().getFirst().gameId());
        assertEquals(GameStatus.COMPLETED, histories.rooms().getFirst().games().getFirst().status());
        assertEquals("Alice", histories.rooms().getFirst().games().getFirst().winner());
        assertEquals(SECOND_GAME_ID, histories.rooms().getFirst().games().getLast().gameId());
        assertEquals(GameStatus.IN_PROGRESS, histories.rooms().getFirst().games().getLast().status());
        assertNull(histories.rooms().getFirst().games().getLast().winner());
    }

    @Test
    void returnsOrderedMovesAndGameResult() {
        GameMoveHistoryResponse history = historyService.getGameMoves(FIRST_GAME_ID);

        assertEquals(FIRST_GAME_ID, history.gameId());
        assertEquals(1, history.moves().getFirst().moveNumber());
        assertEquals(2, history.moves().getLast().moveNumber());
        assertEquals(GameStatus.COMPLETED, history.result().status());
        assertEquals("Alice", history.result().winner());
    }
}
