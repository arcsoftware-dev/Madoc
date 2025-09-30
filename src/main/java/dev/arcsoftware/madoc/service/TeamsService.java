package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.*;
import dev.arcsoftware.madoc.model.entity.TeamEntity;
import dev.arcsoftware.madoc.model.payload.RosterAssignmentDto;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import dev.arcsoftware.madoc.model.payload.TeamDataDto;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.model.request.StandingsRequest;
import dev.arcsoftware.madoc.model.request.StatsRequest;
import dev.arcsoftware.madoc.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamsService {

    private final TeamRepository teamRepository;
    private final StatisticsService statisticsService;
    private final StandingsService standingsService;

    @Autowired
    public TeamsService(
            TeamRepository teamRepository,
            StatisticsService statisticsService,
            StandingsService standingsService
    ) {
        this.teamRepository = teamRepository;
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

    public TeamDataDto getTeamData(List<RosterAssignmentDto> roster, int year, SeasonType seasonType) {
        roster.sort(Comparator.comparing(player -> player.getDraftPosition().getRank()));
        String teamName = roster.getFirst().getTeamName();

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

        return TeamDataDto.builder()
                .teamName(teamName)
                .roster(roster)
                .playerStats(playerStats)
                .goalieStats(goalieStats)
                .teamStats(teamStats)
                .build();
    }

    public boolean teamExistsById(Integer teamId) {
        return teamRepository.teamExistsById(teamId);
    }
}
