package com.svi.tictactoe.repository;

import com.svi.tictactoe.model.Game;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository {

    void save(Game game);

    Game delete(String roomCode);

    Optional<Game> findByRoomCode(String roomCode);

    Optional<Game> findByActiveGameId(UUID gameId);

}
