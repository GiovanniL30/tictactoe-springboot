package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.BoardResponse;
import com.svi.tictactoe.dto.response.CreateGameResponse;
import com.svi.tictactoe.dto.response.GameStatusResponse;
import com.svi.tictactoe.dto.response.JoinGameResponse;
import com.svi.tictactoe.dto.response.PlayAgainResponse;
import com.svi.tictactoe.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<CreateGameResponse> createGame(@Valid @RequestBody CreateGameRequest requestBody) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createGame(requestBody));
    }

    @PostMapping("/{roomCode}/move")
    public ResponseEntity<BoardResponse> addMove(@PathVariable String roomCode, @Valid @RequestBody AddMoveRequest requestBody) {
        return ResponseEntity.ok(gameService.placeMove(roomCode, requestBody));
    }

    @PostMapping("/{roomCode}/play-again")
    public ResponseEntity<PlayAgainResponse> playAgain(@PathVariable String roomCode) {
        return ResponseEntity.ok(gameService.playAgain(roomCode));
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<JoinGameResponse> joinGame(@PathVariable String roomCode, @Valid @RequestBody JoinGameRequest requestBody) {
        return ResponseEntity.ok(gameService.joinGame(roomCode, requestBody));
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<GameStatusResponse> checkGameStatus(@PathVariable String roomCode) {
        return ResponseEntity.ok(gameService.getGameStatus(roomCode));
    }

    @GetMapping("/{roomCode}/board")
    public ResponseEntity<BoardResponse> checkBoardStatus(@PathVariable String roomCode) {
        return ResponseEntity.ok(gameService.getBoardStatus(roomCode));
    }


    @DeleteMapping("/{roomCode}")
    public ResponseEntity<GameStatusResponse> deleteRoom(@PathVariable String roomCode) {
        return ResponseEntity.ok(gameService.deleteGame(roomCode));
    }

}
