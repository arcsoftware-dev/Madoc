package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StandingsCategory;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.model.request.StandingsRequest;
import dev.arcsoftware.madoc.service.StandingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/standings")
public class StandingsController {
    private final StandingsService standingsService;

    @Autowired
    public StandingsController(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    @GetMapping(path = "")
    public ResponseEntity<List<TeamStatsDto>> getStandings(
            @RequestParam(value = "year", required = true) Integer year,
            @RequestParam(value = "season-type", required = true) SeasonType seasonType,
            @RequestParam(value = "sort-field", required = false) StandingsCategory sortField,
            @RequestParam(value = "sort-order", required = false) SortOrder sortOrder
    ) {
        StandingsRequest request = new StandingsRequest(year, seasonType, sortField, sortOrder);

        log.info("Received standings request: {}", request);
        List<TeamStatsDto> teamStats = standingsService.getStandings(request);
        return ResponseEntity.ok(teamStats);
    }
}
