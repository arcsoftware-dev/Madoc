package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.enums.SortOrder;
import dev.arcsoftware.madoc.enums.StandingsCategory;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.model.request.StandingsRequest;
import dev.arcsoftware.madoc.repository.StandingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
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
        return applyTieBreakers(request, stats);
    }

    public TeamStatsDto getTeamStats(String teamName, StandingsRequest request) {
        return getStandings(request)
                .stream()
                .filter(teamStatsDto -> teamStatsDto.getTeamName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);
    }

    private List<TeamStatsDto> applyTieBreakers(StandingsRequest request, List<TeamStatsDto> stats) {
        if(!request.sortCategory().equals(StandingsCategory.POINTS)) return stats;

        List<TeamStatsDto> sortedStats = new ArrayList<>(stats.size());
        int index = 0;
        while(index < stats.size()){
            //Find all teams with the same points
            int points = stats.get(index).getPoints();
            int tieEndIndex = index;
            while(tieEndIndex < stats.size()-1){
                if(stats.get(tieEndIndex+1).getPoints() == points){
                    tieEndIndex++;
                }
                else {
                    break;
                }
            }

            int numberOfTeamsTied = tieEndIndex - index + 1;
            if(numberOfTeamsTied == 1){
                sortedStats.add(stats.get(index));
            }
            //2-team tiebreaker
            else if(numberOfTeamsTied == 2){
                List<TeamStatsDto> tiedTeams = stats.subList(index, tieEndIndex + 1);
                List<TeamStatsDto> tieBrokenTeams = applyTwoTeamTieBreaker(tiedTeams, request);
                sortedStats.addAll(tieBrokenTeams);
            }
            //3+ team tiebreaker
            else {
                List<TeamStatsDto> tiedTeams = stats.subList(index, tieEndIndex + 1);
                List<TeamStatsDto> tieBrokenTeams = applyThreePlusTeamTieBreaker(tiedTeams, request);
                sortedStats.addAll(tieBrokenTeams);
            }

            index += numberOfTeamsTied;
        }
        return sortedStats;
    }

    private List<TeamStatsDto> applyTwoTeamTieBreaker(List<TeamStatsDto> tiedTeams, StandingsRequest request){
        //RS: Wins, losses, wins H-H, fewest GA in H-H, fewest PM, fewest PM H-H, fewest GA, alphabetical
        //PL: Wins, wins H-H, losses, fewest GA in H-H, fewest PM, fewest PM H-H, fewest GA, alphabetical
        if(request.seasonType().equals(SeasonType.REGULAR_SEASON)){
            log.info("Applying regular season tiebreaker for teams: {} and {}", tiedTeams.get(0).getTeamName(), tiedTeams.get(1).getTeamName());
            return applyTwoTeamTieBreakerRegularSeason(tiedTeams, request);
        }
        else {
            log.info("Applying playoff tiebreaker for teams: {} and {}", tiedTeams.get(0).getTeamName(), tiedTeams.get(1).getTeamName());
            return applyTwoTeamTieBreakerPlayoffs(tiedTeams, request);
        }
    }

    private List<TeamStatsDto> applyTwoTeamTieBreakerRegularSeason(List<TeamStatsDto> tiedTeams, StandingsRequest request){
        //RS: Wins, losses, wins H-H, fewest GA in H-H, fewest PM, fewest PM H-H, fewest GA, alphabetical
        //TODO implement H-H tiebreakers
        tiedTeams.sort(
                Comparator.comparing(TeamStatsDto::getWins).reversed()
                        .thenComparing(TeamStatsDto::getLosses)
                        .thenComparing(TeamStatsDto::getPenaltyMinutes)
                        .thenComparing(TeamStatsDto::getGoalsAgainst)
                        .thenComparing(TeamStatsDto::getTeamName)
        );
        return tiedTeams;
    }

    private List<TeamStatsDto> applyTwoTeamTieBreakerPlayoffs(List<TeamStatsDto> tiedTeams, StandingsRequest request){
        //PL: Wins, wins H-H, losses, fewest GA in H-H, fewest PM, fewest PM H-H, fewest GA, alphabetical
        //TODO implement H-H tiebreakers
        tiedTeams.sort(
                Comparator.comparing(TeamStatsDto::getWins).reversed()
                        .thenComparing(TeamStatsDto::getLosses)
                        .thenComparing(TeamStatsDto::getPenaltyMinutes)
                        .thenComparing(TeamStatsDto::getGoalsAgainst)
                        .thenComparing(TeamStatsDto::getTeamName)
        );
        return tiedTeams;
    }

    private List<TeamStatsDto> applyThreePlusTeamTieBreaker(List<TeamStatsDto> tiedTeams, StandingsRequest request){
        log.info("Applying three plus team tiebreaker for teams: {}", tiedTeams.stream().map(TeamStatsDto::getTeamName).toList());

        //RS: Wins, losses, fewest PM, GF, GA, alphabetical
        //PL: Wins, losses, fewest PM, GF, GA, alphabetical
        tiedTeams.sort(
                Comparator.comparing(TeamStatsDto::getWins).reversed()
                .thenComparing(TeamStatsDto::getLosses)
                .thenComparing(TeamStatsDto::getPenaltyMinutes)
                .thenComparing(TeamStatsDto::getGoalsFor).reversed()
                .thenComparing(TeamStatsDto::getGoalsAgainst)
                .thenComparing(TeamStatsDto::getTeamName)
        );

        return tiedTeams;
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
