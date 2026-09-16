package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.game.CreateGameResponse;
import com.svi.tictactoe.dto.response.game.JoinGameResponse;
import com.svi.tictactoe.dto.response.game.PlayAgainResponse;
import com.svi.tictactoe.dto.response.room.RoomInfoResponse;
import com.svi.tictactoe.dto.response.room.RoomsResponse;
import com.svi.tictactoe.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<RoomsResponse> getRooms() {
        return ResponseEntity.ok(roomService.getRooms());
    }

    @PostMapping
    public ResponseEntity<CreateGameResponse> createRoom(@Valid @RequestBody CreateGameRequest requestBody) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(requestBody));
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<JoinGameResponse> joinRoom(@PathVariable String roomCode, @Valid @RequestBody JoinGameRequest requestBody) {
        return ResponseEntity.ok(roomService.joinRoom(roomCode, requestBody));
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<RoomInfoResponse> getRoom(@PathVariable String roomCode) {
        return ResponseEntity.ok(roomService.getRoom(roomCode));
    }

    @PostMapping("/{roomCode}/play-again")
    public ResponseEntity<PlayAgainResponse> playAgain(@PathVariable String roomCode) {
        return ResponseEntity.ok(roomService.playAgain(roomCode));
    }

    @DeleteMapping("/{roomCode}")
    public ResponseEntity<RoomInfoResponse> deleteRoom(@PathVariable String roomCode) {
        return ResponseEntity.ok(roomService.deleteRoom(roomCode));
    }
}
