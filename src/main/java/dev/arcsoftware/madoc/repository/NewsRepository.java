package dev.arcsoftware.madoc.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import lombok.extern.slf4j.Slf4j;

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

    public NewsArticleEntity getNewsById(Long id) {
        log.info("Fetching news article with id: {}", id);
        return jdbcClient.sql(NewsSql.GET_NEWS_BY_ID)
            .param(id)
            .query(NewsArticleEntity.class)
            .optional()
            .orElse(null);
    }

    public NewsArticleEntity createNews(NewsArticleEntity newsArticle) {
        log.info("Creating new news article: {}", newsArticle.getTitle());
        jdbcClient.sql(NewsSql.CREATE_NEWS)
            .param(newsArticle.getTitle())
            .param(newsArticle.getSummary())
            .param(newsArticle.getContent())
            .param(newsArticle.getAuthor())
            .update();
        
        // Return the created article with ID
        return getAllNews().stream()
                .filter(article -> article.getTitle().equals(newsArticle.getTitle()))
                .findFirst()
                .orElse(null);
    }

    public NewsArticleEntity updateNews(NewsArticleEntity newsArticle) {
        log.info("Updating news article: {}", newsArticle.getTitle());
        jdbcClient.sql(NewsSql.UPDATE_NEWS)
            .param(newsArticle.getTitle())
            .param(newsArticle.getSummary())
            .param(newsArticle.getContent())
            .param(newsArticle.getAuthor())
            .param(newsArticle.getId())
            .update();
        
        return newsArticle;
    }

    public void deleteNews(Long id) {
        log.info("Deleting news article with id: {}", id);
        jdbcClient.sql(NewsSql.DELETE_NEWS)
            .param(id)
            .update();
    }

    public static class NewsSql {
        public static final String GET_ALL_NEWS = """
        SELECT id, title, summary, content, author, created_at
            FROM madoc.news
            ORDER BY created_at DESC
        """;

        public static final String GET_NEWS_BY_ID = """
        SELECT id, title, summary, content, author, created_at
            FROM madoc.news
            WHERE id = ?
        """;

        public static final String CREATE_NEWS = """
        INSERT INTO madoc.news (title, summary, content, author)
            VALUES (?, ?, ?, ?)
        """;

        public static final String UPDATE_NEWS = """
        UPDATE madoc.news
            SET title = ?, summary = ?, content = ?, author = ?
            WHERE id = ?
        """;

        public static final String DELETE_NEWS = """
        DELETE FROM madoc.news
            WHERE id = ?
        """;
    }
}
