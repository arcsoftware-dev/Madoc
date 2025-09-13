package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.payload.TeamStatsDto;
import dev.arcsoftware.madoc.util.Utils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class StandingsRepository {
    private static List<TeamStatsDto> staticSeasonStats2024;
    private static List<TeamStatsDto> staticPlayoffStats2024;

    private final JdbcClient jdbcClient;

    @Autowired
    public StandingsRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }


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
        return jdbcClient
                .sql(StandingsSql.GET_STANDINGS_FOR_YEAR_AND_SEASON_TYPE)
                .param("year", year)
                .param("season_type", seasonType.name())
                .query((rs,num) -> TeamStatsDto.builder()
                        .teamName(Utils.toCamelCase(rs.getString("team_name")))
                        .gamesPlayed(rs.getInt("games_played"))
                        .wins(rs.getInt("wins"))
                        .losses(rs.getInt("losses"))
                        .ties(rs.getInt("ties"))
                        .points(rs.getInt("points"))
                        .goalsFor(rs.getInt("goals_for"))
                        .goalsAgainst(rs.getInt("goals_against"))
                        .shutouts(rs.getInt("shutouts"))
                        .penaltyMinutes(rs.getInt("penalty_minutes"))
                        .build())
                .list();
    }

    public static class StandingsSql{
        public static final String GET_STANDINGS_FOR_YEAR_AND_SEASON_TYPE = """
        
                WITH game_scores AS (
            SELECT
                gm.id AS game_id,
                gm.home_team,
                gm.away_team,
                SUM(CASE WHEN ra.team_id = gm.home_team THEN 1 ELSE 0 END) AS home_score,
                SUM(CASE WHEN ra.team_id = gm.away_team THEN 1 ELSE 0 END) AS away_score
            FROM madoc.games gm
                     LEFT JOIN madoc.goals g ON g.game_id = gm.id
                     LEFT JOIN madoc.players pl ON g.player_id = pl.id
                     LEFT JOIN madoc.roster_assignments ra ON pl.id = ra.player_id AND ra.season_year = :year
            WHERE gm.is_finalized = true
              AND gm.season_type = :season_type
              AND ra.season_year = :year
            GROUP BY gm.id, gm.home_team, gm.away_team
        ),
             team_penalties AS (
                 SELECT
                     ra.team_id,
                     SUM(p.minutes) AS total_penalty_minutes
                 FROM madoc.penalties p
                          INNER JOIN madoc.players pl ON p.player_id = pl.id
                          INNER JOIN madoc.roster_assignments ra ON pl.id = ra.player_id AND ra.season_year = :year
                          INNER JOIN madoc.games gm ON p.game_id = gm.id
                 WHERE gm.is_finalized = true
                   AND gm.season_type = :season_type
                   AND ra.season_year = :year
                 GROUP BY ra.team_id
             )
        SELECT
            t.team_name,
            COUNT(DISTINCT gs.game_id) AS games_played,
            SUM(CASE
                    WHEN (t.id = gs.home_team AND gs.home_score > gs.away_score)
                        OR (t.id = gs.away_team AND gs.away_score > gs.home_score)
                        THEN 1 ELSE 0 END) AS wins,
            SUM(CASE
                    WHEN (t.id = gs.home_team AND gs.home_score < gs.away_score)
                        OR (t.id = gs.away_team AND gs.away_score < gs.home_score)
                        THEN 1 ELSE 0 END) AS losses,
            SUM(CASE WHEN gs.home_score = gs.away_score THEN 1 ELSE 0 END) AS ties,
            SUM(CASE
                    WHEN (t.id = gs.home_team AND gs.away_score = 0)
                        OR (t.id = gs.away_team AND gs.home_score = 0)
                        THEN 1 ELSE 0 END) AS shutouts,
            SUM(CASE WHEN t.id = gs.home_team THEN gs.home_score
                     WHEN t.id = gs.away_team THEN gs.away_score
                     ELSE 0 END) AS goals_for,
            SUM(CASE WHEN t.id = gs.home_team THEN gs.away_score
                     WHEN t.id = gs.away_team THEN gs.home_score
                     ELSE 0 END) AS goals_against,
            (2 * SUM(CASE
                         WHEN (t.id = gs.home_team AND gs.home_score > gs.away_score)
                             OR (t.id = gs.away_team AND gs.away_score > gs.home_score)
                             THEN 1 ELSE 0 END)
                + SUM(CASE WHEN gs.home_score = gs.away_score THEN 1 ELSE 0 END)
                ) AS points,
            COALESCE(tp.total_penalty_minutes, 0) AS penalty_minutes
        FROM madoc.teams t
                 LEFT JOIN game_scores gs ON t.id = gs.home_team OR t.id = gs.away_team
                 LEFT JOIN team_penalties tp ON t.id = tp.team_id
        GROUP BY t.id, t.team_name, tp.total_penalty_minutes
        ORDER BY points DESC, games_played ASC;
        """;
    }
}
