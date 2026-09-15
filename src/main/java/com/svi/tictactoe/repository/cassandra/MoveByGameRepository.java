package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.MoveByGameEntity;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MoveByGameRepository extends CassandraRepository<MoveByGameEntity, MapId> {

    @Query("SELECT * FROM moves_by_game WHERE game_id = ?0")
    List<MoveByGameEntity> findAllByGameId(UUID gameId);

    void deleteAllByGameId(UUID gameId);
}
