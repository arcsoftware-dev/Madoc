package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import dev.arcsoftware.madoc.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
}
