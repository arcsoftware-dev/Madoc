package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.ScheduleController;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.GroupedScheduleDto;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import dev.arcsoftware.madoc.service.SeasonMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

@Slf4j
@Controller
@RequestMapping("/schedule")
public class ScheduleView {

    private final ScheduleController scheduleController;
    private final SeasonMetadataService seasonMetadataService;


    @Autowired
    public ScheduleView(ScheduleController scheduleController,
                        SeasonMetadataService seasonMetadataService){
        this.scheduleController = scheduleController;
        this.seasonMetadataService = seasonMetadataService;
    }

    @ModelAttribute("season_years")
    public List<Integer> seasonYears() {
        return seasonMetadataService.getAllSeasonYears();
    }

    @GetMapping("")
    public String schedule(@RequestParam(value = "year", required = false) Integer year,
                           @RequestParam(value = "season-type", required = false) SeasonType seasonType,
                           Model model
    ) {
        //Handles the vars for calls from views. Direct api calls wont allow this to be null.
        var seasonData = seasonMetadataService.normalizeSeasonData(year, seasonType);

        List<GroupedScheduleDto> groupedSchedule = scheduleController.getGroupedSchedule(seasonData.left(), seasonData.right())
                .getBody();

        List<String> teams = Optional.ofNullable(groupedSchedule).orElse(new ArrayList<>())
                .stream()
                .flatMap(gs -> gs.getGames().stream())
                .flatMap(item -> Stream.of(item.getHomeTeam(), item.getAwayTeam()))
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("teams", teams);
        model.addAttribute("year", seasonData.left());
        model.addAttribute("seasonType", seasonData.right());
        model.addAttribute("groupedSchedule", groupedSchedule);

        //Finalized games have scores (see GameRepository query); everything else is still upcoming
        List<ScheduleItemDto> allGames = Optional.ofNullable(groupedSchedule).orElse(new ArrayList<>())
                .stream()
                .flatMap(gs -> gs.getGames().stream())
                .toList();
        List<ScheduleItemDto> completedGames = allGames.stream().filter(this::isCompleted).toList();
        List<ScheduleItemDto> upcomingGames = allGames.stream().filter(g -> !isCompleted(g)).toList();

        //Upcoming: soonest first.  Completed: most recent day first.
        model.addAttribute("upcomingSchedule", groupByDay(upcomingGames, false));
        model.addAttribute("completedSchedule", groupByDay(completedGames, true));
        model.addAttribute("upcomingCount", upcomingGames.size());
        model.addAttribute("completedCount", completedGames.size());
        return "schedule-modern";
    }

    private boolean isCompleted(ScheduleItemDto game) {
        return game.getHomeScore() != null && game.getAwayScore() != null;
    }

    private List<GroupedScheduleDto> groupByDay(List<ScheduleItemDto> games, boolean newestFirst) {
        TreeMap<LocalDate, List<ScheduleItemDto>> byDay = new TreeMap<>(
                newestFirst ? Comparator.<LocalDate>reverseOrder() : Comparator.<LocalDate>naturalOrder());
        games.stream()
                .sorted(Comparator.comparing(ScheduleItemDto::getStartTime))
                .forEach(game -> byDay.computeIfAbsent(game.getStartTime().toLocalDate(), d -> new ArrayList<>()).add(game));
        return byDay.entrySet().stream()
                .map(e -> new GroupedScheduleDto(e.getKey(), e.getValue()))
                .toList();
    }
}
