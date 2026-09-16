package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.history.GameHistorySummaryResponse;
import com.svi.tictactoe.dto.response.history.GameMoveHistoryResponse;
import com.svi.tictactoe.dto.response.history.GameResultResponse;
import com.svi.tictactoe.dto.response.history.MoveResponse;
import com.svi.tictactoe.dto.response.history.RoomHistoriesResponse;
import com.svi.tictactoe.dto.response.history.RoomHistorySummaryResponse;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.entity.GameRoundEntity;
import com.svi.tictactoe.entity.GameMoveEntity;
import com.svi.tictactoe.entity.RoomEntity;
import com.svi.tictactoe.entity.RoomCatalogEntity;
import com.svi.tictactoe.exception.GameNotFoundException;
import com.svi.tictactoe.repository.cassandra.GameRepository;
import com.svi.tictactoe.repository.cassandra.GameRoundRepository;
import com.svi.tictactoe.repository.cassandra.GameMoveRepository;
import com.svi.tictactoe.repository.cassandra.RoomRepository;
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
    private final RoomRepository roomRepository;
    private final GameRoundRepository gameRoundRepository;
    private final GameRepository gameRepository;
    private final GameMoveRepository moveRepository;

    public HistoryServiceImpl(
            RoomCatalogRepository roomCatalogRepository,
            RoomRepository roomRepository,
            GameRoundRepository gameRoundRepository,
            GameRepository gameRepository,
            GameMoveRepository moveRepository) {
        this.roomCatalogRepository = roomCatalogRepository;
        this.roomRepository = roomRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
    }

    @Override
    public RoomHistoriesResponse getAllRoomHistories() {
        List<RoomEntity> rooms = roomCatalogRepository
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
        GameEntity game = requireGame(gameId);
        List<MoveResponse> moves = moveRepository.findAllByGameId(gameId).stream()
                .sorted(Comparator.comparing(GameMoveEntity::getMoveNo))
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

    private RoomHistorySummaryResponse toRoomHistorySummary(RoomEntity room) {
        List<GameHistorySummaryResponse> games = gameRoundRepository
                .findAllByRoomCode(room.getRoomCode()).stream()
                .sorted(Comparator.comparing(GameRoundEntity::getRoundNo))
                .map(this::toGameHistorySummary)
                .toList();

        return new RoomHistorySummaryResponse(
                room.getRoomCode(),
                games
        );
    }

    private GameHistorySummaryResponse toGameHistorySummary(GameRoundEntity gameRound) {
        GameEntity game = requireGame(gameRound.getGameId());

        return new GameHistorySummaryResponse(
                game.getGameId(),
                GameStatus.valueOf(game.getStatus()),
                game.getWinner()
        );
    }

    private MoveResponse toMoveResponse(GameMoveEntity move) {
        return new MoveResponse(
                move.getMoveNo(),
                move.getPlayerName(),
                Symbol.fromString(move.getSymbol()),
                move.getX(),
                move.getY(),
                move.getPlayedAt()
        );
    }

    private GameEntity requireGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(
                        ErrorMessage.GAME_ID_NOT_FOUND.format(gameId)));
    }
}
