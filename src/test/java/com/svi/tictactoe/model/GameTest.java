package com.svi.tictactoe.model;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.exception.GameNotStartedException;
import com.svi.tictactoe.exception.InvalidTurnException;
import com.svi.tictactoe.exception.InvalidPositionException;
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
        assertEquals(Symbol.X, game.getCurrentTurn());
    }

    @Test
    void alternatesTurnsStartingWithX() {
        Game game = new Game("ROOM", new ArrayList<>(), new Board());
        game.join("Alice");
        game.join("Bob");

        assertEquals(Symbol.X, game.getCurrentTurn());

        game.placeMove(Symbol.X, 0, 0);
        assertEquals(Symbol.O, game.getCurrentTurn());

        assertThrows(InvalidTurnException.class, () -> game.placeMove(Symbol.X, 0, 1));
        assertEquals(Symbol.O, game.getCurrentTurn());

        game.placeMove(Symbol.O, 0, 1);
        assertEquals(Symbol.X, game.getCurrentTurn());
    }

    @Test
    void preventsMovesUntilTwoPlayersHaveJoined() {
        Game game = new Game("ROOM", new ArrayList<>(), new Board());
        game.join("Alice");

        assertThrows(GameNotStartedException.class, () -> game.placeMove(Symbol.X, 0, 0));
    }

    @Test
    void rejectsPositionsOutsideTheBoard() {
        Game game = new Game("ROOM", new ArrayList<>(), new Board());
        game.join("Alice");
        game.join("Bob");

        assertThrows(InvalidPositionException.class, () -> game.placeMove(Symbol.X, 3, 0));
        assertEquals(Symbol.X, game.getCurrentTurn());
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
