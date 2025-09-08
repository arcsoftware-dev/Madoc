package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.model.request.StandingsRequest;
import dev.arcsoftware.madoc.repository.StandingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StandingsService {

    private final StandingsRepository standingsRepository;

    @Autowired
    public StandingsService(StandingsRepository standingsRepository) {
        this.standingsRepository = standingsRepository;
    }

    public List<TeamStatsDto> getStandings(StandingsRequest request) {
        List<TeamStatsDto> stats = standingsRepository.getTeamStandings(request.year(), request.seasonType());
        sortStats(request, stats);

        return stats;
    }

    public TeamStatsDto getTeamStats(String teamName, StandingsRequest request) {
        return getStandings(request)
                .stream()
                .filter(teamStatsDto -> teamStatsDto.getTeamName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);
    }

    private void sortStats(StandingsRequest request, List<TeamStatsDto> stats) {
        stats.sort((stat1, stat2) -> {
            int comparison = switch (request.sortCategory()) {
                case GAMES_PLAYED -> Integer.compare(stat2.getGamesPlayed(), stat1.getGamesPlayed());
                case PENALTY_MINUTES -> Integer.compare(stat2.getPenaltyMinutes(), stat1.getPenaltyMinutes());
                case WINS -> Integer.compare(stat2.getWins(), stat1.getWins());
                case LOSSES -> Integer.compare(stat2.getLosses(), stat1.getLosses());
                case TIES -> Integer.compare(stat2.getTies(), stat1.getTies());
                case GOALS_FOR -> Integer.compare(stat2.getGoalsFor(), stat1.getGoalsFor());
                case GOALS_AGAINST -> Integer.compare(stat2.getGoalsAgainst(), stat1.getGoalsAgainst());
                case POINT_PERCENTAGE -> {
                    double ppg1 = (double) stat1.getPoints() / (stat1.getGamesPlayed()*2);
                    double ppg2 = (double) stat2.getPoints() / (stat2.getGamesPlayed()*2);
                    yield Double.compare(ppg2, ppg1);
                }
                default -> Integer.compare(stat2.getPoints(), stat1.getPoints());
            };

            return request.sortOrder() == SortOrder.DESC ? comparison : -comparison;
        });
    }
}
