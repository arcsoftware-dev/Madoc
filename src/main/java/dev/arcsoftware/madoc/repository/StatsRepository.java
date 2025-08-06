package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.StatsDto;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class StatsRepository {
    private static List<StatsDto> staticSkaterStats;
    private static List<StatsDto> staticSkaterPlayoffStats;
    private static List<StatsDto> staticGoalieStats;

    @PostConstruct
    public void loadData(){
        staticSkaterStats = new ArrayList<>();
        ClassPathResource statsResource = new ClassPathResource("stats/2024_SEASON.csv");
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
                        staticSkaterStats.add(statsDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        staticSkaterPlayoffStats = new ArrayList<>();
        ClassPathResource playoffStatsResource = new ClassPathResource("stats/2024_PLAYOFFS.csv");
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
                        staticSkaterPlayoffStats.add(statsDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        staticGoalieStats = new ArrayList<>();
        ClassPathResource goalieStatsResource = new ClassPathResource("stats/2024_SEASON_GOALIES.csv");
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
                        staticGoalieStats.add(statsDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<StatsDto> getSkaterStats(SeasonType seasonType) {
        if(SeasonType.PLAYOFFS.equals(seasonType)) {
            return staticSkaterPlayoffStats;
        }
        return staticSkaterStats;
    }

    public List<StatsDto> getGoalieStats(SeasonType seasonType) {
        if(SeasonType.PLAYOFFS.equals(seasonType)) {
            return Collections.emptyList();
        }
        return staticGoalieStats;
    }
}
