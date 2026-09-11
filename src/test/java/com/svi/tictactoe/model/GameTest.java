package com.svi.tictactoe.model;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.exception.GameNotStartedException;
import com.svi.tictactoe.exception.PlayerAlreadyExistsException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    @Test
    void assignsFirstTwoParticipantsAsPlayersAndLaterParticipantsAsSpectators() {
        Game game = new Game("ROOM", new ArrayList<>(), new Board());

        Player firstPlayer = game.join("Alice");
        Player secondPlayer = game.join("Bob");
        Player spectator = game.join("Charlie");

        assertEquals(PlayerType.PLAYER, firstPlayer.getType());
        assertEquals(Symbol.X, firstPlayer.getSymbol());
        assertEquals(PlayerType.PLAYER, secondPlayer.getType());
        assertEquals(Symbol.O, secondPlayer.getSymbol());
        assertEquals(PlayerType.SPECTATOR, spectator.getType());
        assertNull(spectator.getSymbol());
        assertEquals(2, game.getPlayers().size());
        assertEquals(1, game.getSpectators().size());
        assertTrue(game.isStarted());
    }

    @Test
    void preventsStartingNextRoundWithoutTwoPlayers() {
        Game game = new Game("ROOM", new ArrayList<>(), new Board());
        game.join("Alice");

        assertFalse(game.isStarted());
        assertThrows(GameNotStartedException.class, game::startNextRound);
    }

    @Test
    void startingNextRoundClearsBoardAndIncrementsRound() {
        Game game = new Game("ROOM", new ArrayList<>(), new Board());
        game.join("Alice");
        game.join("Bob");
        game.placeMove(Symbol.X, 0, 0);

        game.startNextRound();

        assertNull(game.getBoard().getGrid()[0][0]);
        assertEquals(2, game.getRound());
    }

    @Test
    void rejectsDuplicateNamesRegardlessOfCaseForPlayersAndSpectators() {
        Game game = new Game("ROOM", new ArrayList<>(), new Board());
        game.join("Alice");

        assertThrows(PlayerAlreadyExistsException.class, () -> game.join("alice"));

        game.join("Bob");
        game.join("Charlie");

        assertThrows(PlayerAlreadyExistsException.class, () -> game.join("CHARLIE"));
        assertEquals("Alice", game.getPlayers().getFirst().getPlayerName());
    }
}
