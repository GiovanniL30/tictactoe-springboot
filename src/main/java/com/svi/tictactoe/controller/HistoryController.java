package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.response.history.GameMoveHistoryResponse;
import com.svi.tictactoe.dto.response.history.RoomHistoriesResponse;
import com.svi.tictactoe.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/rooms")
    public ResponseEntity<RoomHistoriesResponse> getAllRooms() {
        return ResponseEntity.ok(historyService.getAllRoomHistories());
    }

    @GetMapping("/games/{gameId}/moves")
    public ResponseEntity<GameMoveHistoryResponse> getGameMoves(@PathVariable UUID gameId) {
        return ResponseEntity.ok(historyService.getGameMoves(gameId));
    }
}
