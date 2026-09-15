package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("player_catalog")
public class PlayerCatalogEntity {

    public static final String ALL_PLAYERS = "ALL";

    @PrimaryKeyColumn(name = "catalog_key", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String catalogKey;

    @PrimaryKeyColumn(name = "normalized_player_name", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String normalizedPlayerName;

    @Column("player_name")
    private String playerName;

    public PlayerCatalogEntity() {
    }

    public PlayerCatalogEntity(String catalogKey, String normalizedPlayerName, String playerName) {
        this.catalogKey = catalogKey;
        this.normalizedPlayerName = normalizedPlayerName;
        this.playerName = playerName;
    }

    public String getCatalogKey() {
        return catalogKey;
    }

    public void setCatalogKey(String catalogKey) {
        this.catalogKey = catalogKey;
    }

    public String getNormalizedPlayerName() {
        return normalizedPlayerName;
    }

    public void setNormalizedPlayerName(String normalizedPlayerName) {
        this.normalizedPlayerName = normalizedPlayerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
