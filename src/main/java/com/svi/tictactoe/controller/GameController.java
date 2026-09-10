package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.CreateGameResponse;
import com.svi.tictactoe.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<Map<String, String>> addMove(@PathVariable String roomCode, @Valid @RequestBody AddMoveRequest requestBody) {
        boolean placed = gameService.placeMove(roomCode, requestBody);

        if (!placed) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to place move."));
        }

        return ResponseEntity.ok(Map.of("message", "Move placed successfully."));
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
