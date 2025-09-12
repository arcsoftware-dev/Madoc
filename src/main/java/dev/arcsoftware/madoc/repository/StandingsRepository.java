package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.util.Utils;
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
public class StandingsRepository {
    private static List<TeamStatsDto> staticSeasonStats2024;
    private static List<TeamStatsDto> staticPlayoffStats2024;

    @PostConstruct
    public void loadData(){
        staticSeasonStats2024 = new ArrayList<>();
        ClassPathResource statsResource = new ClassPathResource("data/standings/2024_SEASON.csv");
        try(BufferedReader seasonStats = new BufferedReader(new BufferedReader(new InputStreamReader(statsResource.getInputStream())))) {
            seasonStats.lines()
                    .skip(1) // Skip header line
                    .forEach(line -> {
                        //Team, GP, W, L, T, P, GF, GA, PIM
                        String[] split = line.split(",");
                        TeamStatsDto statsDto = TeamStatsDto.builder()
                                .teamName(Utils.toCamelCase(split[0]))
                                .gamesPlayed(Integer.parseInt(split[1]))
                                .wins(Integer.parseInt(split[2]))
                                .losses(Integer.parseInt(split[3]))
                                .ties(Integer.parseInt(split[4]))
                                .points(Integer.parseInt(split[5]))
                                .goalsFor(Integer.parseInt(split[6]))
                                .goalsAgainst(Integer.parseInt(split[7]))
                                .penaltyMinutes(Integer.parseInt(split[8]))
                                .build();
                        staticSeasonStats2024.add(statsDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        staticPlayoffStats2024 = new ArrayList<>();
        ClassPathResource playoffStatsResource = new ClassPathResource("data/standings/2024_PLAYOFFS.csv");
        try(BufferedReader playoffStats = new BufferedReader(new BufferedReader(new InputStreamReader(playoffStatsResource.getInputStream())))) {
            playoffStats.lines()
                    .skip(1) // Skip header line
                    .forEach(line -> {
                        //Team, GP, W, L, T, P, GF, GA, PIM
                        String[] split = line.split(",");
                        TeamStatsDto statsDto = TeamStatsDto.builder()
                                .teamName(Utils.toCamelCase(split[0]))
                                .gamesPlayed(Integer.parseInt(split[1]))
                                .wins(Integer.parseInt(split[2]))
                                .losses(Integer.parseInt(split[3]))
                                .ties(Integer.parseInt(split[4]))
                                .points(Integer.parseInt(split[5]))
                                .goalsFor(Integer.parseInt(split[6]))
                                .goalsAgainst(Integer.parseInt(split[7]))
                                .penaltyMinutes(Integer.parseInt(split[8]))
                                .build();
                        staticPlayoffStats2024.add(statsDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<TeamStatsDto> getTeamStandings(int year, SeasonType seasonType) {
        log.info("Getting standings for year {}, season type: {}", year, seasonType);
        if(year == 2024){
            if(SeasonType.PLAYOFFS.equals(seasonType)) {
                return staticPlayoffStats2024;
            }
            return staticSeasonStats2024;
        }
        return Collections.emptyList();
    }
}
