package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.response.GameMoveHistoryResponse;
import com.svi.tictactoe.dto.response.RoomHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface HistoryService {

    List<RoomHistoryResponse> getAllRoomHistories();

    RoomHistoryResponse getRoomHistory(String roomCode);

    GameMoveHistoryResponse getGameMoves(UUID gameId);
}
