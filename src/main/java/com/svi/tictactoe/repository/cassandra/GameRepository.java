package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.GameEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface GameRepository extends CassandraRepository<GameEntity, UUID> {
}
