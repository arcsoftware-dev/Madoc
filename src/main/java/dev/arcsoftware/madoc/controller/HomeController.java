package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.model.payload.NewsArticleDto;
import dev.arcsoftware.madoc.model.payload.RuleDto;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import dev.arcsoftware.madoc.service.ConstitutionService;
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
    private final ConstitutionService constitutionService;

    @Autowired
    public HomeController(NewsService newsService, ScheduleService scheduleService, ConstitutionService constitutionService) {
        this.newsService = newsService;
        this.scheduleService = scheduleService;
        this.constitutionService = constitutionService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<NewsArticleDto> newsArticleDtos = newsService.getNewsArticles();
        model.addAttribute("newsArticles", newsArticleDtos);

        List<ScheduleItemDto> upcomingMatches = scheduleService.getUpcomingMatches();
        model.addAttribute("upcomingMatches", upcomingMatches);

        return "index";
    }

    @GetMapping("/constitution")
    public String constitution(Model model) {
        List<RuleDto> rules = constitutionService.getRules();
        model.addAttribute("rules", rules);
        return "constitution";
    }
}