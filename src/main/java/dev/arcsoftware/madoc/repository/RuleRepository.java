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

    public void updateRule(RuleEntity ruleEntity){
        log.info("Updating Rule in DB: {}", ruleEntity.getTitle());
        this.jdbcClient
                .sql(RulesSql.UPDATE_RULE)
                .params(ruleEntity.toParameterMap())
                .update();
    }

    public void insertNewRules(List<RuleEntity> rules) {
        log.info("Inserting {} Rules in DB", rules.size());
        int total = 0;
        for(RuleEntity ruleEntity : rules){
            total += this.jdbcClient
                    .sql(RulesSql.INSERT_RULE)
                    .params(ruleEntity.toParameterMap())
                    .update();
        }
        log.info("Successfully inserted {} Rules in DB", total);
    }

    public void deleteAll() {
        log.info("Deleting all rules from the database");
        int deleted = this.jdbcClient
                .sql(RulesSql.DELETE_ALL_RULES)
                .update();
        log.info("Deleted {} rules from the database", deleted);
    }

    public static class RulesSql {
        public static final String GET_ALL_RULES = """
        SELECT id, title, description, created_at, updated_at
            FROM madoc.rules
            ORDER BY id ASC
        """;

        public static final String UPDATE_RULE = """
            UPDATE madoc.rules SET title = :title, description = :description, updated_at = ?
            WHERE id = :id
        """;

        public static final String DELETE_ALL_RULES = """
        DELETE FROM madoc.rules WHERE 1=1;
        """;

        public static final String INSERT_RULE = """
        INSERT INTO madoc.rules (title, description)
        VALUES (:title, :description)
        """;
    }
}
