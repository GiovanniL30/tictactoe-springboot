package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.CreateRoomRequest;
import com.svi.tictactoe.dto.request.JoinRoomRequest;
import com.svi.tictactoe.dto.response.room.CreateRoomResponse;
import com.svi.tictactoe.dto.response.room.JoinRoomResponse;
import com.svi.tictactoe.dto.response.room.LeaveRoomResponse;
import com.svi.tictactoe.dto.response.game.PlayAgainResponse;
import com.svi.tictactoe.dto.response.room.RoomInfoResponse;
import com.svi.tictactoe.dto.response.room.RoomsResponse;
import com.svi.tictactoe.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<CreateRoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest requestBody) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(requestBody));
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<JoinRoomResponse> joinRoom(@PathVariable String roomCode, @Valid @RequestBody JoinRoomRequest requestBody) {
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

    @PostMapping("/{roomCode}/players/{playerName}/leave")
    public ResponseEntity<LeaveRoomResponse> leaveRoom(@PathVariable String roomCode, @PathVariable String playerName) {
        return ResponseEntity.ok(roomService.leaveRoom(roomCode, playerName));
    }

    @DeleteMapping("/{roomCode}")
    public ResponseEntity<RoomInfoResponse> deleteRoom(@PathVariable String roomCode) {
        return ResponseEntity.ok(roomService.deleteRoom(roomCode));
    }
}
