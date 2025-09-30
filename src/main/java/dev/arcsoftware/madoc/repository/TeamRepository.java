package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.entity.TeamEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    public void insertTeam(TeamEntity teamEntity) {
        int id = jdbcClient
                .sql(TeamsSql.INSERT_TEAM)
                .params(teamEntity.toParameterMap())
                .query(Integer.class)
                .single();
        teamEntity.setId(id);
    }

    public int findTeamIdByNameAndYear(String teamName, int year) {
        return jdbcClient
                .sql(TeamsSql.GET_TEAM_ID_BY_NAME_YEAR)
                .param("year", year)
                .param("team_name", teamName)
                .query(Integer.class)
                .single();
    }

    public Optional<String> findTeamNameById(Integer teamId) {
        return jdbcClient
                .sql(TeamsSql.GET_TEAM_NAME_BY_ID)
                .param("id", teamId)
                .query(String.class)
                .optional();
    }

    public boolean teamExistsById(Integer teamId) {
        String sql = "SELECT COUNT(*) FROM madoc.teams WHERE id = :team_id";
        Integer count = jdbcClient
                .sql(sql)
                .param("team_id", teamId)
                .query(Integer.class)
                .single();
        return count > 0;
    }

    public static class TeamsSql {
        public static final String INSERT_TEAM = """
        INSERT INTO madoc.teams (team_name, year)
        VALUES (:team_name, :year)
        RETURNING id;
        """;

        public static final String GET_TEAMS_BY_YEAR = """
        SELECT id, team_name, year
            FROM madoc.teams
            WHERE year = :year
            ORDER BY id ASC
        """;

        public static final String GET_TEAM_ID_BY_NAME_YEAR = """
        SELECT id
            FROM madoc.teams
            WHERE year = :year
            AND team_name = :team_name
        """;
        public static final String GET_TEAM_NAME_BY_ID = """
        SELECT team_name FROM madoc.teams WHERE id = :id
        """;
    }
}
