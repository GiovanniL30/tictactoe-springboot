package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.BoardResponse;
import com.svi.tictactoe.dto.response.CreateGameResponse;
import com.svi.tictactoe.dto.response.GameStatusResponse;
import com.svi.tictactoe.dto.response.JoinGameResponse;
import com.svi.tictactoe.dto.response.PlayAgainResponse;
import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.model.Player;
import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<CreateGameResponse> createGame(@Valid @RequestBody CreateGameRequest requestBody) {
        Game game = gameService.createGame(requestBody);
        CreateGameResponse response = new CreateGameResponse(
                "Game created successfully.",
                game.getRoomCode(),
                game.getPlayers().getFirst()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{roomCode}/move")
    public ResponseEntity<BoardResponse> addMove(@PathVariable String roomCode, @Valid @RequestBody AddMoveRequest requestBody) {
        Optional<Game> updatedGame = gameService.placeMove(roomCode, requestBody);

        return updatedGame
                .map(game -> ResponseEntity.ok(new BoardResponse(
                        "Move placed successfully.",
                        game.getBoard().getGrid(),
                        game.getCurrentTurn()
                )))
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        new BoardResponse("Failed to place move.", null, null)
                ));
    }

    @PostMapping("/{roomCode}/play-again")
    public ResponseEntity<PlayAgainResponse> playAgain(@PathVariable String roomCode) {
        Game game = gameService.playAgain(roomCode);

        return ResponseEntity.ok(new PlayAgainResponse(
                "New round started.",
                game.getRound(),
                game.getCurrentTurn(),
                game.getPlayers()
        ));
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<JoinGameResponse> joinGame(@PathVariable String roomCode, @Valid @RequestBody JoinGameRequest requestBody) {
        Player participant = gameService.joinGame(roomCode, requestBody);
        String message = participant.getType() == PlayerType.PLAYER
                ? "Player joined successfully."
                : "Game already has two players. Joined as spectator.";

        return ResponseEntity.ok(new JoinGameResponse(message, participant));
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<GameStatusResponse> checkGameStatus(@PathVariable String roomCode) {
        Game game = gameService.getGame(roomCode);

        return ResponseEntity.ok(new GameStatusResponse(
                game.getBoard().getGrid(),
                game.getPlayers(),
                game.getRoomCode(),
                game.getRound(),
                game.getCurrentTurn(),
                game.getSpectators().size(),
                "Game status retrieved successfully."
        ));
    }

    @GetMapping("/{roomCode}/board")
    public ResponseEntity<BoardResponse> checkBoardStatus(@PathVariable String roomCode) {
        Game game = gameService.getGame(roomCode);

        return ResponseEntity.ok(new BoardResponse(
                "Latest Board Grid",
                game.getBoard().getGrid(),
                game.getCurrentTurn()
        ));
    }


    @DeleteMapping("/{roomCode}")
    public ResponseEntity<GameStatusResponse> deleteRoom(@PathVariable String roomCode) {
        Game game = gameService.deleteGame(roomCode);

        return ResponseEntity.ok(new GameStatusResponse(
                game.getBoard().getGrid(),
                game.getPlayers(),
                game.getRoomCode(),
                game.getRound(),
                game.getCurrentTurn(),
                game.getSpectators().size(),
                "Game deleted successfully."
        ));
    }

}
