package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.dto.response.player.PlayerGamesResponse;
import com.svi.tictactoe.dto.response.player.PlayersResponse;
import com.svi.tictactoe.entity.PlayerCatalogEntity;
import com.svi.tictactoe.entity.PlayerGameEntity;
import com.svi.tictactoe.exception.PlayerNotFoundException;
import com.svi.tictactoe.mapper.PlayerMapper;
import com.svi.tictactoe.repository.cassandra.PlayerCatalogRepository;
import com.svi.tictactoe.repository.cassandra.PlayerGameRepository;
import com.svi.tictactoe.service.PlayerHistoryService;
import com.svi.tictactoe.util.PlayerNameUtil;
import org.springframework.stereotype.Service;

import java.util.List;


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
        List<PlayerCatalogEntity> players = playerCatalogRepository.findAllByCatalogKey(PlayerCatalogEntity.ALL_PLAYERS);
        return PlayerMapper.toPlayersResponse(players);
    }

    @Override
    public PlayerGamesResponse getPlayerGames(String playerName) {
        String normalizedPlayerName = PlayerNameUtil.normalize(playerName);

        PlayerCatalogEntity player = playerCatalogRepository
                .findByCatalogKeyAndNormalizedPlayerName(PlayerCatalogEntity.ALL_PLAYERS, normalizedPlayerName)
                .orElseThrow(() -> new PlayerNotFoundException(ErrorMessage.PLAYER_NOT_FOUND.format(playerName)));

        List<PlayerGameEntity> playerGameEntities = playerGameRepository.findAllByNormalizedPlayerName(normalizedPlayerName);

        return PlayerMapper.toPlayerGamesResponse(playerGameEntities, player.getPlayerName());
    }

}
