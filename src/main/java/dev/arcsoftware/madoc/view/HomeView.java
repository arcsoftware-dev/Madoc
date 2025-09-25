package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.NewsController;
import dev.arcsoftware.madoc.controller.ScheduleController;
import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/")
public class HomeView {

    private final NewsController newsController;
    private final ScheduleController scheduleController;

    @Autowired
    public HomeView(NewsController newsController,
                    ScheduleController scheduleController) {
        this.newsController = newsController;
        this.scheduleController = scheduleController;
    }

    @GetMapping("/")
    public String home(Model model) {
        log.info("Fetching news articles");
        List<NewsArticleEntity> newsArticleEntities = newsController.getLatestNews(5).getBody();
        model.addAttribute("newsArticles", newsArticleEntities);

        log.info("Fetching upcoming matches");
        List<ScheduleItemDto> upcomingMatches = scheduleController.getUpcomingSchedule(3).getBody();
        model.addAttribute("upcomingMatches", upcomingMatches);

        return "index";
    }
}