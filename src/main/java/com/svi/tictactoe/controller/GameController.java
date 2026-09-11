package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.BoardResponse;
import com.svi.tictactoe.dto.response.CreateGameResponse;
import com.svi.tictactoe.model.Board;
import com.svi.tictactoe.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
    public ResponseEntity<BoardResponse> addMove(@PathVariable String roomCode, @Valid @RequestBody AddMoveRequest requestBody) {
        Optional<Board> updatedBoard = gameService.placeMove(roomCode, requestBody);

        return updatedBoard
                .map(board -> ResponseEntity.ok(new BoardResponse("Move placed successfully.", board.getGrid())))
                .orElseGet(() -> ResponseEntity.badRequest().body(new BoardResponse("Failed to place move.", null)));
    }

    @PostMapping("/{roomCode}/restart")
    public ResponseEntity<Map<String, String>> restartGame(@PathVariable String roomCode) {
        gameService.restartGame(roomCode);
        return ResponseEntity.ok(Map.of("message", "Game have been restarted."));
    }

    @PostMapping("/{roomCode}join")
    public ResponseEntity<String> joinGame(@PathVariable String roomCode, @Valid @RequestBody JoinGameRequest requestBody) {
        return null;
    }

    @GetMapping("/{roomCode}/status")
    public ResponseEntity<String> checkGameStatus(@PathVariable String roomCode) {
        return null;
    }

    @GetMapping("/{roomCode}/board")
    public ResponseEntity<BoardResponse> checkBoardStatus(@PathVariable String roomCode) {
        Board board = gameService.getBoard(roomCode);

        return ResponseEntity.ok(new BoardResponse("Latest Board Grid", board.getGrid()));
    }


    @DeleteMapping("/{roomCode}")
    public ResponseEntity<String> deleteRoom(@PathVariable String roomCode) {
        return null;
    }

}
