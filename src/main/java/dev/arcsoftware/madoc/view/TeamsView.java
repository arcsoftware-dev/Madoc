package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.controller.RosterController;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.Pair;
import dev.arcsoftware.madoc.model.payload.RosterAssignmentDto;
import dev.arcsoftware.madoc.model.payload.TeamDataDto;
import dev.arcsoftware.madoc.service.SeasonMetadataService;
import dev.arcsoftware.madoc.service.TeamsService;
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
public class TeamsView {

    private final RosterController rosterController;
    private final TeamsService teamsService;
    private final SeasonMetadataService seasonMetadataService;

    @Autowired
    public TeamsView(RosterController rosterController,
                     TeamsService teamsService,
                     SeasonMetadataService seasonMetadataService
    ) {
        this.rosterController = rosterController;
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
        Pair<Integer, SeasonType> normalizedRequest = seasonMetadataService.normalizeSeasonData(year, seasonType);

        //We need to normalize the team name to match how it's stored.  Teams like the 'Red Wings' will come in as 'RedWings', and db lookup will fail otherwise
        String normalizedTeamName = normalizeTeamName(teamName);

        List<RosterAssignmentDto> roster = rosterController.getRosterAssignmentsByTeam(normalizedRequest.left(), normalizedTeamName).getBody();

        TeamDataDto teamData = teamsService.getTeamData(roster, normalizedRequest.left(), normalizedRequest.right());

        model.addAttribute("teamData", teamData);
        return "team-details";
    }

    private String normalizeTeamName(String teamName) {
        StringBuilder normalizedTeamName = new StringBuilder();
        char[] chars = teamName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (i > 0 && Character.isUpperCase(c)) {
                normalizedTeamName.append(" ");
            }
            normalizedTeamName.append(c);
        }
        return normalizedTeamName.toString();
    }

    //Just need teamname and roster
    @GetMapping(path = "")
    public String getAllTeams(
            @RequestParam(value = "year", required = false) Integer year,
            Model model
    ) {
        log.info("Fetching data for all teams");

        Pair<Integer, SeasonType> normalizedRequest = seasonMetadataService.normalizeSeasonData(year, null);
        var rosters = rosterController.getRostersByYear(normalizedRequest.left()).getBody();

        //convert to TeamDataDto
        assert rosters != null;
        List<TeamDataDto> teams = rosters.stream()
                .map(r -> TeamDataDto.builder()
                .teamName(r.getFirst().getTeamName())
                        .roster(r)
                        .build())
                .toList();

        model.addAttribute("teams", teams);
        return "teams";
    }
}