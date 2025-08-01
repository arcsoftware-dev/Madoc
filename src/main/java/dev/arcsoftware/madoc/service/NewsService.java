package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.model.payload.NewsArticleDto;
import dev.arcsoftware.madoc.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public List<NewsArticleDto> getNewsArticles() {
        return newsRepository.findAll();
    }

}
