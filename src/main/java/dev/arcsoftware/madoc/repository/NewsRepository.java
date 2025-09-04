package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
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

    public List<NewsArticleEntity> getAllNews() {
        log.info("Fetching all news articles from the database");
        return jdbcClient.sql(NewsSql.GET_ALL_NEWS)
            .query(NewsArticleEntity.class)
            .list();
    }

    public static class NewsSql {
        public static final String GET_ALL_NEWS = """
        SELECT id, title, summary, content, author, created_at
            FROM madoc.news
            ORDER BY created_at DESC
        """;
    }
}
