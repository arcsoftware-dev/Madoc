package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StandingsCategory;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.model.request.StandingsRequest;
import dev.arcsoftware.madoc.service.SeasonMetadataService;
import dev.arcsoftware.madoc.service.StandingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/standings")
public class StandingsController {

    private final StandingsService standingsService;
    private final SeasonMetadataService seasonMetadataService;

    @Autowired
    public StandingsController(StandingsService standingsService,
                               SeasonMetadataService seasonMetadataService) {
        this.standingsService = standingsService;
        this.seasonMetadataService = seasonMetadataService;
    }

    @GetMapping(path = "/")
    public String getStandings(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "season-type", required = false) SeasonType seasonType,
            @RequestParam(value = "sort-field", required = false) StandingsCategory sortField,
            @RequestParam(value = "sort-order", required = false) SortOrder sortOrder,
            Model model
    ) {
        StandingsRequest request = normalizeRequest(year, seasonType, sortField, sortOrder);

        model.addAttribute("request", request);
        log.info("Received standings request: {}", request);

        List<TeamStatsDto> playerStats = standingsService.getStandings(request);

        model.addAttribute("teamStats", playerStats);
        return "standings";
    }


    private StandingsRequest normalizeRequest(
            Integer year,
            SeasonType seasonType,
            StandingsCategory sortField,
            SortOrder sortOrder
    ) {
        return new StandingsRequest(
                year == null ? seasonMetadataService.getCurrentSeasonYear() : year,
                seasonType == null ? seasonMetadataService.getCurrentSeasonType() : seasonType,
                sortField == null ? StandingsCategory.POINTS  : sortField,
                sortOrder == null ? SortOrder.DESC : sortOrder
        );
    }
}
