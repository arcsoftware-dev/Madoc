package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.StandingsController;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StandingsCategory;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.model.request.StandingsRequest;
import dev.arcsoftware.madoc.service.SeasonMetadataService;
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
public class StandingsView {

    private final StandingsController standingsController;
    private final SeasonMetadataService seasonMetadataService;

    @Autowired
    public StandingsView(StandingsController standingsController,
                         SeasonMetadataService seasonMetadataService) {
        this.standingsController = standingsController;
        this.seasonMetadataService = seasonMetadataService;
    }

    @GetMapping(path = "")
    public String getStandings(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "season-type", required = false) SeasonType seasonType,
            @RequestParam(value = "sort-field", required = false) StandingsCategory sortField,
            @RequestParam(value = "sort-order", required = false) SortOrder sortOrder,
            Model model
    ) {
        var seasonData = seasonMetadataService.normalizeSeasonData(year, seasonType);
        List<TeamStatsDto> teamStats = standingsController.getStandings(seasonData.left(), seasonData.right(), sortField, sortOrder)
                .getBody();

        StandingsRequest request = new StandingsRequest(seasonData.left(), seasonData.right(), sortField, sortOrder);

        model.addAttribute("request", request);
        model.addAttribute("teamStats", teamStats);
        return "standings";
    }
}
