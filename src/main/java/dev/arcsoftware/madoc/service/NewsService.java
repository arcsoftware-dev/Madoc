package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import dev.arcsoftware.madoc.repository.NewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static dev.arcsoftware.madoc.config.CacheConfig.CACHE_MANAGER;
import static dev.arcsoftware.madoc.config.CacheConfig.NEWS_CACHE;

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
        log.info("cache-miss for news: calling repository");
        return newsRepository.getAllNews();
    }

}
