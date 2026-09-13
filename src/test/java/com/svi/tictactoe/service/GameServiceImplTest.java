package com.svi.tictactoe.service;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.BoardResponse;
import com.svi.tictactoe.dto.response.CreateGameResponse;
import com.svi.tictactoe.dto.response.JoinGameResponse;
import com.svi.tictactoe.dto.response.PlayAgainResponse;
import com.svi.tictactoe.repository.impl.GameRepositoryImpl;
import com.svi.tictactoe.service.impl.GameServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameServiceImplTest {

    @Test
    void coordinatesCreationJoiningAndSpectatorAssignment() {
        GameService service = new GameServiceImpl(new GameRepositoryImpl());
        CreateGameRequest createRequest = new CreateGameRequest("Alice");

        CreateGameResponse game = service.createGame(createRequest);
        JoinGameResponse secondPlayer = service.joinGame(game.roomCode(), joinRequest("Bob"));
        JoinGameResponse spectator = service.joinGame(game.roomCode(), joinRequest("Charlie"));

        assertNotNull(game.gameId());
        assertEquals(Symbol.X, game.player().getSymbol());
        assertEquals(Symbol.O, secondPlayer.participant().getSymbol());
        assertEquals(PlayerType.SPECTATOR, spectator.participant().getType());
        assertNull(spectator.participant().getSymbol());
    }

    @Test
    void coordinatesMovePlacementAndNextRound() {
        GameService service = new GameServiceImpl(new GameRepositoryImpl());
        CreateGameRequest createRequest = new CreateGameRequest("Alice");
        CreateGameResponse game = service.createGame(createRequest);
        service.joinGame(game.roomCode(), joinRequest("Bob"));

        AddMoveRequest moveRequest = new AddMoveRequest(0, 0, Symbol.X);

        BoardResponse board = service.placeMove(game.gameId(), moveRequest);
        assertEquals(Symbol.X, board.grid()[0][0]);
        assertEquals(game.gameId(), board.gameId());

        PlayAgainResponse nextRound = service.playAgain(game.roomCode());

        assertEquals(2, nextRound.currentRound());
        assertEquals(game.roomCode(), nextRound.roomCode());
        assertNotEquals(game.gameId(), nextRound.gameId());
        assertNull(service.getBoardStatus(game.roomCode()).grid()[0][0]);
    }

    private JoinGameRequest joinRequest(String playerName) {
        return new JoinGameRequest(playerName);
    }
}
