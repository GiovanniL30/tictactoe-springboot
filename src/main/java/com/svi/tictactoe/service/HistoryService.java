package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.response.history.GameMoveHistoryResponse;
import com.svi.tictactoe.dto.response.history.RoomHistoriesResponse;

import java.util.UUID;

public interface HistoryService {

    RoomHistoriesResponse getAllRoomHistories();

    GameMoveHistoryResponse getGameMoves(UUID gameId);
}
