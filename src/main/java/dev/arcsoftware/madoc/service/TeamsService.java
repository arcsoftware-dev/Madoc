package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.*;
import dev.arcsoftware.madoc.exception.ResultNotFoundException;
import dev.arcsoftware.madoc.model.entity.TeamEntity;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import dev.arcsoftware.madoc.model.payload.TeamDataDto;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.model.request.StandingsRequest;
import dev.arcsoftware.madoc.model.request.StatsRequest;
import dev.arcsoftware.madoc.model.payload.RosterDto;
import dev.arcsoftware.madoc.repository.RosterRepository;
import dev.arcsoftware.madoc.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamsService {

    private final TeamRepository teamRepository;
    private final RosterRepository rosterRepository;
    private final StatisticsService statisticsService;
    private final StandingsService standingsService;

    @Autowired
    public TeamsService(
            TeamRepository teamRepository,
            RosterRepository rosterRepository,
            StatisticsService statisticsService,
            StandingsService standingsService
    ) {
        this.teamRepository = teamRepository;
        this.rosterRepository = rosterRepository;
        this.statisticsService = statisticsService;
        this.standingsService = standingsService;
    }

    public List<TeamEntity> getTeamsByYear(int year) {
        return teamRepository.getTeamsByYear(year);
    }

    public List<TeamEntity> getAndCreateTeamsIfNotFound(int year, Set<String> teamNames){
        //Look up team entities and add them if they don't exist
        List<TeamEntity> teamEntities = getTeamsByYear(year);
        int originalTeamCount = teamEntities.size();
        log.info("Found {} teams", originalTeamCount);

        Set<String> existingTeamEntities = teamEntities.stream().map(TeamEntity::getTeamName).collect(Collectors.toSet());

        boolean hasNewTeams = false;
        for(String team : teamNames){
            if(!existingTeamEntities.contains(team)){
                hasNewTeams = true;
                TeamEntity teamEntity = new TeamEntity();
                teamEntity.setTeamName(team);
                teamEntity.setYear(year);
                teamRepository.insertTeam(teamEntity);
            }
        }

        if(hasNewTeams){
            teamEntities = getTeamsByYear(year);
            log.info("Inserted an additional {} teams", teamEntities.size() - originalTeamCount);
        }
        return teamEntities;
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
