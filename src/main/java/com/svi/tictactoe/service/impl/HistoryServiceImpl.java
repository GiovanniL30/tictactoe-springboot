package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.constants.GameStatus;
import com.svi.tictactoe.constants.SuccessMessage;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.GameInfoResponse;
import com.svi.tictactoe.dto.response.GameMoveHistoryResponse;
import com.svi.tictactoe.dto.response.GameResultResponse;
import com.svi.tictactoe.dto.response.MoveResponse;
import com.svi.tictactoe.dto.response.RoomHistoryResponse;
import com.svi.tictactoe.entity.GameByIdEntity;
import com.svi.tictactoe.entity.MoveByGameEntity;
import com.svi.tictactoe.entity.ParticipantByRoomEntity;
import com.svi.tictactoe.entity.RoomByCodeEntity;
import com.svi.tictactoe.entity.RoomCatalogEntity;
import com.svi.tictactoe.exception.GameNotFoundException;
import com.svi.tictactoe.mapper.GameMapper;
import com.svi.tictactoe.mapper.PlayerMapper;
import com.svi.tictactoe.repository.cassandra.GameByIdRepository;
import com.svi.tictactoe.repository.cassandra.GameByRoomRepository;
import com.svi.tictactoe.repository.cassandra.MoveByGameRepository;
import com.svi.tictactoe.repository.cassandra.ParticipantByRoomRepository;
import com.svi.tictactoe.repository.cassandra.RoomByCodeRepository;
import com.svi.tictactoe.repository.cassandra.RoomCatalogRepository;
import com.svi.tictactoe.service.HistoryService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final RoomCatalogRepository roomCatalogRepository;
    private final RoomByCodeRepository roomRepository;
    private final GameByRoomRepository gameByRoomRepository;
    private final GameByIdRepository gameByIdRepository;
    private final ParticipantByRoomRepository participantRepository;
    private final MoveByGameRepository moveRepository;

    public HistoryServiceImpl(
            RoomCatalogRepository roomCatalogRepository,
            RoomByCodeRepository roomRepository,
            GameByRoomRepository gameByRoomRepository,
            GameByIdRepository gameByIdRepository,
            ParticipantByRoomRepository participantRepository,
            MoveByGameRepository moveRepository) {
        this.roomCatalogRepository = roomCatalogRepository;
        this.roomRepository = roomRepository;
        this.gameByRoomRepository = gameByRoomRepository;
        this.gameByIdRepository = gameByIdRepository;
        this.participantRepository = participantRepository;
        this.moveRepository = moveRepository;
    }

    @Override
    public List<RoomHistoryResponse> getAllRoomHistories() {
        return roomCatalogRepository.findAllByCatalogKey(RoomCatalogEntity.ALL_ROOMS).stream()
                .sorted(Comparator.comparing(RoomCatalogEntity::getRoomCode))
                .map(RoomCatalogEntity::getRoomCode)
                .map(roomRepository::findById)
                .flatMap(java.util.Optional::stream)
                .map(this::toRoomHistory)
                .toList();
    }

    @Override
    public RoomHistoryResponse getRoomHistory(String roomCode) {
        return toRoomHistory(requireRoom(roomCode));
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

    private RoomHistoryResponse toRoomHistory(RoomByCodeEntity room) {
        List<ParticipantByRoomEntity> participants = participantRepository.findAllByRoomCode(room.getRoomCode());
        PlayerMapper.ParticipantSummary summary = PlayerMapper.summarize(participants);

        List<GameInfoResponse> games = gameByRoomRepository.findAllByRoomCode(room.getRoomCode()).stream()
                .sorted(Comparator.comparingInt(round -> round.getRoundNo()))
                .map(round -> gameByIdRepository.findById(round.getGameId()))
                .flatMap(java.util.Optional::stream)
                .map(game -> GameMapper.toGameInfoResponse(
                        game,
                        summary.players(),
                        summary.spectatorCount(),
                        SuccessMessage.GAME_INFO_RETRIEVED.getMessage()))
                .toList();

        return new RoomHistoryResponse(room.getRoomCode(), games);
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

    private RoomByCodeEntity requireRoom(String roomCode) {
        return roomRepository.findById(roomCode)
                .orElseThrow(() -> new GameNotFoundException(
                        ErrorMessage.GAME_NOT_FOUND.format(roomCode)));
    }

    private GameByIdEntity requireGame(UUID gameId) {
        return gameByIdRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(
                        ErrorMessage.GAME_ID_NOT_FOUND.format(gameId)));
    }
}
