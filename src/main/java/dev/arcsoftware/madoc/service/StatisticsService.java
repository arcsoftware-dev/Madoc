package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.PlayerType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StatsCategory;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import dev.arcsoftware.madoc.model.request.StatsRequest;
import dev.arcsoftware.madoc.repository.StatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Function;

@Slf4j
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
            stats = statsRepository.getGoalieStats(request.year(), request.seasonType());
        }
        else {
            stats = statsRepository.getSkaterStats(request.year(), request.seasonType());
        }
        sortStats(request, stats);

        addRankings(stats, request.sortCategory());

        return stats;
    }

    public List<StatsDto> getStatsByTeam(String teamName, StatsRequest request) {
        log.info("Fetching stats for team: {} with request: {}", teamName, request);
        List<StatsDto> teamStats = getStats(request).stream()
                .filter(stat -> stat.getTeamName().equalsIgnoreCase(teamName))
                .toList();
        addRankings(teamStats, request.sortCategory());
        return teamStats;
    }

    private void addRankings(List<StatsDto> stats, StatsCategory sortCategory) {
        Function<StatsDto, Number> extractor = valueExtractor(sortCategory);
        int rank = 1;
        int increment = 0;

        if(CollectionUtils.isEmpty(stats)) {
            log.warn("No stats available to rank.");
            return;
        }
        Number previousValue = extractor.apply(stats.getFirst());

        for (StatsDto stat : stats) {
            stat.setRank(0); // Reset rank before calculation
            Number currentValue = extractor.apply(stat);

            if(previousValue.equals(currentValue)) {
                increment++;
            }
            else{
                rank += increment;
                increment = 1;
                previousValue = currentValue;
            }
            stat.setRank(rank);

        }
    }

    private Function<StatsDto, Number> valueExtractor(StatsCategory sortCategory) {
        return (stat) -> switch (sortCategory) {
            case GAMES_PLAYED -> stat.getGamesPlayed();
            case GOALS -> stat.getGoals();
            case ASSISTS -> stat.getAssists();
            case PENALTY_MINUTES -> stat.getPenaltyMinutes();
            case WINS -> stat.getWins();
            case LOSSES -> stat.getLosses();
            case TIES -> stat.getTies();
            case SHUTOUTS -> stat.getShutouts();
            case GOALS_AGAINST -> stat.getGoalsAgainst();
            case POINTS -> stat.getPoints();
            case POINTS_PER_GAME -> (double)stat.getPoints()/stat.getGamesPlayed();
            case GOALS_AGAINST_AVERAGE -> (double)stat.getGoalsAgainst()/stat.getGamesPlayed();
            default -> Integer.MAX_VALUE;
        };
    };

    private void sortStats(StatsRequest request, List<StatsDto> stats) {
        stats.sort((stat1, stat2) -> {
            int comparison = switch (request.sortCategory()) {
                case GAMES_PLAYED -> Integer.compare(stat2.getGamesPlayed(), stat1.getGamesPlayed());
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
