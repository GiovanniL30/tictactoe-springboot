package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.RoomPlayerEntity;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomPlayerRepository extends CassandraRepository<RoomPlayerEntity, MapId> {

    @Query("SELECT * FROM room_players WHERE room_code = ?0")
    List<RoomPlayerEntity> findAllByRoomCode(String roomCode);

    void deleteAllByRoomCode(String roomCode);
}
