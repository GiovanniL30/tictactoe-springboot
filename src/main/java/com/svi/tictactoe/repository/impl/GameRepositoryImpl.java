package com.svi.tictactoe.repository.impl;

import com.svi.tictactoe.model.Game;
import com.svi.tictactoe.repository.GameRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameRepositoryImpl implements GameRepository {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    @Override
    public void save(Game game) {
        games.put(game.getRoomCode(), game);
    }

    @Override
    public Game delete(String roomCode) {
        return games.remove(roomCode);
    }

    @Override
    public Optional<Game> findByRoomCode(String roomCode) {
        return Optional.ofNullable(games.get(roomCode));
    }
}
