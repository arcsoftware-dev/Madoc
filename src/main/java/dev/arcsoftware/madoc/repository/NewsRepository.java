package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.payload.NewsArticleDto;
import dev.arcsoftware.madoc.repository.sql.NewsSql;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class NewsRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public NewsRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<NewsArticleDto> findAll() {
        log.info("Fetching all news articles from the database");
        return jdbcClient.sql(NewsSql.GET_ALL_NEWS)
            .query(NewsArticleDto.class)
            .list();
    }
}
