package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.AddMoveRequest;
import com.svi.tictactoe.dto.response.game.BoardResponse;
import com.svi.tictactoe.dto.response.game.GameInfoResponse;
import com.svi.tictactoe.dto.response.game.GameMovesResponse;
import com.svi.tictactoe.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameInfoResponse> getGame(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameService.getGame(gameId));
    }

    @GetMapping("/{gameId}/board")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameService.getBoard(gameId));
    }

    @GetMapping("/{gameId}/moves")
    public ResponseEntity<GameMovesResponse> getMoves(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameService.getMoves(gameId));
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<BoardResponse> addMove(@PathVariable UUID gameId, @Valid @RequestBody AddMoveRequest requestBody) {
        return ResponseEntity.ok(gameService.placeMove(gameId, requestBody));
    }

}
