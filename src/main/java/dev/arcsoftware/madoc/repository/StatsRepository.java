package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.Position;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.entity.AssistEntity;
import dev.arcsoftware.madoc.model.entity.GoalEntity;
import dev.arcsoftware.madoc.model.entity.PenaltyEntity;
import dev.arcsoftware.madoc.model.payload.PlayerGameLogDto;
import dev.arcsoftware.madoc.model.payload.PlayerGameLogPage;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
                        .playerId(rs.getObject("player_id", Integer.class))
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
                        .playerId(rs.getObject("player_id", Integer.class))
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

    public PlayerGameLogPage getPlayerGameLogs(int playerId, int offset, int limit) {
        log.info("Getting game logs for playerId={}, offset={}, limit={}", playerId, offset, limit);
        AtomicInteger totalCount = new AtomicInteger(0);
        List<PlayerGameLogDto> games = jdbcClient
                .sql(StatsSql.GET_PLAYER_GAME_LOGS)
                .param("player_id", playerId)
                .param("offset", offset)
                .param("limit", limit)
                .query((rs, num) -> {
                    totalCount.set(rs.getInt("total_count"));
                    Timestamp ts = rs.getTimestamp("game_time");
                    LocalDateTime gameTime = ts != null ? ts.toLocalDateTime() : null;

                    SeasonType seasonType = SeasonType.REGULAR_SEASON;
                    String stRaw = rs.getString("season_type");
                    if (stRaw != null) {
                        try {
                            seasonType = SeasonType.valueOf(stRaw);
                        } catch (IllegalArgumentException ignored) {
                            // Keep default
                        }
                    }

                    Position position = null;
                    String posRaw = rs.getString("position");
                    if (posRaw != null) {
                        try {
                            position = Position.valueOf(posRaw);
                        } catch (IllegalArgumentException ignored) {
                            // Leave null
                        }
                    }

                    int teamScore = rs.getInt("team_score");
                    int opponentScore = rs.getInt("opponent_score");
                    int goals = rs.getInt("goals");
                    int assists = rs.getInt("assists");
                    String result = teamScore > opponentScore ? "W" : (teamScore < opponentScore ? "L" : "T");

                    return PlayerGameLogDto.builder()
                            .gameId(rs.getInt("game_id"))
                            .gameTime(gameTime)
                            .seasonYear(rs.getInt("season_year"))
                            .seasonType(seasonType)
                            .home(rs.getBoolean("is_home"))
                            .position(position)
                            .teamName(rs.getString("team_name"))
                            .opponentTeamName(rs.getString("opponent_team_name"))
                            .teamScore(teamScore)
                            .opponentScore(opponentScore)
                            .goals(goals)
                            .assists(assists)
                            .points(goals + assists)
                            .penaltyMinutes(rs.getInt("penalty_minutes"))
                            .result(result)
                            .shutout(opponentScore == 0)
                            .build();
                })
                .list();

        return new PlayerGameLogPage(games, totalCount.get(), offset, limit);
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
                 LEFT JOIN penalty_minutes ON ra.id = penalty_minutes.roster_assignment_id
       WHERE ra.season_year = :year;
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
            SELECT gm.id as game_id, ra.id as goalie_id, ra.team_id as team_id
            FROM madoc.attendance at
                     JOIN madoc.roster_assignments ra ON at.roster_assignment_id = ra.id
                     JOIN madoc.games gm ON at.game_id = gm.id
            WHERE gm.year = :year
              AND gm.season_type = :season_type
              AND ra.position = 'GOALIE'
              AND at.attended = true
        ),
        penalty_minutes AS (
            SELECT roster_assignment_id, SUM(minutes) AS total_penalty_minutes
            FROM madoc.penalties
            GROUP BY roster_assignment_id
        ),
        game_results AS (
            SELECT
                gm.id AS game_id,
                gm.home_team,
                gm.away_team,
                SUM(CASE WHEN ra.team_id = gm.home_team THEN 1 ELSE 0 END) AS away_goalie_goals_against,
                SUM(CASE WHEN ra.team_id = gm.away_team THEN 1 ELSE 0 END) AS home_goalie_goals_against,
                at.goalie_id as home_goalie_id,
                at2.goalie_id as away_goalie_id,
                CASE
                    WHEN SUM(CASE WHEN ra.team_id = gm.home_team THEN 1 ELSE 0 END) >
                        SUM(CASE WHEN ra.team_id = gm.away_team THEN 1 ELSE 0 END)
                    THEN 'W'
                    WHEN SUM(CASE WHEN ra.team_id = gm.home_team THEN 1 ELSE 0 END) <
                        SUM(CASE WHEN ra.team_id = gm.away_team THEN 1 ELSE 0 END)
                    THEN 'L'
                    ELSE 'T'
                END as home_goalie_result,
                CASE
                    WHEN SUM(CASE WHEN ra.team_id = gm.home_team THEN 1 ELSE 0 END) <
                        SUM(CASE WHEN ra.team_id = gm.away_team THEN 1 ELSE 0 END)
                    THEN 'W'
                    WHEN SUM(CASE WHEN ra.team_id = gm.home_team THEN 1 ELSE 0 END) >
                        SUM(CASE WHEN ra.team_id = gm.away_team THEN 1 ELSE 0 END)
                    THEN 'L'
                    ELSE 'T'
                END as away_goalie_result
            FROM madoc.games gm
                LEFT JOIN madoc.goals g ON g.game_id = gm.id
                LEFT JOIN madoc.roster_assignments ra ON g.scorer_roster_assignment_id = ra.id AND ra.season_year = :year
                LEFT JOIN attendance at ON gm.id = at.game_id AND gm.home_team = at.team_id
                LEFT JOIN attendance at2 ON gm.id = at2.game_id AND gm.away_team = at2.team_id
            WHERE gm.is_finalized = true
                AND gm.season_type = :season_type
                AND gm.year = :year
            GROUP BY gm.id, gm.home_team, gm.away_team, at.goalie_id, at2.goalie_id
        ),
        game_stats AS (
            SELECT
                goalie_id,
                SUM(goals_against) AS total_goals_against,
                SUM(CASE WHEN result = 'W' THEN 1 ELSE 0 END) AS wins,
                SUM(CASE WHEN result = 'L' THEN 1 ELSE 0 END) AS losses,
                SUM(CASE WHEN result = 'T' THEN 1 ELSE 0 END) AS ties,
                SUM(CASE WHEN goals_against = 0 THEN 1 ELSE 0 END) AS shutouts
            FROM (
                     SELECT home_goalie_id AS goalie_id,
                            home_goalie_goals_against AS goals_against,
                            home_goalie_result AS result
                     FROM game_results
                     UNION ALL
                     SELECT away_goalie_id AS goalie_id,
                            away_goalie_goals_against AS goals_against,
                            away_goalie_result AS result
                     FROM game_results
                 ) AS all_goalies
            WHERE goalie_id IS NOT NULL
            GROUP BY goalie_id
        ),
        games_played AS (
            SELECT goalie_id, COUNT(*) AS games_played
            FROM attendance
            GROUP BY goalie_id
        )
        SELECT p.id AS player_id,
               ra.jersey_number AS jersey_number,
               CONCAT(p.first_name, ' ', p.last_name) AS player_name,
               t.team_name AS team_name,
               COALESCE(games_played.games_played, 0) AS games_played,
               gs.wins AS wins,
               gs.losses  AS losses,
               gs.ties  AS ties,
               gs.shutouts  AS shutouts,
               COALESCE(penalty_minutes.total_penalty_minutes, 0) AS penalty_minutes,
                gs.total_goals_against  AS goals_against
        FROM madoc.roster_assignments ra
                 LEFT JOIN madoc.players p ON ra.player_id = p.id
                 LEFT JOIN madoc.teams t ON ra.team_id = t.id
                 LEFT JOIN attendance ON ra.id = attendance.goalie_id
                 LEFT JOIN penalty_minutes ON ra.id = penalty_minutes.roster_assignment_id
                 LEFT JOIN game_stats gs ON ra.id = gs.goalie_id
                 LEFT JOIN games_played ON ra.id = games_played.goalie_id
        WHERE ra.season_year = :year
        GROUP BY p.id, ra.jersey_number, p.first_name, p.last_name, t.team_name, penalty_minutes.total_penalty_minutes, ra.position, gs.wins, gs.losses, gs.ties, gs.shutouts, gs.total_goals_against, games_played.games_played
        HAVING ra.position = 'GOALIE';
        
        """;

        public static final String GET_PLAYER_GAME_LOGS = """
        WITH player_ras AS (
            SELECT ra.id AS ra_id, ra.team_id, ra.season_year, ra.position
            FROM madoc.roster_assignments ra
            WHERE ra.player_id = :player_id
        ),
        player_game_links AS (
            SELECT DISTINCT gm.id AS game_id, pra.ra_id, pra.team_id, pra.position
            FROM player_ras pra
            JOIN madoc.games gm ON gm.year = pra.season_year
                AND (gm.home_team = pra.team_id OR gm.away_team = pra.team_id)
            WHERE gm.is_finalized = true
              AND (
                  EXISTS (
                      SELECT 1 FROM madoc.attendance at
                      WHERE at.game_id = gm.id
                        AND at.roster_assignment_id = pra.ra_id
                        AND at.attended = true
                  )
                  OR EXISTS (
                      SELECT 1 FROM madoc.goals go
                      WHERE go.game_id = gm.id
                        AND (go.scorer_roster_assignment_id = pra.ra_id
                             OR go.primary_assist_roster_assignment_id = pra.ra_id
                             OR go.secondary_assist_roster_assignment_id = pra.ra_id)
                  )
                  OR EXISTS (
                      SELECT 1 FROM madoc.penalties pen
                      WHERE pen.game_id = gm.id
                        AND pen.roster_assignment_id = pra.ra_id
                  )
              )
        ),
        game_scores AS (
            SELECT
                pgl.game_id,
                COALESCE(SUM(CASE WHEN ra_s.team_id = gm.home_team THEN 1 ELSE 0 END), 0) AS home_score,
                COALESCE(SUM(CASE WHEN ra_s.team_id = gm.away_team THEN 1 ELSE 0 END), 0) AS away_score
            FROM (SELECT DISTINCT game_id FROM player_game_links) pgl
            JOIN madoc.games gm ON gm.id = pgl.game_id
            LEFT JOIN madoc.goals gl ON gl.game_id = gm.id
            LEFT JOIN madoc.roster_assignments ra_s ON gl.scorer_roster_assignment_id = ra_s.id
            GROUP BY pgl.game_id
        ),
        player_goals AS (
            SELECT
                go.game_id,
                pgl.ra_id,
                SUM(CASE WHEN go.scorer_roster_assignment_id = pgl.ra_id THEN 1 ELSE 0 END) AS goals,
                SUM(CASE WHEN go.primary_assist_roster_assignment_id = pgl.ra_id THEN 1 ELSE 0 END)
                  + SUM(CASE WHEN go.secondary_assist_roster_assignment_id = pgl.ra_id THEN 1 ELSE 0 END) AS assists
            FROM player_game_links pgl
            JOIN madoc.goals go ON go.game_id = pgl.game_id
            WHERE go.scorer_roster_assignment_id = pgl.ra_id
               OR go.primary_assist_roster_assignment_id = pgl.ra_id
               OR go.secondary_assist_roster_assignment_id = pgl.ra_id
            GROUP BY go.game_id, pgl.ra_id
        ),
        player_penalties AS (
            SELECT
                pen.game_id,
                pgl.ra_id,
                COALESCE(SUM(pen.minutes), 0) AS penalty_minutes
            FROM player_game_links pgl
            JOIN madoc.penalties pen ON pen.game_id = pgl.game_id AND pen.roster_assignment_id = pgl.ra_id
            GROUP BY pen.game_id, pgl.ra_id
        )
        SELECT
            gm.id AS game_id,
            gm.game_time AS game_time,
            gm.year AS season_year,
            gm.season_type AS season_type,
            (pgl.team_id = gm.home_team) AS is_home,
            pgl.position AS position,
            pt.team_name AS team_name,
            opp.team_name AS opponent_team_name,
            CASE WHEN pgl.team_id = gm.home_team THEN gs.home_score ELSE gs.away_score END AS team_score,
            CASE WHEN pgl.team_id = gm.home_team THEN gs.away_score ELSE gs.home_score END AS opponent_score,
            COALESCE(pg.goals, 0) AS goals,
            COALESCE(pg.assists, 0) AS assists,
            COALESCE(pp.penalty_minutes, 0) AS penalty_minutes,
            COUNT(*) OVER() AS total_count
        FROM player_game_links pgl
        JOIN madoc.games gm ON gm.id = pgl.game_id
        JOIN madoc.teams pt ON pt.id = pgl.team_id
        JOIN madoc.teams opp ON opp.id = CASE WHEN pgl.team_id = gm.home_team THEN gm.away_team ELSE gm.home_team END
        LEFT JOIN game_scores gs ON gs.game_id = gm.id
        LEFT JOIN player_goals pg ON pg.game_id = gm.id AND pg.ra_id = pgl.ra_id
        LEFT JOIN player_penalties pp ON pp.game_id = gm.id AND pp.ra_id = pgl.ra_id
        ORDER BY gm.game_time DESC, gm.id DESC
        LIMIT :limit OFFSET :offset;
        """;
    }
}
