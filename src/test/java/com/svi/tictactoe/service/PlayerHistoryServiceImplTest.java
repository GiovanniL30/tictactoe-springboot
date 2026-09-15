package com.svi.tictactoe.service;

import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.player.PlayerGamesResponse;
import com.svi.tictactoe.dto.response.player.PlayersResponse;
import com.svi.tictactoe.entity.GameByPlayerEntity;
import com.svi.tictactoe.entity.PlayerCatalogEntity;
import com.svi.tictactoe.exception.PlayerNotFoundException;
import com.svi.tictactoe.repository.cassandra.GameByPlayerRepository;
import com.svi.tictactoe.repository.cassandra.PlayerCatalogRepository;
import com.svi.tictactoe.service.impl.PlayerHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerHistoryServiceImplTest {

    private static final String NORMALIZED_NAME = "alice";

    private PlayerCatalogRepository playerCatalogRepository;
    private GameByPlayerRepository gameByPlayerRepository;
    private PlayerHistoryService playerHistoryService;

    @BeforeEach
    void setUp() {
        playerCatalogRepository = mock(PlayerCatalogRepository.class);
        gameByPlayerRepository = mock(GameByPlayerRepository.class);
        playerHistoryService = new PlayerHistoryServiceImpl(
                playerCatalogRepository,
                gameByPlayerRepository
        );
    }

    @Test
    void returnsAllPlayersInNameOrder() {
        when(playerCatalogRepository.findAllByCatalogKey(PlayerCatalogEntity.ALL_PLAYERS))
                .thenReturn(List.of(
                        player("bob", "Bob"),
                        player(NORMALIZED_NAME, "Alice")
                ));

        PlayersResponse response = playerHistoryService.getAllPlayers();

        assertEquals(2, response.totalPlayers());
        assertEquals("Alice", response.players().getFirst().playerName());
        assertEquals("Bob", response.players().getLast().playerName());
    }

    @Test
    void returnsGamesForPlayerUsingCaseInsensitiveName() {
        UUID olderGameId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID newerGameId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(playerCatalogRepository.findByCatalogKeyAndNormalizedPlayerName(
                PlayerCatalogEntity.ALL_PLAYERS,
                NORMALIZED_NAME))
                .thenReturn(Optional.of(player(NORMALIZED_NAME, "Alice")));
        when(gameByPlayerRepository.findAllByNormalizedPlayerName(NORMALIZED_NAME))
                .thenReturn(List.of(
                        game(newerGameId, false),
                        game(olderGameId, true)
                ));

        PlayerGamesResponse response = playerHistoryService.getPlayerGames(" ALICE ");

        assertEquals("Alice", response.playerName());
        assertEquals(2, response.totalGames());
        var wonGame = response.games().stream()
                .filter(game -> olderGameId.equals(game.gameId()))
                .findFirst()
                .orElseThrow();
        assertEquals(Symbol.X, wonGame.symbol());
        assertTrue(wonGame.won());
    }

    @Test
    void rejectsUnknownPlayer() {
        when(playerCatalogRepository.findByCatalogKeyAndNormalizedPlayerName(
                PlayerCatalogEntity.ALL_PLAYERS,
                "unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                PlayerNotFoundException.class,
                () -> playerHistoryService.getPlayerGames("Unknown")
        );
    }

    private PlayerCatalogEntity player(String normalizedName, String playerName) {
        return new PlayerCatalogEntity(PlayerCatalogEntity.ALL_PLAYERS, normalizedName, playerName);
    }

    private GameByPlayerEntity game(UUID gameId, boolean won) {
        return new GameByPlayerEntity(
                NORMALIZED_NAME,
                gameId,
                "ROOM",
                Symbol.X.name(),
                won
        );
    }
}
