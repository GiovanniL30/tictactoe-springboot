package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.PlayerCatalogEntity;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayerCatalogRepository extends CassandraRepository<PlayerCatalogEntity, MapId> {

    @Query("SELECT * FROM player_catalog WHERE catalog_key = ?0")
    List<PlayerCatalogEntity> findAllByCatalogKey(String catalogKey);

    @Query("SELECT * FROM player_catalog WHERE catalog_key = ?0 AND normalized_player_name = ?1")
    Optional<PlayerCatalogEntity> findByCatalogKeyAndNormalizedPlayerName(String catalogKey, String normalizedPlayerName);
}
