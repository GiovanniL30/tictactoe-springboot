package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.GameByRoomEntity;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameByRoomRepository extends CassandraRepository<GameByRoomEntity, MapId> {

    @Query("SELECT * FROM games_by_room WHERE room_code = ?0")
    List<GameByRoomEntity> findAllByRoomCode(String roomCode);

    @Query("SELECT * FROM games_by_room WHERE room_code = ?0 AND round_no = ?1")
    Optional<GameByRoomEntity> findByRoomCodeAndRoundNo(String roomCode, Integer roundNo);

    void deleteAllByRoomCode(String roomCode);
}
