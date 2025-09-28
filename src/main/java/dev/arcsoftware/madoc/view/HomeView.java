package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.AuthorizationController;
import dev.arcsoftware.madoc.controller.NewsController;
import dev.arcsoftware.madoc.controller.ScheduleController;
import dev.arcsoftware.madoc.model.entity.NewsArticleEntity;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/")
public class HomeView {

    private final AuthorizationController authorizationController;
    private final NewsController newsController;
    private final ScheduleController scheduleController;

    @Autowired
    public HomeView(AuthorizationController authorizationController,
                    NewsController newsController,
                    ScheduleController scheduleController) {
        this.authorizationController = authorizationController;
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

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String loginRedirect(
            HttpServletResponse response,
            @RequestParam Map<String, String> paramMap
    ){
        authorizationController.login(response, paramMap);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        authorizationController.logout(response);
        return "redirect:/";
    }
}