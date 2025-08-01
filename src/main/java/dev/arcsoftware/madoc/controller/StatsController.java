package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.enums.PlayerType;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StatsCategory;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import dev.arcsoftware.madoc.model.request.StatsRequest;
import dev.arcsoftware.madoc.service.SeasonMetadataService;
import dev.arcsoftware.madoc.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static dev.arcsoftware.madoc.enums.StatsCategory.POINTS;

@Slf4j
@Controller
@RequestMapping("/stats")
public class StatsController {

    private final StatisticsService statisticsService;
    private final SeasonMetadataService seasonMetadataService;

    @Autowired
    public StatsController(StatisticsService statisticsService,
                           SeasonMetadataService seasonMetadataService) {
        this.statisticsService = statisticsService;
        this.seasonMetadataService = seasonMetadataService;
    }

    @GetMapping(path = "/{playerType}")
    public String getStats(
            @PathVariable(value = "playerType") PlayerType playerType,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "season-type", required = false) SeasonType seasonType,
            @RequestParam(value = "sort-field", required = false) StatsCategory sortField,
            @RequestParam(value = "sort-order", required = false, defaultValue = "DESC") SortOrder sortOrder,
            Model model
    ) {
        StatsRequest request = normalizeRequest(playerType, year, seasonType, sortField, sortOrder);

        model.addAttribute("request", request);
        log.info("Received stats request: {}", request);

        List<StatsDto> playerStats = statisticsService.getStats(request);

        model.addAttribute("playerStats", playerStats);
        return "stats";
    }


    private StatsRequest normalizeRequest(
            PlayerType playerType,
            Integer year,
            SeasonType seasonType,
            StatsCategory sortField,
            SortOrder sortOrder
    ) {
        return new StatsRequest(
                playerType,
                year == null ? seasonMetadataService.getCurrentSeasonYear() : year,
                seasonType == null ? seasonMetadataService.getCurrentSeasonType() : seasonType,
                sortField == null
                        ? (PlayerType.GOALIES.equals(playerType)
                            ? StatsCategory.GOALS_AGAINST_AVERAGE
                            : POINTS)
                        : sortField,
                sortOrder
        );
    }
}
