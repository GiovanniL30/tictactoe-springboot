package com.svi.tictactoe.repository;

import com.svi.tictactoe.model.Board;
import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.repository.impl.GameRepositoryImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRepositoryImplTest {

    @Test
    void savesAndFindsGameByRoomCode() {
        GameRepository repository = new GameRepositoryImpl();
        Game game = new Game("ROOM", new ArrayList<>(), new Board());

        repository.save(game);

        assertSame(game, repository.findByRoomCode("ROOM").orElseThrow());
        assertSame(game, repository.findByActiveGameId(game.getActiveGameId()).orElseThrow());
        assertTrue(repository.findByRoomCode("MISSING").isEmpty());
    }
}
