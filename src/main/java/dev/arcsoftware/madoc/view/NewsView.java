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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @GetMapping("")
    public String news(Model model) {
        List<NewsArticleEntity> articles = newsController.getAllNews().getBody();
        model.addAttribute("allNews", articles != null ? articles : List.of());
        if (!model.containsAttribute("newsArticle")) {
            model.addAttribute("newsArticle", new NewsArticleEntity());
        }
        return "news-modern";
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @PostMapping("/create")
    public String createNewsArticle(
        @ModelAttribute NewsArticleEntity newsArticleEntity,
        RedirectAttributes redirectAttributes
    ) {
        String title = newsArticleEntity.getTitle() != null ? newsArticleEntity.getTitle().trim() : "";
        String summary = newsArticleEntity.getSummary() != null ? newsArticleEntity.getSummary().trim() : "";

        if (title.isEmpty() || summary.isEmpty()) {
            redirectAttributes.addFlashAttribute("newsError", "Both a title and summary are required.");
            redirectAttributes.addFlashAttribute("newsArticle", newsArticleEntity);
            return "redirect:/news";
        }
        if (title.length() > 100) {
            redirectAttributes.addFlashAttribute("newsError", "Title must be 100 characters or fewer.");
            redirectAttributes.addFlashAttribute("newsArticle", newsArticleEntity);
            return "redirect:/news";
        }

        // Get the current authenticated user
        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            log.info("found: {}", authentication.getName());
            username = authentication.getName();
        }

        if (username == null) {
            log.error("Username not available to set on new article");
            throw new IllegalArgumentException("Username not available for news article");
        }

        newsArticleEntity.setTitle(title);
        newsArticleEntity.setSummary(summary);
        newsArticleEntity.setContent(summary);
        newsArticleEntity.setAuthor(username.length() > 20 ? username.substring(0, 20) : username);
        newsArticleEntity.setCreatedAt(LocalDateTime.now());

        newsController.createNews(newsArticleEntity);
        redirectAttributes.addFlashAttribute("newsSuccess", "News article published.");
        return "redirect:/news";
    }

    @PreAuthorize("hasRole('ROLE_[ADMIN]')")
    @PostMapping("/delete")
    public String deleteNewsArticle(
        @ModelAttribute NewsArticleEntity newsArticleEntity,
        RedirectAttributes redirectAttributes
    ) {
        newsController.deleteNews(newsArticleEntity.getId());
        redirectAttributes.addFlashAttribute("newsSuccess", "News article deleted.");
        return "redirect:/news";
    }
}
