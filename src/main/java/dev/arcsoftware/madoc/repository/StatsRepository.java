package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class StatsRepository {
    private static List<StatsDto> staticSkaterStats2024;
    private static List<StatsDto> staticSkaterPlayoffStats2024;
    private static List<StatsDto> staticGoalieStats2024;

    @PostConstruct
    public void loadData(){
        staticSkaterStats2024 = new ArrayList<>();
        ClassPathResource statsResource = new ClassPathResource("data/stats/2024_SEASON.csv");
        try(BufferedReader seasonStats = new BufferedReader(new BufferedReader(new InputStreamReader(statsResource.getInputStream())))) {
            seasonStats.lines()
                    .skip(1) // Skip header line
                    .forEach(line -> {
                        //# ,Player,Team,G,A,PTS,PIM
                        String[] split = line.split(",");
                        StatsDto statsDto = StatsDto.builder()
                                .playerName(split[1] + " (#" + split[0] + ")")
                                .teamName(split[2])
                                .gamesPlayed(20) // Assuming 20 games for simplicity
                                .goals(Integer.parseInt(split[3]))
                                .assists(Integer.parseInt(split[4]))
                                .points(Integer.parseInt(split[5]))
                                .penaltyMinutes(Integer.parseInt(split[6]))
                                .build();
                        staticSkaterStats2024.add(statsDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        staticSkaterPlayoffStats2024 = new ArrayList<>();
        ClassPathResource playoffStatsResource = new ClassPathResource("data/stats/2024_PLAYOFFS.csv");
        try(BufferedReader playoffStats = new BufferedReader(new BufferedReader(new InputStreamReader(playoffStatsResource.getInputStream())))) {
            playoffStats.lines()
                    .skip(1) // Skip header line
                    .forEach(line -> {
                        //# ,Player,Team,G,A,PTS,PIM
                        String[] split = line.split(",");
                        StatsDto statsDto = StatsDto.builder()
                                .playerName(split[1] + " (#" + split[0] + ")")
                                .teamName(split[2])
                                .gamesPlayed(5) // Assuming 5 games for simplicity
                                .goals(Integer.parseInt(split[3]))
                                .assists(Integer.parseInt(split[4]))
                                .points(Integer.parseInt(split[5]))
                                .penaltyMinutes(Integer.parseInt(split[6]))
                                .build();
                        staticSkaterPlayoffStats2024.add(statsDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        staticGoalieStats2024 = new ArrayList<>();
        ClassPathResource goalieStatsResource = new ClassPathResource("data/stats/2024_SEASON_GOALIES.csv");
        try(BufferedReader goalieStats = new BufferedReader(new BufferedReader(new InputStreamReader(goalieStatsResource.getInputStream())))) {
            goalieStats.lines()
                    .skip(1) // Skip header line
                    .forEach(line -> {
                        //PLAYER,Team,GP,W,L,T,SO,ENG,PIM,GA,GAA
                        String[] split = line.split(",");
                        StatsDto statsDto = StatsDto.builder()
                                .playerName(split[0])
                                .teamName(split[1])
                                .gamesPlayed(Integer.parseInt(split[2]))
                                .wins(Integer.parseInt(split[3]))
                                .losses(Integer.parseInt(split[4]))
                                .ties(Integer.parseInt(split[5]))
                                .shutouts(Integer.parseInt(split[6]))
                                .penaltyMinutes(Integer.parseInt(split[8]))
                                .goalsAgainst(Integer.parseInt(split[9]))
                                .build();
                        staticGoalieStats2024.add(statsDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<StatsDto> getSkaterStats(int year, SeasonType seasonType) {
        log.info("Getting skater stats for year {}, season type: {}", year, seasonType);
        if(year == 2024) {
            if(SeasonType.PLAYOFFS.equals(seasonType)) {
                return staticSkaterPlayoffStats2024;
            }
            return staticSkaterStats2024;
        }
        return Collections.emptyList();
    }

    public List<StatsDto> getGoalieStats(int year, SeasonType seasonType) {
        log.info("Getting goalie stats for year {}, season type: {}", year, seasonType);
        if(year == 2024) {
            if(SeasonType.PLAYOFFS.equals(seasonType)) {
                return Collections.emptyList();
            }
            return staticGoalieStats2024;
        }
        return Collections.emptyList();
    }
}
