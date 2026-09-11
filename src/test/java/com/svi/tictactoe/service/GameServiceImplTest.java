package com.svi.tictactoe.service;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.model.Player;
import com.svi.tictactoe.repository.impl.GameRepositoryImpl;
import com.svi.tictactoe.service.impl.GameServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameServiceImplTest {

    @Test
    void coordinatesCreationJoiningAndSpectatorAssignment() {
        GameService service = new GameServiceImpl(new GameRepositoryImpl());
        CreateGameRequest createRequest = new CreateGameRequest();
        createRequest.setPlayerName("Alice");

        Game game = service.createGame(createRequest);
        Player secondPlayer = service.joinGame(game.getRoomCode(), joinRequest("Bob"));
        Player spectator = service.joinGame(game.getRoomCode(), joinRequest("Charlie"));

        assertEquals(Symbol.X, game.getPlayers().getFirst().getSymbol());
        assertEquals(Symbol.O, secondPlayer.getSymbol());
        assertEquals(PlayerType.SPECTATOR, spectator.getType());
        assertNull(spectator.getSymbol());
    }

    @Test
    void coordinatesMovePlacementAndNextRound() {
        GameService service = new GameServiceImpl(new GameRepositoryImpl());
        CreateGameRequest createRequest = new CreateGameRequest();
        createRequest.setPlayerName("Alice");
        Game game = service.createGame(createRequest);
        service.joinGame(game.getRoomCode(), joinRequest("Bob"));

        AddMoveRequest moveRequest = new AddMoveRequest();
        moveRequest.setX(0);
        moveRequest.setY(0);
        moveRequest.setSymbol(Symbol.X);

        assertTrue(service.placeMove(game.getRoomCode(), moveRequest).isPresent());
        assertEquals(Symbol.X, game.getBoard().getGrid()[0][0]);

        Game nextRound = service.playAgain(game.getRoomCode());

        assertEquals(2, nextRound.getRound());
        assertNull(nextRound.getBoard().getGrid()[0][0]);
    }

    private JoinGameRequest joinRequest(String playerName) {
        JoinGameRequest request = new JoinGameRequest();
        request.setPlayerName(playerName);
        return request;
    }
}
