package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("room_catalog")
public class RoomCatalogEntity {

    public static final String ALL_ROOMS = "ALL";

    @PrimaryKeyColumn(name = "catalog_key", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String catalogKey;

    @PrimaryKeyColumn(name = "room_code", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String roomCode;

    public RoomCatalogEntity() {
    }

    public RoomCatalogEntity(String catalogKey, String roomCode) {
        this.catalogKey = catalogKey;
        this.roomCode = roomCode;
    }

    public String getCatalogKey() {
        return catalogKey;
    }

    public void setCatalogKey(String catalogKey) {
        this.catalogKey = catalogKey;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }
}
