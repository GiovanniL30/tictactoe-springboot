package com.svi.tictactoe.repository;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.model.Player;
import com.svi.tictactoe.repository.impl.GameRepositoryImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameRepositoryImplTest {

    @Test
    void assignsCreatorAsXSecondJoinAsOAndLaterJoinsAsSpectators() {
        GameRepository repository = new GameRepositoryImpl();

        Game game = repository.createGame("Alice");
        Player secondPlayer = repository.joinGame(game.getRoomCode(), "Bob");
        Player spectator = repository.joinGame(game.getRoomCode(), "Charlie");

        Player creator = game.getPlayers().getFirst();
        assertEquals("Alice", creator.getPlayerName());
        assertEquals(Symbol.X, creator.getSymbol());
        assertEquals(PlayerType.PLAYER, creator.getType());
        assertEquals(Symbol.O, secondPlayer.getSymbol());
        assertEquals(PlayerType.PLAYER, secondPlayer.getType());
        assertEquals(PlayerType.SPECTATOR, spectator.getType());
        assertNull(spectator.getSymbol());
    }
}
