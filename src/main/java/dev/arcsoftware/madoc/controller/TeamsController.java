package dev.arcsoftware.madoc.controller;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.Pair;
import dev.arcsoftware.madoc.model.payload.TeamDataDto;
import dev.arcsoftware.madoc.service.TeamsService;
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
@RequestMapping("/teams")
public class TeamsController {

    private final TeamsService teamsService;
    private final SeasonMetadataService seasonMetadataService;

    @Autowired
    public TeamsController(TeamsService teamsService, SeasonMetadataService seasonMetadataService) {
        this.teamsService = teamsService;
        this.seasonMetadataService = seasonMetadataService;
    }

    @GetMapping(path = "/{teamName}")
    public String getTeamData(
            @PathVariable("teamName") String teamName,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "season-type", required = false) SeasonType seasonType,
            Model model
    ) {
        log.info("Fetching data for team with ID: {}", teamName);

        Pair<Integer, SeasonType> normalizedRequest = normalizeYearAndSeasonType(year, seasonType);
        TeamDataDto teamData = teamsService.getTeamData(teamName, normalizedRequest.left(), normalizedRequest.right());

        model.addAttribute("teamData", teamData);
        return "team-details";
    }

    @GetMapping(path = "")
    public String getAllTeams(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "season-type", required = false) SeasonType seasonType,
            Model model
    ) {
        log.info("Fetching data for all teams");

        Pair<Integer, SeasonType> normalizedRequest = normalizeYearAndSeasonType(year, seasonType);
        List<TeamDataDto> teams = teamsService.getTeams(normalizedRequest.left(), normalizedRequest.right());

        model.addAttribute("teams", teams);
        return "teams";
    }

    private Pair<Integer, SeasonType> normalizeYearAndSeasonType(Integer year, SeasonType seasonType) {
        Integer yearResult = year;
        if(yearResult == null){
            yearResult = seasonMetadataService.getCurrentSeasonYear();
        }

        SeasonType seasonTypeResult = seasonType;
        if(seasonTypeResult == null){
            seasonTypeResult = seasonMetadataService.getCurrentSeasonType();
        }

        return new Pair<>(yearResult, seasonTypeResult);
    }
}