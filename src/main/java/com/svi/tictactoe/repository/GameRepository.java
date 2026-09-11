package com.svi.tictactoe.repository;

import com.svi.tictactoe.model.Game;

import java.util.Optional;

public interface GameRepository {

    void save(Game game);

    Game delete(String roomCode);

    Optional<Game> findByRoomCode(String roomCode);

}
