package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.response.PlayerGamesResponse;
import com.svi.tictactoe.dto.response.PlayersResponse;

public interface PlayerHistoryService {

    PlayersResponse getAllPlayers();

    PlayerGamesResponse getPlayerGames(String playerName);
}
