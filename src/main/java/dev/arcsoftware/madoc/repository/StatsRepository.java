package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.entity.AssistEntity;
import dev.arcsoftware.madoc.model.entity.GoalEntity;
import dev.arcsoftware.madoc.model.entity.PenaltyEntity;
import dev.arcsoftware.madoc.model.payload.StatsDto;
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
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class StatsRepository {
    private final JdbcClient jdbcClient;

    private static List<StatsDto> staticSkaterStats2024;
    private static List<StatsDto> staticSkaterPlayoffStats2024;
    private static List<StatsDto> staticGoalieStats2024;

    @Autowired
    public StatsRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

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
                                .playerName(Utils.toCamelCase(split[1]) + " (#" + split[0] + ")")
                                .teamName(Utils.toCamelCase(split[2]))
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
                                .playerName(Utils.toCamelCase(split[1]) + " (#" + split[0] + ")")
                                .teamName(Utils.toCamelCase(split[2]))
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
                                .playerName(Utils.toCamelCase(split[0]))
                                .teamName(Utils.toCamelCase(split[1]))
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
        return jdbcClient
                .sql(StatsSql.GET_PLAYER_SEASON_TYPE_STATS_BY_YEAR)
                .param("year", year)
                .param("season_type", seasonType.name())
                .query((rs, num) -> StatsDto.builder()
                        .playerName(rs.getString("player_name"))
                        .teamName(rs.getString("team_name"))
                        .gamesPlayed(rs.getInt("games_played"))
                        .goals(rs.getInt("goals"))
                        .assists(rs.getInt("assists"))
                        .points(rs.getInt("points"))
                        .penaltyMinutes(rs.getInt("penalty_minutes"))
                        .build())
                .list();
    }

    public List<StatsDto> getGoalieStats(int year, SeasonType seasonType) {
        log.info("Getting goalie stats for year {}, season type: {}", year, seasonType);
        if(year == 2024) {
            if(SeasonType.PLAYOFFS.equals(seasonType)) {
                return Collections.emptyList();
            }
            return staticGoalieStats2024;
        }
        return jdbcClient
                .sql(StatsSql.GET_GOALIE_SEASON_TYPE_STATS_BY_YEAR)
                .param("year", year)
                .param("season_type", seasonType.name())
                .query((rs, num) -> StatsDto.builder()
                        .playerName(rs.getString("player_name"))
                        .teamName(rs.getString("team_name"))
                        .gamesPlayed(rs.getInt("games_played"))
                        .wins(rs.getInt("wins"))
                        .losses(rs.getInt("losses"))
                        .ties(rs.getInt("ties"))
                        .shutouts(rs.getInt("shutouts"))
                        .penaltyMinutes(rs.getInt("penalty_minutes"))
                        .goalsAgainst(rs.getInt("goals_against"))
                        .build())
                .list();
    }

    public void insertGoalAndAssists(GoalEntity goal) {
        int id = jdbcClient
                .sql(StatsSql.INSERT_GOAL)
                .params(goal.toParameterMap())
                .query(Integer.class)
                .single();
        goal.setId(id);
    }

    public void insertPenalty(PenaltyEntity penalty) {
        int id = jdbcClient
                .sql(StatsSql.INSERT_PENALTY)
                .params(penalty.toParameterMap())
                .query(Integer.class)
                .single();
        penalty.setId(id);
    }

    public void clearStatsByGameId(int gameId){
        int goals = jdbcClient
                .sql(StatsSql.CLEAR_GOALS_BY_GAME_ID)
                .param("game_id", gameId)
                .update();
        int penalties = jdbcClient
                .sql(StatsSql.CLEAR_PENALTIES_BY_GAME_ID)
                .param("game_id", gameId)
                .update();

        log.info("Deleted {} goals, {} penalties", goals, penalties);
    }

    public static class StatsSql{
        public static final String INSERT_GOAL = """
        INSERT INTO madoc.goals (game_id, goal_type, scorer_roster_assignment_id, primary_assist_roster_assignment_id, secondary_assist_roster_assignment_id, period, time)
        VALUES (:game_id, :goal_type, :scorer_id, :primary_assist_id, :secondary_assist_id, :period, :time)
        RETURNING id;
        """;

        public static final String INSERT_PENALTY = """
        INSERT INTO madoc.penalties (game_id, roster_assignment_id, infraction, minutes, period, time)
        VALUES (:game_id, :player_id, :infraction, :minutes, :period, :time)
        RETURNING id;
        """;

        public static final String GET_PLAYER_SEASON_TYPE_STATS_BY_YEAR = """
        WITH attendance AS (
            SELECT ra.id, count(*) AS games_played
            FROM madoc.attendance at
                JOIN madoc.roster_assignments ra ON at.roster_assignment_id = ra.id
                JOIN madoc.games gm ON at.game_id = gm.id
            WHERE gm.year = :year
                AND gm.season_type = :season_type
            GROUP BY ra.id, at.attended
            HAVING at.attended = true
        ),
        goals AS (
            SELECT ra.id, count(*) AS goals_scored
            FROM madoc.goals go
                JOIN madoc.roster_assignments ra ON go.scorer_roster_assignment_id = ra.id
                JOIN madoc.games gm ON go.game_id = gm.id
            WHERE gm.year = :year
                AND gm.season_type = :season_type
            GROUP BY ra.id
        ),
        assists AS (
            SELECT ra.id, count(*) AS total_assists
            FROM madoc.goals go
                JOIN madoc.roster_assignments ra ON go.primary_assist_roster_assignment_id = ra.id OR go.secondary_assist_roster_assignment_id = ra.id
                JOIN madoc.games gm ON go.game_id = gm.id
            WHERE gm.year = :year
                AND gm.season_type = :season_type
            GROUP BY ra.id
        ),
        penalty_minutes AS (
            SELECT roster_assignment_id, SUM(minutes) AS total_penalty_minutes
            FROM madoc.penalties p
            JOIN madoc.games gm ON p.game_id = gm.id
            WHERE gm.year = :year
                AND gm.season_type = :season_type
            GROUP BY roster_assignment_id
        )
        SELECT p.id AS player_id,
               ra.jersey_number AS jersey_number,
               CONCAT(p.first_name, ' ', p.last_name) AS player_name,
               t.team_name AS team_name,
               COALESCE(attendance.games_played, 0) AS games_played,
               COALESCE(goals.goals_scored, 0) AS goals,
               COALESCE(assists.total_assists, 0) AS assists,
               COALESCE(goals.goals_scored, 0) + COALESCE(assists.total_assists, 0) AS points,
               COALESCE(penalty_minutes.total_penalty_minutes, 0) AS penalty_minutes
        FROM madoc.roster_assignments ra
                 LEFT JOIN madoc.players p ON ra.player_id = p.id
                 LEFT JOIN madoc.teams t ON ra.team_id = t.id
                 LEFT JOIN attendance ON ra.id = attendance.id
                 LEFT JOIN goals ON ra.id = goals.id
                 LEFT JOIN assists ON ra.id = assists.id
                 LEFT JOIN penalty_minutes ON ra.id = penalty_minutes.roster_assignment_id;
        """;

        public static final String CLEAR_GOALS_BY_GAME_ID = """
        DELETE FROM madoc.goals
        WHERE game_id = :game_id
        """;
        public static final String CLEAR_PENALTIES_BY_GAME_ID = """
        DELETE FROM madoc.penalties
        WHERE game_id = :game_id
        """;

        public static final String GET_GOALIE_SEASON_TYPE_STATS_BY_YEAR = """
        WITH attendance AS (
            SELECT ra.id, count(*) AS games_played
            FROM madoc.attendance at
                JOIN madoc.roster_assignments ra ON at.roster_assignment_id = ra.id
                JOIN madoc.games gm ON at.game_id = gm.id
            WHERE gm.year = :year
                AND gm.season_type = :season_type
            GROUP BY ra.id, at.attended
            HAVING at.attended = true
        ),
        penalty_minutes AS (
             SELECT roster_assignment_id, SUM(minutes) AS total_penalty_minutes
             FROM madoc.penalties
             GROUP BY roster_assignment_id
        ),
        game_scores AS (
            SELECT
                gm.id AS game_id,
                gm.home_team,
                gm.away_team,
                SUM(CASE WHEN ra.team_id = gm.home_team THEN 1 ELSE 0 END) AS home_score,
                SUM(CASE WHEN ra.team_id = gm.away_team THEN 1 ELSE 0 END) AS away_score
            FROM madoc.games gm
                     LEFT JOIN madoc.goals g ON g.game_id = gm.id
                     LEFT JOIN madoc.roster_assignments ra ON g.scorer_roster_assignment_id = ra.id AND ra.season_year = :year
            WHERE gm.is_finalized = true
              AND gm.season_type = :season_type
              AND gm.year = :year
            GROUP BY gm.id, gm.home_team, gm.away_team
        ),
        team_standings AS (
            SELECT
                t.id as team_id,
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
                SUM(CASE WHEN t.id = gs.home_team THEN gs.away_score
                         WHEN t.id = gs.away_team THEN gs.home_score
                         ELSE 0 END) AS goals_against
            FROM madoc.teams t
                     LEFT JOIN game_scores gs ON t.id = gs.home_team OR t.id = gs.away_team
            GROUP BY t.id, t.team_name
        )
        SELECT p.id AS player_id,
               ra.jersey_number AS jersey_number,
               CONCAT(p.first_name, ' ', p.last_name) AS player_name,
               t.team_name AS team_name,
               COALESCE(attendance.games_played, 0) AS games_played,
               ts.wins AS wins,
               ts.losses  AS losses,
               ts.ties  AS ties,
               ts.shutouts  AS shutouts,
               COALESCE(penalty_minutes.total_penalty_minutes, 0) AS penalty_minutes,
               ts.goals_against  AS goals_against
        FROM madoc.roster_assignments ra
                 LEFT JOIN madoc.players p ON ra.player_id = p.id
                 LEFT JOIN madoc.teams t ON ra.team_id = t.id
                 LEFT JOIN attendance ON ra.id = attendance.id
                 LEFT JOIN penalty_minutes ON ra.id = penalty_minutes.roster_assignment_id
                 LEFT JOIN team_standings ts ON ra.team_id = ts.team_id
        WHERE ra.position = 'GOALIE';
        """;
    }
}
