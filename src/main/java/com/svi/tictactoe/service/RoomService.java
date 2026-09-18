package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.request.CreateRoomRequest;
import com.svi.tictactoe.dto.request.JoinRoomRequest;
import com.svi.tictactoe.dto.response.room.CreateRoomResponse;
import com.svi.tictactoe.dto.response.room.JoinRoomResponse;
import com.svi.tictactoe.dto.response.room.LeaveRoomResponse;
import com.svi.tictactoe.dto.response.game.PlayAgainResponse;
import com.svi.tictactoe.dto.response.room.RoomInfoResponse;
import com.svi.tictactoe.dto.response.room.RoomsResponse;

public interface RoomService {

    CreateRoomResponse createRoom(CreateRoomRequest requestBody);

    JoinRoomResponse joinRoom(String roomCode, JoinRoomRequest requestBody);

    LeaveRoomResponse leaveRoom(String roomCode, String playerName);

    RoomsResponse getRooms();

    RoomInfoResponse getRoom(String roomCode);

    PlayAgainResponse playAgain(String roomCode);

    RoomInfoResponse deleteRoom(String roomCode);
}
