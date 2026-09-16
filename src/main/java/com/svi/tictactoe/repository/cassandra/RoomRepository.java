package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.RoomEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface RoomRepository extends CassandraRepository<RoomEntity, String> {
}
