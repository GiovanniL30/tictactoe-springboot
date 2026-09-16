package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.game.JoinGameResponse;
import com.svi.tictactoe.dto.response.player.PlayerResponse;
import com.svi.tictactoe.entity.RoomPlayerEntity;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PlayerMapper {

    private PlayerMapper() {
    }

    public static JoinGameResponse toJoinGameResponse(
            RoomPlayerEntity player,
            UUID gameId,
            String message) {
        return new JoinGameResponse(message, gameId, toPlayerResponse(player));
    }

    public static PlayerResponse toPlayerResponse(RoomPlayerEntity entity) {
        return new PlayerResponse(
                entity.getPlayerName(),
                entity.getScore() == null ? 0 : entity.getScore(),
                toSymbol(entity.getSymbol()),
                PlayerType.valueOf(entity.getPlayerType())
        );
    }

    public static PlayerSummary summarize(List<RoomPlayerEntity> roomPlayers) {
        List<PlayerResponse> players = roomPlayers.stream()
                .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                .sorted(Comparator.comparingInt(player -> Symbol.fromString(player.getSymbol()).ordinal()))
                .map(PlayerMapper::toPlayerResponse)
                .toList();

        int spectatorCount = (int) roomPlayers.stream()
                .filter(player -> PlayerType.SPECTATOR.name().equals(player.getPlayerType()))
                .count();

        return new PlayerSummary(players, spectatorCount);
    }

    private static Symbol toSymbol(String value) {
        return value == null || value.isBlank() ? null : Symbol.fromString(value);
    }

    public record PlayerSummary(
            List<PlayerResponse> players,
            int spectatorCount) {
    }
}
