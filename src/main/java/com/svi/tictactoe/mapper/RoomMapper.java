package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.game.CreateGameResponse;
import com.svi.tictactoe.dto.response.game.LeaveGameResponse;
import com.svi.tictactoe.dto.response.game.PlayAgainResponse;
import com.svi.tictactoe.dto.response.room.GameSummaryResponse;
import com.svi.tictactoe.dto.response.room.RoomInfoResponse;
import com.svi.tictactoe.dto.response.room.RoomsResponse;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.entity.GameRoundEntity;
import com.svi.tictactoe.entity.RoomEntity;
import com.svi.tictactoe.entity.RoomPlayerEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static CreateGameResponse toCreateGameResponse(RoomEntity room, GameEntity game, RoomPlayerEntity creator, String message) {
        return new CreateGameResponse(
                message,
                room.getRoomCode(),
                game.getGameId(),
                room.getCreatedAt(),
                PlayerMapper.toPlayerResponse(creator)
        );
    }

    public static LeaveGameResponse toLeaveGameResponse(String message) {
        return new LeaveGameResponse(message);
    }

    public static PlayAgainResponse toPlayAgainResponse(RoomEntity entity, GameRoundEntity round, String message) {
        return new PlayAgainResponse(
                message,
                entity.getRoomCode(),
                entity.getActiveGameId(),
                entity.getCurrentRound(),
                Symbol.X,
                round.getCreatedAt()
        );
    }

    public static RoomInfoResponse toRoomInfoResponse(RoomEntity room, List<GameRoundEntity> rounds, Map<UUID, GameEntity> gamesById) {
        List<GameSummaryResponse> games = rounds.stream()
                .sorted(Comparator.comparing(GameRoundEntity::getRoundNo))
                .map(round -> GameMapper.toGameSummaryResponse(gamesById.get(round.getGameId()), round))
                .toList();

        return new RoomInfoResponse(
                room.getRoomCode(),
                room.getCreatedAt(),
                games
        );
    }

    public static RoomsResponse toRoomsResponse(List<RoomInfoResponse> rooms) {
        int totalGames = rooms.stream()
                .mapToInt(room -> room.games().size())
                .sum();

        return new RoomsResponse(rooms.size(), totalGames, rooms);
    }

}
