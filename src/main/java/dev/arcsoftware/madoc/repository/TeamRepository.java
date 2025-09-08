package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.entity.TeamEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TeamRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public TeamRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<TeamEntity> getTeamsByYear(int year) {
        return jdbcClient
                .sql(TeamsSql.GET_TEAMS_BY_YEAR)
                .param("year", year)
                .query(TeamEntity.class)
                .list();
    }

    public static class TeamsSql {
        public static final String GET_TEAMS_BY_YEAR = """
        SELECT id, team_name, year
            FROM madoc.teams
            WHERE year = :year
            ORDER BY id ASC
        """;
    }
}
