package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.request.CreateGameRequest;
import com.svi.tictactoe.dto.request.JoinGameRequest;
import com.svi.tictactoe.dto.response.game.CreateGameResponse;
import com.svi.tictactoe.dto.response.game.JoinGameResponse;
import com.svi.tictactoe.dto.response.game.LeaveGameResponse;
import com.svi.tictactoe.dto.response.game.PlayAgainResponse;
import com.svi.tictactoe.dto.response.room.RoomInfoResponse;
import com.svi.tictactoe.dto.response.room.RoomsResponse;

public interface RoomService {

    CreateGameResponse createRoom(CreateGameRequest requestBody);

    JoinGameResponse joinRoom(String roomCode, JoinGameRequest requestBody);

    LeaveGameResponse leaveRoom(String roomCode, String playerName);

    RoomsResponse getRooms();

    RoomInfoResponse getRoom(String roomCode);

    PlayAgainResponse playAgain(String roomCode);

    RoomInfoResponse deleteRoom(String roomCode);
}
