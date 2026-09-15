package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.RoomByCodeEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface RoomByCodeRepository extends CassandraRepository<RoomByCodeEntity, String> {
}
