package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.payload.RuleDto;
import dev.arcsoftware.madoc.repository.sql.RulesSql;
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

    public List<RuleDto> getAllRules() {
        log.info("Fetching all rules from the database");
        return this.jdbcClient
                .sql(RulesSql.GET_ALL_RULES)
                .query(RuleDto.class)
                .list();
    }
}
