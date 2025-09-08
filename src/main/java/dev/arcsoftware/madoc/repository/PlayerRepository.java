package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.entity.PlayerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PlayerRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public PlayerRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<PlayerEntity> getPlayers() {
        return jdbcClient
                .sql(PlayersSql.GET_ALL_PLAYERS)
                .query(PlayerEntity.class)
                .list();
    }

    public static class PlayersSql {
        public static final String GET_ALL_PLAYERS = """
        SELECT id, first_name, last_name, email, phone_number, created_at
            FROM madoc.players
            ORDER BY id ASC
        """;
    }
}
