package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.player.PlayerGameSummaryResponse;
import com.svi.tictactoe.dto.response.player.PlayerGamesResponse;
import com.svi.tictactoe.dto.response.player.PlayerSummaryResponse;
import com.svi.tictactoe.dto.response.player.PlayersResponse;
import com.svi.tictactoe.entity.PlayerGameEntity;
import com.svi.tictactoe.entity.PlayerCatalogEntity;
import com.svi.tictactoe.exception.PlayerNotFoundException;
import com.svi.tictactoe.repository.cassandra.PlayerGameRepository;
import com.svi.tictactoe.repository.cassandra.PlayerCatalogRepository;
import com.svi.tictactoe.service.PlayerHistoryService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PlayerHistoryServiceImpl implements PlayerHistoryService {

    private final PlayerCatalogRepository playerCatalogRepository;
    private final PlayerGameRepository playerGameRepository;

    public PlayerHistoryServiceImpl(
            PlayerCatalogRepository playerCatalogRepository,
            PlayerGameRepository playerGameRepository) {
        this.playerCatalogRepository = playerCatalogRepository;
        this.playerGameRepository = playerGameRepository;
    }

    @Override
    public PlayersResponse getAllPlayers() {
        List<PlayerSummaryResponse> players = playerCatalogRepository
                .findAllByCatalogKey(PlayerCatalogEntity.ALL_PLAYERS).stream()
                .sorted(Comparator.comparing(PlayerCatalogEntity::getNormalizedPlayerName))
                .map(player -> new PlayerSummaryResponse(player.getPlayerName()))
                .toList();

        return new PlayersResponse(players.size(), players);
    }

    @Override
    public PlayerGamesResponse getPlayerGames(String playerName) {
        String normalizedPlayerName = normalizeName(playerName);

        PlayerCatalogEntity player = playerCatalogRepository
                .findByCatalogKeyAndNormalizedPlayerName(PlayerCatalogEntity.ALL_PLAYERS, normalizedPlayerName)
                .orElseThrow(() -> new PlayerNotFoundException(ErrorMessage.PLAYER_NOT_FOUND.format(playerName)));

        List<PlayerGameSummaryResponse> games = playerGameRepository
                .findAllByNormalizedPlayerName(normalizedPlayerName).stream()
                .sorted(Comparator.comparing(PlayerGameEntity::getRoomCode)
                        .thenComparing(PlayerGameEntity::getGameId))
                .map(this::toPlayerGameSummary)
                .toList();

        return new PlayerGamesResponse(player.getPlayerName(), games.size(), games);
    }

    private PlayerGameSummaryResponse toPlayerGameSummary(PlayerGameEntity game) {
        return new PlayerGameSummaryResponse(
                game.getRoomCode(),
                game.getGameId(),
                Symbol.fromString(game.getSymbol()),
                Boolean.TRUE.equals(game.getWon())
        );
    }

    private String normalizeName(String playerName) {
        return playerName.trim().toLowerCase(Locale.ROOT);
    }
}
