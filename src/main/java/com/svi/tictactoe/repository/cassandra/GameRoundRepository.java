package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.GameRoundEntity;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameRoundRepository extends CassandraRepository<GameRoundEntity, MapId> {

    @Query("SELECT * FROM games_by_room WHERE room_code = ?0")
    List<GameRoundEntity> findAllByRoomCode(String roomCode);

    @Query("SELECT * FROM games_by_room WHERE room_code = ?0 AND round_no = ?1")
    Optional<GameRoundEntity> findByRoomCodeAndRoundNo(String roomCode, Integer roundNo);

    void deleteAllByRoomCode(String roomCode);
}
