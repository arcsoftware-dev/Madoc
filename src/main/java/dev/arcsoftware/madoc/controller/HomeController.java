package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.GroupedScheduleDto;
import dev.arcsoftware.madoc.model.payload.NewsArticleDto;
import dev.arcsoftware.madoc.model.payload.RuleDto;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import dev.arcsoftware.madoc.service.ConstitutionService;
import dev.arcsoftware.madoc.service.NewsService;
import dev.arcsoftware.madoc.service.ScheduleService;
import dev.arcsoftware.madoc.service.SeasonMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/")
public class HomeController {

    private final NewsService newsService;
    private final ScheduleService scheduleService;
    private final ConstitutionService constitutionService;
    private final SeasonMetadataService seasonMetadataService;

    @Autowired
    public HomeController(NewsService newsService,
                          ScheduleService scheduleService,
                          ConstitutionService constitutionService,
                          SeasonMetadataService seasonMetadataService) {
        this.newsService = newsService;
        this.scheduleService = scheduleService;
        this.constitutionService = constitutionService;
        this.seasonMetadataService = seasonMetadataService;
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

    @GetMapping("/schedule")
    public String schedule(@RequestParam(value = "year", required = false) Integer year,
                           @RequestParam(value = "season-type", required = false) SeasonType seasonType,
                           Model model) {
        List<GroupedScheduleDto> groupedSchedule = scheduleService.getGroupedSchedule(seasonType, year);
        if(year == null) {
            model.addAttribute("year", seasonMetadataService.getCurrentSeasonYear());
        }
        else{
            model.addAttribute("year", year);
        }
        if(seasonType == null) {
            model.addAttribute("seasonType", seasonMetadataService.getCurrentSeasonType());
        }
        else{
            model.addAttribute("seasonType", seasonType);
        }
        model.addAttribute("groupedSchedule", groupedSchedule);
        return "schedule";
    }
}