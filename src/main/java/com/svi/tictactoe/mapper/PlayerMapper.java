package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.room.JoinRoomResponse;
import com.svi.tictactoe.dto.response.player.*;
import com.svi.tictactoe.entity.PlayerCatalogEntity;
import com.svi.tictactoe.entity.PlayerGameEntity;
import com.svi.tictactoe.entity.RoomPlayerEntity;
import com.svi.tictactoe.util.BoardUtil;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PlayerMapper {

    private PlayerMapper() {
    }

    public static JoinRoomResponse toJoinGameResponse(RoomPlayerEntity player, UUID gameId, String message) {
        return new JoinRoomResponse(message, gameId, toPlayerResponse(player));
    }

    public static PlayerResponse toPlayerResponse(RoomPlayerEntity entity) {
        return new PlayerResponse(
                entity.getPlayerName(),
                entity.getScore() == null ? 0 : entity.getScore(),
                BoardUtil.toSymbol(entity.getSymbol()),
                PlayerType.valueOf(entity.getPlayerType()),
                entity.getJoinedAt()
        );
    }

    public static PlayersResponse toPlayersResponse(List<PlayerCatalogEntity> playerCatalogEntities) {
        List<PlayerListResponse> playerListResponse = playerCatalogEntities.stream()
                .sorted(Comparator.comparing(PlayerCatalogEntity::getNormalizedPlayerName))
                .map(player -> new PlayerListResponse(player.getPlayerName()))
                .toList();

        return new PlayersResponse(playerListResponse.size(), playerListResponse);
    }

    public static PlayerGamesResponse toPlayerGamesResponse(List<PlayerGameEntity> playerGameEntities, String playerName) {
        List<PlayerGameSummaryResponse> playerGamesResponses = playerGameEntities.stream()
                .sorted(Comparator.comparing(PlayerGameEntity::getRoomCode).thenComparing(PlayerGameEntity::getGameId))
                .map(PlayerMapper::toPlayerGameSummary)
                .toList();

        return new PlayerGamesResponse(playerName, playerGamesResponses.size(), playerGamesResponses);
    }

    public static PlayerGameSummaryResponse toPlayerGameSummary(PlayerGameEntity game) {
        return new PlayerGameSummaryResponse(
                game.getRoomCode(),
                game.getGameId(),
                Symbol.fromString(game.getSymbol()),
                Boolean.TRUE.equals(game.getWon())
        );
    }

    public static PlayersSummaryResponse summarize(List<RoomPlayerEntity> roomPlayers) {
        List<PlayerResponse> players = roomPlayers.stream()
                .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                .sorted(Comparator.comparingInt(player -> Symbol.fromString(player.getSymbol()).ordinal()))
                .map(PlayerMapper::toPlayerResponse)
                .toList();

        int spectatorCount = (int) roomPlayers.stream()
                .filter(player -> PlayerType.SPECTATOR.name().equals(player.getPlayerType()))
                .count();

        return new PlayersSummaryResponse(players, spectatorCount);
    }
}
