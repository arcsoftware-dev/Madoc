package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.model.payload.NewsArticleDto;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import dev.arcsoftware.madoc.service.NewsService;
import dev.arcsoftware.madoc.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
public class HomeController {

    private final NewsService newsService;
    private final ScheduleService scheduleService;

    @Autowired
    public HomeController(NewsService newsService, ScheduleService scheduleService) {
        this.newsService = newsService;
        this.scheduleService = scheduleService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<NewsArticleDto> newsArticleDtos = newsService.getNewsArticles();
        model.addAttribute("newsArticles", newsArticleDtos);

        List<ScheduleItemDto> upcomingMatches = scheduleService.getUpcomingMatches();
        model.addAttribute("upcomingMatches", upcomingMatches);

        return "index";
    }
}