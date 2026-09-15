package com.svi.tictactoe.repository.cassandra;

import com.svi.tictactoe.entity.RoomCatalogEntity;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;

public interface RoomCatalogRepository extends CassandraRepository<RoomCatalogEntity, MapId> {

    @Query("SELECT * FROM room_catalog WHERE catalog_key = ?0")
    List<RoomCatalogEntity> findAllByCatalogKey(String catalogKey);
}
