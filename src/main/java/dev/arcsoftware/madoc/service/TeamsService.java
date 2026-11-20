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

import java.util.ArrayList;
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

    public Optional<TeamDataDto> getTeamData(String requestedTeamName, List<RosterAssignmentDto> roster, int year, SeasonType seasonType) {
        List<RosterAssignmentDto> safeRoster = roster != null ? new ArrayList<>(roster) : new ArrayList<>();
        String resolvedTeamName = requestedTeamName;

        if (!safeRoster.isEmpty()) {
            safeRoster.sort(Comparator.comparing(player -> player.getDraftPosition().getRank()));
            resolvedTeamName = safeRoster.getFirst().getTeamName();
        }

        List<StatsDto> playerStats = statisticsService.getStatsByTeam(
                resolvedTeamName,
                new StatsRequest(PlayerType.SKATERS, year, seasonType, StatsCategory.POINTS, SortOrder.DESC)
        );
        List<StatsDto> goalieStats = statisticsService.getStatsByTeam(
                resolvedTeamName,
                new StatsRequest(PlayerType.GOALIES, year, seasonType, StatsCategory.WINS, SortOrder.DESC)
        );
        TeamStatsDto teamStats = standingsService.getTeamStats(
                resolvedTeamName,
                new StandingsRequest(year, seasonType, StandingsCategory.POINTS, SortOrder.DESC)
        );

        boolean existsInSeason = !safeRoster.isEmpty()
                || teamStats != null
                || !playerStats.isEmpty()
                || !goalieStats.isEmpty();

        if (!existsInSeason && seasonType == SeasonType.PLAYOFFS) {
            TeamStatsDto regularStats = standingsService.getTeamStats(
                    resolvedTeamName,
                    new StandingsRequest(year, SeasonType.REGULAR_SEASON, StandingsCategory.POINTS, SortOrder.DESC)
            );
            if (regularStats != null) {
                existsInSeason = true;
                resolvedTeamName = regularStats.getTeamName();
            }
        }

        if (!existsInSeason) {
            return Optional.empty();
        }

        if (safeRoster.isEmpty()) {
            if (teamStats != null && teamStats.getTeamName() != null) {
                resolvedTeamName = teamStats.getTeamName();
            } else if (!playerStats.isEmpty() && playerStats.getFirst().getTeamName() != null) {
                resolvedTeamName = playerStats.getFirst().getTeamName();
            } else if (!goalieStats.isEmpty() && goalieStats.getFirst().getTeamName() != null) {
                resolvedTeamName = goalieStats.getFirst().getTeamName();
            }
        }

        return Optional.of(TeamDataDto.builder()
                .teamName(resolvedTeamName)
                .roster(safeRoster)
                .playerStats(playerStats)
                .goalieStats(goalieStats)
                .teamStats(teamStats)
                .build());
    }

    public List<Integer> findSeasonsForTeam(String teamName, List<Integer> allYears) {
        if (teamName == null || teamName.isBlank() || allYears == null) {
            return List.of();
        }
        List<Integer> matchedYears = new ArrayList<>();
        for (Integer year : allYears) {
            if (year == null) {
                continue;
            }
            boolean inTeamsTable = teamRepository.getTeamsByYear(year).stream()
                    .anyMatch(t -> t.getTeamName() != null && t.getTeamName().equalsIgnoreCase(teamName));
            boolean inStandings = !inTeamsTable && standingsService.getTeamStats(
                    teamName,
                    new StandingsRequest(year, SeasonType.REGULAR_SEASON, StandingsCategory.POINTS, SortOrder.DESC)
            ) != null;
            if (inTeamsTable || inStandings) {
                matchedYears.add(year);
            }
        }
        matchedYears.sort(Comparator.reverseOrder());
        return matchedYears;
    }

    public boolean teamExistsById(Integer teamId) {
        return teamRepository.teamExistsById(teamId);
    }
}
