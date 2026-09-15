package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.response.player.PlayerGamesResponse;
import com.svi.tictactoe.dto.response.player.PlayersResponse;

public interface PlayerHistoryService {

    PlayersResponse getAllPlayers();

    PlayerGamesResponse getPlayerGames(String playerName);
}
