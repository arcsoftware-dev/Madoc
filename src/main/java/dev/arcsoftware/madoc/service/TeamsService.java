package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.*;
import dev.arcsoftware.madoc.exception.ResultNotFoundException;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import dev.arcsoftware.madoc.model.payload.TeamDataDto;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.model.request.StandingsRequest;
import dev.arcsoftware.madoc.model.request.StatsRequest;
import dev.arcsoftware.madoc.model.timesheet.RosterDto;
import dev.arcsoftware.madoc.repository.RosterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamsService {

    private final RosterRepository rosterRepository;
    private final StatisticsService statisticsService;
    private final StandingsService standingsService;

    @Autowired
    public TeamsService(
            RosterRepository rosterRepository,
            StatisticsService statisticsService,
            StandingsService standingsService
    ) {
        this.rosterRepository = rosterRepository;
        this.statisticsService = statisticsService;
        this.standingsService = standingsService;
    }

    public TeamDataDto getTeamData(String teamName, int year, SeasonType seasonType) {
        TeamDataDto teamDataDto = getTeams(year, seasonType)
                .stream()
                .filter(team -> teamName.equalsIgnoreCase(team.getTeamName().replaceAll(" ", "")))
                .findFirst()
                .orElse(null);

        if(teamDataDto == null) {
            throw new ResultNotFoundException("Team not found: " + teamName);
        }
        else {
            return teamDataDto;
        }
    }

    public List<TeamDataDto> getTeams(int year, SeasonType seasonType) {
        List<TeamDataDto> teams = new ArrayList<>();
        List<RosterDto> rosterItems = rosterRepository.getRostersByYear(year);

        Map<String, List<RosterDto>> rosters = rosterItems.stream()
                .collect(Collectors.groupingBy(RosterDto::getTeamName));

        rosters.keySet().forEach(teamName -> {
            List<RosterDto> teamRoster = rosters.get(teamName)
                    .stream()
                    .sorted(Comparator.comparing(player -> player.getDraftRank().getRank()))
                    .toList();
            List<StatsDto> playerStats = statisticsService.getStatsByTeam(
                    teamName,
                    new StatsRequest(PlayerType.SKATERS, year, seasonType, StatsCategory.POINTS, SortOrder.DESC)
            );
            List<StatsDto> goalieStats = statisticsService.getStatsByTeam(
                    teamName,
                    new StatsRequest(PlayerType.GOALIES, year, seasonType, StatsCategory.WINS, SortOrder.DESC)
            );
            TeamStatsDto teamStats = standingsService.getTeamStats(
                    teamName,
                    new StandingsRequest(year, seasonType, StandingsCategory.POINTS, SortOrder.DESC)
            );

            TeamDataDto teamData = TeamDataDto.builder()
                    .teamName(teamName)
                    .roster(teamRoster)
                    .playerStats(playerStats)
                    .goalieStats(goalieStats)
                    .teamStats(teamStats)
                    .build();

            teams.add(teamData);
        });

        return teams;
    }
}
