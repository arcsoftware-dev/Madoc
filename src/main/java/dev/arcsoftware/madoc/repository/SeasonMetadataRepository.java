package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.SeasonType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class SeasonMetadataRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public SeasonMetadataRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Integer> getCurrentYear() {
        log.info("Fetching current season metadata year from database");
        return jdbcClient
                .sql(SeasonMetadataSql.GET_CURRENT_SEASON_YEAR_METADATA)
                .query(Integer.class)
                .optional();
    }

    public Optional<SeasonType> getCurrentSeasonType() {
        log.info("Fetching current season metadata type from database");
        return jdbcClient
                .sql(SeasonMetadataSql.GET_CURRENT_SEASON_TYPE_METADATA)
                .query(SeasonType.class)
                .optional();
    }

    public static class SeasonMetadataSql {
        public static final String GET_CURRENT_SEASON_TYPE_METADATA = """
            SELECT season_type
                FROM madoc.season_metadata
                WHERE is_current = TRUE
            """;

        public static final String GET_CURRENT_SEASON_YEAR_METADATA = """
            SELECT season_year
                FROM madoc.season_metadata
                WHERE is_current = TRUE
            """;
    }
}
