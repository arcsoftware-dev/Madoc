package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.enums.PlayerType;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StatsCategory;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import dev.arcsoftware.madoc.model.request.StatsRequest;
import dev.arcsoftware.madoc.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/stats")
public class StatsController {

    private final StatisticsService statisticsService;

    @Autowired
    public StatsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping(path = "/{year}/{season-type}/{player-type}")
    public ResponseEntity<List<StatsDto>> getStats(
            @PathVariable(value = "player-type") PlayerType playerType,
            @PathVariable(value = "year") Integer year,
            @PathVariable(value = "season-type") SeasonType seasonType,
            @RequestParam(value = "sort-field", required = false) StatsCategory sortField,
            @RequestParam(value = "sort-order", required = false) SortOrder sortOrder
    ) {
        StatsRequest request = new StatsRequest(playerType, year, seasonType, sortField, sortOrder);
        log.info("Received stats request: {}", request);

        List<StatsDto> playerStats = statisticsService.getStats(request);
        return ResponseEntity.ok(playerStats);
    }
}
