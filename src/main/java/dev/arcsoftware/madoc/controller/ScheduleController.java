package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.GroupedScheduleDto;
import dev.arcsoftware.madoc.service.ScheduleService;
import dev.arcsoftware.madoc.service.SeasonMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final SeasonMetadataService seasonMetadataService;


    public ScheduleController(ScheduleService scheduleService, SeasonMetadataService seasonMetadataService) {
        this.scheduleService = scheduleService;
        this.seasonMetadataService = seasonMetadataService;
    }


    @GetMapping("")
    public String schedule(@RequestParam(value = "year", required = false) Integer year,
                           @RequestParam(value = "season-type", required = false) SeasonType seasonType,
                           Model model
    ) {
        if (seasonType == null) {
            seasonType = seasonMetadataService.getCurrentSeasonType();
        }
        if (year == null) {
            year = seasonMetadataService.getCurrentSeasonYear();
        }
        log.info("Fetching schedule for year: {}, seasonType: {}", year, seasonType);

        List<GroupedScheduleDto> groupedSchedule = scheduleService.getGroupedSchedule(seasonType, year);

        List<String> teams = scheduleService.getSchedule(seasonType, year).stream()
                .flatMap(item -> List.of(item.getHomeTeam(), item.getAwayTeam()).stream())
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("teams", teams);
        model.addAttribute("year", year);
        model.addAttribute("seasonType", seasonType);
        model.addAttribute("groupedSchedule", groupedSchedule);
        return "schedule";
    }

    @GetMapping(value = "/printable", produces = "application/csv")
    public ResponseEntity<Resource> printable(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "season-type", required = false) SeasonType seasonType,
            @RequestParam(value = "team-filter", required = false) String teamFilter
    ) {
        if (seasonType == null) {
            seasonType = seasonMetadataService.getCurrentSeasonType();
        }
        if (year == null) {
            year = seasonMetadataService.getCurrentSeasonYear();
        }
        log.info("Fetching printable schedule for year: {}, seasonType: {}", year, seasonType);

        byte[] csvBytes = scheduleService.generatePrintableScheduleCsv(year, seasonType, teamFilter);
        String team = teamFilter == null ? "All" : teamFilter;
        String fileName = String.format("MMHL-Schedule-%s-%d-%s.csv", team, year, seasonType.name().toLowerCase());
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csvBytes));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(resource);
    }
}
