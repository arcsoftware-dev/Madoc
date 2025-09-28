package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import dev.arcsoftware.madoc.repository.NewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static dev.arcsoftware.madoc.config.CacheConfig.*;

@Slf4j
@Service
public class NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Cacheable(cacheManager = CACHE_MANAGER, value = NEWS_CACHE)
    public List<NewsArticleEntity> getNewsArticles() {
        log.info("cache-miss for getNewsArticles: calling repository");
        return newsRepository.getAllNews();
    }

    @Cacheable(cacheManager = CACHE_MANAGER, value = LATEST_NEWS_CACHE)
    public List<NewsArticleEntity> getLatestNewsArticles(int limit) {
        log.info("cache-miss for getLatestNewsArticles with limit {}: calling repository", limit);
        return getNewsArticles()
                .stream()
                .sorted(Comparator.comparing(NewsArticleEntity::getCreatedAt).reversed())
                .limit(limit)
                .toList();
    }

}
