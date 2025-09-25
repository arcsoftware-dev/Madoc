package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import dev.arcsoftware.madoc.repository.NewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public List<NewsArticleEntity> getNewsArticles() {
        return newsRepository.getAllNews();
    }

    public List<NewsArticleEntity> getLatestNewsArticles(int limit) {
        return getNewsArticles()
                .stream()
                .sorted(Comparator.comparing(NewsArticleEntity::getCreatedAt).reversed())
                .limit(limit)
                .toList();
    }

}
