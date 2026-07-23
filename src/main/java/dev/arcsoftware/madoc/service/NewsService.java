package dev.arcsoftware.madoc.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static dev.arcsoftware.madoc.config.CacheConfig.CACHE_MANAGER;
import static dev.arcsoftware.madoc.config.CacheConfig.LATEST_NEWS_CACHE;
import static dev.arcsoftware.madoc.config.CacheConfig.NEWS_CACHE;
import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import dev.arcsoftware.madoc.repository.NewsRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    //@Cacheable(cacheManager = CACHE_MANAGER, value = NEWS_CACHE)
    public List<NewsArticleEntity> getNewsArticles() {
        log.info("cache-miss for getNewsArticles: calling repository");
        return newsRepository.getAllNews();
    }

    //@Cacheable(cacheManager = CACHE_MANAGER, value = LATEST_NEWS_CACHE)
    public List<NewsArticleEntity> getLatestNewsArticles(int limit) {
        log.info("cache-miss for getLatestNewsArticles with limit {}: calling repository", limit);
        return getNewsArticles()
                .stream()
                .sorted(Comparator.comparing(NewsArticleEntity::getCreatedAt).reversed())
                .limit(limit)
                .toList();
    }

    public NewsArticleEntity getNewsById(long id) {
        return this.newsRepository.getNewsById(id);
    }

    public NewsArticleEntity createNews(NewsArticleEntity newsArticle) {
        log.info("Creating new news article: {}", newsArticle.getTitle());
        NewsArticleEntity createdArticle = newsRepository.createNews(newsArticle);
        
        // Invalidate cache when creating a new article
//        evictAllCaches();
        return createdArticle;
    }

    public NewsArticleEntity updateNews(NewsArticleEntity newsArticle) {
        log.info("Updating news article: {}", newsArticle.getTitle());
        NewsArticleEntity updatedArticle = newsRepository.updateNews(newsArticle);
        
        // Invalidate cache when updating an article
//        evictAllCaches();
        return updatedArticle;
    }

    public void deleteNews(Long id) {
        log.info("Deleting news article with id: {}", id);
        newsRepository.deleteNews(id);
        
        // Invalidate cache when deleting an article
//        evictAllCaches();
    }
}
