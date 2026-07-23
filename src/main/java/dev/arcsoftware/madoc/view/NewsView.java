package dev.arcsoftware.madoc.view;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.arcsoftware.madoc.controller.NewsController;
import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
@RequestMapping("/news")
public class NewsView {

    private final NewsController newsController;

    @Autowired
    public NewsView(NewsController newsController){
        this.newsController = newsController;
    }

    @ModelAttribute("allNews")
    public List<NewsArticleEntity> newsArticles(){
        return newsController.getAllNews().getBody();
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @GetMapping("")
    public String news(Model model) {
        model.addAttribute("newsArticle", new NewsArticleEntity());
        return "news";
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @PostMapping("/create")
    public String createNewsArticle(Model model,
        @ModelAttribute NewsArticleEntity newsArticleEntity
    ) {
        // Get the current authenticated user
        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            log.info("found: {}", authentication.getPrincipal());
            username = ((String) authentication.getPrincipal());
            newsArticleEntity.setAuthor(username);
            newsArticleEntity.setCreatedAt(LocalDateTime.now());
            newsArticleEntity.setContent(newsArticleEntity.getSummary());
        }

        if(username == null){
            log.error("Username not available to set on new article");
            throw new IllegalArgumentException("Username not available for news article");
        }

        newsController.createNews(newsArticleEntity);

        model.addAttribute("newsArticle", new NewsArticleEntity());
        return "news";
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @PostMapping("/delete")
    public String deleteNewsArticle(Model model,
        @ModelAttribute NewsArticleEntity newsArticleEntity
    ) {
        newsController.deleteNews(newsArticleEntity.getId());

        model.addAttribute("newsArticle", new NewsArticleEntity());
        return "news";
    }
}
