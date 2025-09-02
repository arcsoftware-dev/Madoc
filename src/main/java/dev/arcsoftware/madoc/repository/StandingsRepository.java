package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class StandingsRepository {
    private static List<TeamStatsDto> staticSeasonStats;
    private static List<TeamStatsDto> staticPlayoffStats;

    @PostConstruct
    public void loadData(){
        staticSeasonStats = new ArrayList<>();
        ClassPathResource statsResource = new ClassPathResource("data/standings/2024_SEASON.csv");
        try(BufferedReader seasonStats = new BufferedReader(new BufferedReader(new InputStreamReader(statsResource.getInputStream())))) {
            seasonStats.lines()
                    .skip(1) // Skip header line
                    .forEach(line -> {
                        //Team, GP, W, L, T, P, GF, GA, PIM
                        String[] split = line.split(",");
                        TeamStatsDto statsDto = TeamStatsDto.builder()
                                .teamName(split[0])
                                .gamesPlayed(Integer.parseInt(split[1]))
                                .wins(Integer.parseInt(split[2]))
                                .losses(Integer.parseInt(split[3]))
                                .ties(Integer.parseInt(split[4]))
                                .points(Integer.parseInt(split[5]))
                                .goalsFor(Integer.parseInt(split[6]))
                                .goalsAgainst(Integer.parseInt(split[7]))
                                .penaltyMinutes(Integer.parseInt(split[8]))
                                .build();
                        staticSeasonStats.add(statsDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        staticPlayoffStats = new ArrayList<>();
        ClassPathResource playoffStatsResource = new ClassPathResource("data/standings/2024_PLAYOFFS.csv");
        try(BufferedReader playoffStats = new BufferedReader(new BufferedReader(new InputStreamReader(playoffStatsResource.getInputStream())))) {
            playoffStats.lines()
                    .skip(1) // Skip header line
                    .forEach(line -> {
                        //Team, GP, W, L, T, P, GF, GA, PIM
                        String[] split = line.split(",");
                        TeamStatsDto statsDto = TeamStatsDto.builder()
                                .teamName(split[0])
                                .gamesPlayed(Integer.parseInt(split[1]))
                                .wins(Integer.parseInt(split[2]))
                                .losses(Integer.parseInt(split[3]))
                                .ties(Integer.parseInt(split[4]))
                                .points(Integer.parseInt(split[5]))
                                .goalsFor(Integer.parseInt(split[6]))
                                .goalsAgainst(Integer.parseInt(split[7]))
                                .penaltyMinutes(Integer.parseInt(split[8]))
                                .build();
                        staticPlayoffStats.add(statsDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<TeamStatsDto> getTeamStandings(SeasonType seasonType) {
        log.info("Getting stats for season type: {}", seasonType);
        if(SeasonType.PLAYOFFS.equals(seasonType)) {
            return staticPlayoffStats;
        }
        return staticSeasonStats;
    }
}
