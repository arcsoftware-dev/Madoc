package dev.arcsoftware.madoc.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import dev.arcsoftware.madoc.service.NewsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/news")
public class NewsController {
    @Autowired
    private NewsService newsService;

    @GetMapping("/latest")
    public ResponseEntity<List<NewsArticleEntity>> getLatestNews(
            @RequestParam(value = "limit", defaultValue = "5") Integer limit
    ) {
        log.info("Fetching latest {} news articles", limit);
        List<NewsArticleEntity> newsArticleEntities = newsService.getLatestNewsArticles(limit);
        return ResponseEntity.ok(newsArticleEntities);
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @GetMapping
    public ResponseEntity<List<NewsArticleEntity>> getAllNews() {
        log.info("Fetching all news articles");
        List<NewsArticleEntity> newsArticleEntities = newsService.getNewsArticles();
        return ResponseEntity.ok(newsArticleEntities);
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @GetMapping("/{newsId}")
    public ResponseEntity<NewsArticleEntity> getNewsById(@PathVariable(name = "newsId") Long newsId) {
        log.info("Fetching news article with id: {}", newsId);
        NewsArticleEntity newsArticle = newsService.getNewsById(newsId);
        if (newsArticle != null) {
            return ResponseEntity.ok(newsArticle);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @PostMapping
    public ResponseEntity<NewsArticleEntity> createNews(@RequestBody NewsArticleEntity newsArticle) {
        log.info("Creating new news article: {}", newsArticle.getTitle());
        NewsArticleEntity createdArticle = newsService.createNews(newsArticle);
        return ResponseEntity.ok(createdArticle);
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @PutMapping("/{id}")
    public ResponseEntity<NewsArticleEntity> updateNews(@PathVariable Long id, @RequestBody NewsArticleEntity newsArticle) {
        log.info("Updating news article with id: {}", id);
        // Set the ID to ensure we're updating the correct article
        newsArticle.setId(id);
        NewsArticleEntity updatedArticle = newsService.updateNews(newsArticle);
        return ResponseEntity.ok(updatedArticle);
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        log.info("Deleting news article with id: {}", id);
        newsService.deleteNews(id);
        return ResponseEntity.ok().build();
    }
}
