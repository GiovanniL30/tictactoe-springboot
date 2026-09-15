package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.GameMoveHistoryResponse;
import com.svi.tictactoe.dto.response.GameResultResponse;
import com.svi.tictactoe.dto.response.GameHistorySummaryResponse;
import com.svi.tictactoe.dto.response.MoveResponse;
import com.svi.tictactoe.dto.response.RoomHistoriesResponse;
import com.svi.tictactoe.dto.response.RoomHistorySummaryResponse;
import com.svi.tictactoe.entity.GameByIdEntity;
import com.svi.tictactoe.entity.GameByRoomEntity;
import com.svi.tictactoe.entity.MoveByGameEntity;
import com.svi.tictactoe.entity.RoomByCodeEntity;
import com.svi.tictactoe.entity.RoomCatalogEntity;
import com.svi.tictactoe.exception.GameNotFoundException;
import com.svi.tictactoe.repository.cassandra.GameByIdRepository;
import com.svi.tictactoe.repository.cassandra.GameByRoomRepository;
import com.svi.tictactoe.repository.cassandra.MoveByGameRepository;
import com.svi.tictactoe.repository.cassandra.RoomByCodeRepository;
import com.svi.tictactoe.repository.cassandra.RoomCatalogRepository;
import com.svi.tictactoe.service.HistoryService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final RoomCatalogRepository roomCatalogRepository;
    private final RoomByCodeRepository roomRepository;
    private final GameByRoomRepository gameByRoomRepository;
    private final GameByIdRepository gameByIdRepository;
    private final MoveByGameRepository moveRepository;

    public HistoryServiceImpl(
            RoomCatalogRepository roomCatalogRepository,
            RoomByCodeRepository roomRepository,
            GameByRoomRepository gameByRoomRepository,
            GameByIdRepository gameByIdRepository,
            MoveByGameRepository moveRepository) {
        this.roomCatalogRepository = roomCatalogRepository;
        this.roomRepository = roomRepository;
        this.gameByRoomRepository = gameByRoomRepository;
        this.gameByIdRepository = gameByIdRepository;
        this.moveRepository = moveRepository;
    }

    @Override
    public RoomHistoriesResponse getAllRoomHistories() {
        List<RoomByCodeEntity> rooms = roomCatalogRepository
                .findAllByCatalogKey(RoomCatalogEntity.ALL_ROOMS).stream()
                .sorted(Comparator.comparing(RoomCatalogEntity::getRoomCode))
                .map(RoomCatalogEntity::getRoomCode)
                .map(roomRepository::findById)
                .flatMap(Optional::stream)
                .toList();

        List<RoomHistorySummaryResponse> summaries = rooms.stream()
                .map(this::toRoomHistorySummary)
                .toList();

        int totalGames = summaries.stream()
                .mapToInt(summary -> summary.games().size())
                .sum();

        return new RoomHistoriesResponse(summaries.size(), totalGames, summaries);
    }

    @Override
    public GameMoveHistoryResponse getGameMoves(UUID gameId) {
        GameByIdEntity game = requireGame(gameId);
        List<MoveResponse> moves = moveRepository.findAllByGameId(gameId).stream()
                .sorted(Comparator.comparing(MoveByGameEntity::getMoveNo))
                .map(this::toMoveResponse)
                .toList();

        GameResultResponse result = new GameResultResponse(
                GameStatus.valueOf(game.getStatus()),
                game.getWinner()
        );

        return new GameMoveHistoryResponse(
                game.getGameId(),
                game.getRoomCode(),
                game.getRoundNo(),
                moves,
                result
        );
    }

    private RoomHistorySummaryResponse toRoomHistorySummary(RoomByCodeEntity room) {
        List<GameHistorySummaryResponse> games = gameByRoomRepository
                .findAllByRoomCode(room.getRoomCode()).stream()
                .sorted(Comparator.comparing(GameByRoomEntity::getRoundNo))
                .map(this::toGameHistorySummary)
                .toList();

        return new RoomHistorySummaryResponse(
                room.getRoomCode(),
                games
        );
    }

    private GameHistorySummaryResponse toGameHistorySummary(GameByRoomEntity gameByRoom) {
        GameByIdEntity game = requireGame(gameByRoom.getGameId());

        return new GameHistorySummaryResponse(
                game.getGameId(),
                GameStatus.valueOf(game.getStatus()),
                game.getWinner()
        );
    }

    private MoveResponse toMoveResponse(MoveByGameEntity move) {
        return new MoveResponse(
                move.getMoveNo(),
                move.getPlayerName(),
                Symbol.fromString(move.getSymbol()),
                move.getX(),
                move.getY(),
                move.getPlayedAt()
        );
    }

    private GameByIdEntity requireGame(UUID gameId) {
        return gameByIdRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(
                        ErrorMessage.GAME_ID_NOT_FOUND.format(gameId)));
    }
}
