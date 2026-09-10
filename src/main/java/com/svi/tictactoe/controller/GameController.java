package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.AddMoveResponse;
import com.svi.tictactoe.dto.response.CreateGameResponse;
import com.svi.tictactoe.constants.Symbol;
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
    public ResponseEntity<CreateGameResponse> createGame() {
        String roomCode = gameService.createGame();
        CreateGameResponse response = new CreateGameResponse("Game created successfully.", roomCode);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{roomCode}/move")
    public ResponseEntity<AddMoveResponse> addMove(@PathVariable String roomCode, @Valid @RequestBody AddMoveRequest requestBody) {
        Optional<Symbol[][]> updatedGrid = gameService.placeMove(roomCode, requestBody);

        return updatedGrid
                .map(symbols -> ResponseEntity.ok(new AddMoveResponse("Move placed successfully.", symbols)))
                .orElseGet(() -> ResponseEntity.badRequest().body(new AddMoveResponse("Failed to place move.", null)));
    }

    @PostMapping("/{roomCode}/restart")
    public ResponseEntity<String> restartGame(@PathVariable String roomCode) {
        return null;
    }

    @PostMapping("/{roomCode}join")
    public ResponseEntity<String> joinGame(@PathVariable String roomCode, @Valid @RequestBody JoinGameRequest requestBody) {
        return null;
    }

    @GetMapping("/{roomCode}/status")
    public ResponseEntity<String> checkGameStatus(@PathVariable String roomCode) {
        return null;
    }

    @GetMapping("/{roomCode}/status/board")
    public ResponseEntity<String> checkBoardStatus(@PathVariable String roomCode) {
        return null;
    }


    @DeleteMapping("/{roomCode}")
    public ResponseEntity<String> deleteRoom(@PathVariable String roomCode) {
        return null;
    }

}
