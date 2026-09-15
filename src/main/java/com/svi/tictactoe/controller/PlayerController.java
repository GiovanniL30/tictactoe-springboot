package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.response.player.PlayerGamesResponse;
import com.svi.tictactoe.dto.response.player.PlayersResponse;
import com.svi.tictactoe.service.PlayerHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private final PlayerHistoryService playerHistoryService;

    public PlayerController(PlayerHistoryService playerHistoryService) {
        this.playerHistoryService = playerHistoryService;
    }

    @GetMapping
    public ResponseEntity<PlayersResponse> getAllPlayers() {
        return ResponseEntity.ok(playerHistoryService.getAllPlayers());
    }

    @GetMapping("/{playerName}/games")
    public ResponseEntity<PlayerGamesResponse> getPlayerGames(@PathVariable String playerName) {
        return ResponseEntity.ok(playerHistoryService.getPlayerGames(playerName));
    }
}
