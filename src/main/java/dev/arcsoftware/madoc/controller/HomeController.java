package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.model.entity.RuleEntity;
import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import dev.arcsoftware.madoc.service.ConstitutionService;
import dev.arcsoftware.madoc.service.NewsService;
import dev.arcsoftware.madoc.service.ScheduleService;
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
public class HomeController {

    private final NewsService newsService;
    private final ScheduleService scheduleService;
    private final ConstitutionService constitutionService;

    @Autowired
    public HomeController(NewsService newsService,
                          ScheduleService scheduleService,
                          ConstitutionService constitutionService) {
        this.newsService = newsService;
        this.scheduleService = scheduleService;
        this.constitutionService = constitutionService;
    }

    @GetMapping("/")
    public String home(Model model) {
        log.info("Fetching news articles");
        List<NewsArticleEntity> newsArticleEntities = newsService.getNewsArticles();
        model.addAttribute("newsArticles", newsArticleEntities);

        log.info("Fetching upcoming matches");
        List<ScheduleItemDto> upcomingMatches = scheduleService.getUpcomingMatches();
        model.addAttribute("upcomingMatches", upcomingMatches);

        return "index";
    }

    @GetMapping("/constitution")
    public String constitution(Model model) {
        log.info("Fetching constitution rules");
        List<RuleEntity> rules = constitutionService.getRules();
        model.addAttribute("rules", rules);
        return "constitution";
    }
}