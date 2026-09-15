package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.GameByIdEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface GameByIdRepository extends CassandraRepository<GameByIdEntity, UUID> {
}
