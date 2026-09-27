package dev.arcsoftware.madoc.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class HomeViewCountRepository {
    private final JdbcClient jdbcClient;


    public HomeViewCountRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public long getNextViewCount(){
        return this.jdbcClient
                .sql("SELECT nextval('madoc.home_view_count_sequence')")
                .query(Long.class)
                .single();
    }

    public long getCurrentViewCount(){
        return this.jdbcClient
                .sql("SELECT last_value FROM madoc.home_view_count_sequence")
                .query(Long.class)
                .single();
    }
}
