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

    public int findPlayerIdByJerseyNumberAndTeam(Integer jerseyNumber, int teamId) {
        return jdbcClient
                .sql(PlayersSql.GET_PLAYER_ID_BY_JERSEY_AND_TEAM)
                .param("team_id", teamId)
                .param("jersey_number", jerseyNumber)
                .query(Integer.class)
                .single();
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

        public static final String GET_PLAYER_ID_BY_JERSEY_AND_TEAM = """
        SELECT player_id from madoc.roster_assignments
        WHERE team_id = :team_id
        AND jersey_number = :jersey_number
        """;
    }
}
