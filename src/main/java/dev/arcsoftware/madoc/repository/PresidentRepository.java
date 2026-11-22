package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.entity.PresidentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class PresidentRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public PresidentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<PresidentEntity> getAllPresidents() {
        log.info("Fetching all league presidents from the database");
        return this.jdbcClient
                .sql(PresidentSql.GET_ALL_PRESIDENTS)
                .query(PresidentEntity.class)
                .list();
    }

    public static class PresidentSql {
        public static final String GET_ALL_PRESIDENTS = """
                SELECT number, name, start_year, end_year, photo_id
                FROM madoc.presidents
                ORDER BY number DESC
                """;
    }
}
