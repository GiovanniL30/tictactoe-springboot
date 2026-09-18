package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.game.PlayAgainResponse;
import com.svi.tictactoe.dto.response.room.GameSummaryResponse;
import com.svi.tictactoe.dto.response.room.RoomInfoResponse;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.entity.GameRoundEntity;
import com.svi.tictactoe.entity.RoomEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static PlayAgainResponse toPlayAgainResponse(
            RoomEntity entity,
            GameRoundEntity round,
            String message) {
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

}
