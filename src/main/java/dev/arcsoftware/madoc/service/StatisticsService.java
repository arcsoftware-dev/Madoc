package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.PlayerType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import dev.arcsoftware.madoc.model.request.StatsRequest;
import dev.arcsoftware.madoc.repository.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsService {

    private final StatsRepository statsRepository;

    @Autowired
    public StatisticsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public List<StatsDto> getStats(StatsRequest request) {
        List<StatsDto> stats;
        if(PlayerType.GOALIES.equals(request.playerType())) {
            stats = statsRepository.getGoalieStats();
        }
        else {
            stats = statsRepository.getSkaterStats();
        }
        sortStats(request, stats);

        return stats;
    }

    private void sortStats(StatsRequest request, List<StatsDto> stats) {
        stats.sort((stat1, stat2) -> {
            int comparison = switch (request.sortCategory()) {
                case GOALS -> Integer.compare(stat2.getGoals(), stat1.getGoals());
                case ASSISTS -> Integer.compare(stat2.getAssists(), stat1.getAssists());
                case PENALTY_MINUTES -> Integer.compare(stat2.getPenaltyMinutes(), stat1.getPenaltyMinutes());
                case WINS -> Integer.compare(stat2.getWins(), stat1.getWins());
                case LOSSES -> Integer.compare(stat2.getLosses(), stat1.getLosses());
                case TIES -> Integer.compare(stat2.getTies(), stat1.getTies());
                case SHUTOUTS -> Integer.compare(stat2.getShutouts(), stat1.getShutouts());
                case GOALS_AGAINST -> Integer.compare(stat2.getGoalsAgainst(), stat1.getGoalsAgainst());
                case GOALS_AGAINST_AVERAGE -> {
                    double gaa1 = (double) stat1.getGoalsAgainst() / stat1.getGamesPlayed();
                    double gaa2 = (double) stat2.getGoalsAgainst() / stat2.getGamesPlayed();
                    yield Double.compare(gaa2, gaa1);
                }
                case POINTS_PER_GAME -> {
                    double ppg1 = (double) stat1.getPoints() / stat1.getGamesPlayed();
                    double ppg2 = (double) stat2.getPoints() / stat2.getGamesPlayed();
                    yield Double.compare(ppg2, ppg1);
                }
                default -> {
                    if(request.playerType().equals(PlayerType.SKATERS)){
                        yield Integer.compare(stat2.getPoints(), stat1.getPoints());
                    }
                    else{
                        yield Integer.compare(stat2.getWins(), stat1.getWins());
                    }
                }
            };

            return request.sortOrder() == SortOrder.DESC ? comparison : -comparison;
        });
    }
}
