package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.ScheduleController;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.GroupedScheduleDto;
import dev.arcsoftware.madoc.service.SeasonMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        return "schedule";
    }
}
