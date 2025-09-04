package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.entity.RuleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class RuleRepository {
    private final JdbcClient jdbcClient;

    @Autowired
    public RuleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<RuleEntity> getAllRules() {
        log.info("Fetching all rules from the database");
        return this.jdbcClient
                .sql(RulesSql.GET_ALL_RULES)
                .query(RuleEntity.class)
                .list();
    }

    public static class RulesSql {
        public static final String GET_ALL_RULES = """
        SELECT id, title, description, created_at, updated_at
            FROM madoc.rules
            ORDER BY id ASC
        """;
    }
}
