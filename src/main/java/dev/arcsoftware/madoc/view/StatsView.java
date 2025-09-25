package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.StatsController;
import dev.arcsoftware.madoc.enums.PlayerType;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StatsCategory;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import dev.arcsoftware.madoc.model.request.StatsRequest;
import dev.arcsoftware.madoc.service.SeasonMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/stats")
public class StatsView {

    private final StatsController statsController;
    private final SeasonMetadataService seasonMetadataService;

    @Autowired
    public StatsView(StatsController statsController,
                     SeasonMetadataService seasonMetadataService) {
        this.statsController = statsController;
        this.seasonMetadataService = seasonMetadataService;
    }

    @GetMapping(path = "/{playerType}")
    public String getStats(
            @PathVariable(value = "playerType") PlayerType playerType,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "season-type", required = false) SeasonType seasonType,
            @RequestParam(value = "sort-field", required = false) StatsCategory sortField,
            @RequestParam(value = "sort-order", required = false) SortOrder sortOrder,
            Model model
    ) {
        var seasonData = seasonMetadataService.normalizeSeasonData(year, seasonType);

        List<StatsDto> playerStats = statsController.getStats(playerType, seasonData.left(), seasonData.right(), sortField, sortOrder)
                        .getBody();

        StatsRequest request = new StatsRequest(playerType, seasonData.left(), seasonData.right(), sortField, sortOrder);
        model.addAttribute("request", request);
        model.addAttribute("playerStats", playerStats);
        return "stats";
    }
}
