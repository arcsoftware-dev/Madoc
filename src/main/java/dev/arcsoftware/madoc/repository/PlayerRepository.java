package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.entity.PlayerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    public Optional<PlayerEntity> findById(int playerId) {
        return jdbcClient
                .sql(PlayersSql.GET_PLAYER_BY_ID)
                .param("player_id", playerId)
                .query(PlayerEntity.class)
                .optional();
    }

    public Optional<PlayerEntity> findByFullNameIgnoreCase(String fullName) {
        return jdbcClient
                .sql(PlayersSql.GET_PLAYER_BY_FULL_NAME)
                .param("full_name", fullName)
                .query(PlayerEntity.class)
                .list()
                .stream()
                .findFirst();
    }

    public boolean playerExistsById(Integer playerId) {
        String sql = "SELECT COUNT(*) FROM madoc.players WHERE id = :player_id";
        Integer count = jdbcClient
                .sql(sql)
                .param("player_id", playerId)
                .query(Integer.class)
                .single();
        return count > 0;
    }

    public static class PlayersSql {
        public static final String GET_ALL_PLAYERS = """
        SELECT id, first_name, last_name, email, phone_number, created_at
            FROM madoc.players
            ORDER BY id ASC
        """;

        public static final String GET_PLAYER_BY_ID = """
        SELECT id, first_name, last_name, email, phone_number, created_at
            FROM madoc.players
            WHERE id = :player_id
        """;

        public static final String GET_PLAYER_BY_FULL_NAME = """
        SELECT id, first_name, last_name, email, phone_number, created_at
            FROM madoc.players
            WHERE LOWER(TRIM(CONCAT(first_name, ' ', last_name))) = LOWER(TRIM(:full_name))
            ORDER BY id ASC
        """;
    }
}
