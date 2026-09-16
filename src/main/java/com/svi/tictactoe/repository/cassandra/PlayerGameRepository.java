package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.PlayerGameEntity;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PlayerGameRepository extends CassandraRepository<PlayerGameEntity, MapId> {

    @Query("SELECT * FROM games_by_player WHERE normalized_player_name = ?0")
    List<PlayerGameEntity> findAllByNormalizedPlayerName(String normalizedPlayerName);

    @Query("DELETE FROM games_by_player WHERE normalized_player_name = ?0 AND game_id = ?1")
    void deleteByNormalizedPlayerNameAndGameId(String normalizedPlayerName, UUID gameId);
}
