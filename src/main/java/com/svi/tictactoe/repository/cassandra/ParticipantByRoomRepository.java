package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.ParticipantByRoomEntity;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;

public interface ParticipantByRoomRepository extends CassandraRepository<ParticipantByRoomEntity, MapId> {

    @Query("SELECT * FROM participants_by_room WHERE room_code = ?0")
    List<ParticipantByRoomEntity> findAllByRoomCode(String roomCode);

    void deleteAllByRoomCode(String roomCode);
}
