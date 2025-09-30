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
        return Collections.emptyList();
    }

    public void insertGoalAndAssists(GoalEntity goal) {
        Integer primaryAssistId = null;
        if(goal.getPrimaryAssistPlayer() != null) {
            primaryAssistId = insertAssist(new AssistEntity(goal.getPrimaryAssistPlayer(), true));
        }
        Integer secondaryAssistId = null;
        if(goal.getSecondaryAssistPlayer() != null) {
            secondaryAssistId = insertAssist(new AssistEntity(goal.getSecondaryAssistPlayer(), false));
        }

        int id = jdbcClient
                .sql(StatsSql.INSERT_GOAL)
                .params(goal.toParameterMap())
                .param("primary_assist_id", primaryAssistId)
                .param("secondary_assist_id", secondaryAssistId)
                .query(Integer.class)
                .single();
        goal.setId(id);
    }

    public int insertAssist(AssistEntity assist) {
        int id = jdbcClient
                .sql(StatsSql.INSERT_ASSIST)
                .params(assist.toParameterMap())
                .query(Integer.class)
                .single();
        assist.setId(id);
        return id;
    }

    public void insertPenalty(PenaltyEntity penalty) {
        int id = jdbcClient
                .sql(StatsSql.INSERT_PENALTY)
                .params(penalty.toParameterMap())
                .query(Integer.class)
                .single();
        penalty.setId(id);
    }

    public static class StatsSql{
        public static final String INSERT_GOAL = """
        INSERT INTO madoc.goals (game_id, goal_type, player_id, primary_assist_id, secondary_assist_id, period, time)
        VALUES (:game_id, :goal_type, :player_id, :primary_assist_id, :secondary_assist_id, :period, :time)
        RETURNING id;
        """;

        public static final String INSERT_ASSIST = """
        INSERT INTO madoc.assists (player_id, is_primary)
        VALUES (:player_id, :is_primary)
        RETURNING id;
        """;

        public static final String INSERT_PENALTY = """
        INSERT INTO madoc.penalties (game_id, player_id, infraction, minutes, period, time)
        VALUES (:game_id, :player_id, :infraction, :minutes, :period, :time)
        RETURNING id;
        """;

        public static final String GET_PLAYER_SEASON_TYPE_STATS_BY_YEAR = """
        SELECT
            p.id AS player_id,
            ra.jersey_number AS jersey_number,
            CONCAT(p.first_name, ' ', p.last_name) AS player_name,
            t.team_name AS team_name,
            COUNT(DISTINCT a.id) AS games_played,
            COUNT(DISTINCT go.id) AS goals,
            COUNT(DISTINCT ass.id) AS assists,
            (COUNT(DISTINCT go.id) + COUNT(DISTINCT ass.id)) AS points,
            COALESCE(pm.total_minutes, 0) AS penalty_minutes
        FROM madoc.players p
                 LEFT JOIN madoc.attendance a
                           ON p.id = a.player_id AND a.attended = true
                 LEFT JOIN madoc.games ga_att
                           ON a.game_id = ga_att.id AND ga_att.year = :year AND ga_att.season_type = :season_type
                 LEFT JOIN madoc.goals go
                           ON p.id = go.player_id
                 LEFT JOIN madoc.games ga_go
                           ON go.game_id = ga_go.id AND ga_go.year = :year AND ga_go.season_type = :season_type
                 LEFT JOIN madoc.assists ass
                           ON p.id = ass.player_id
                 LEFT JOIN madoc.goals go_ass
                           ON (go_ass.primary_assist_id = ass.id OR go_ass.secondary_assist_id = ass.id)
                 LEFT JOIN madoc.games ga_ass
                           ON go_ass.game_id = ga_ass.id AND ga_ass.year = :year AND ga_ass.season_type = :season_type
                 LEFT JOIN (
            SELECT pe.player_id, SUM(pe.minutes) AS total_minutes
            FROM madoc.penalties pe
                     JOIN madoc.games ga_pe ON pe.game_id = ga_pe.id
            WHERE ga_pe.year = :year AND ga_pe.season_type = :season_type
            GROUP BY pe.player_id
        ) pm ON p.id = pm.player_id
                 LEFT JOIN madoc.roster_assignments ra
                           ON ra.player_id = p.id AND ra.season_year = :year
                 LEFT JOIN madoc.teams t
                           ON ra.team_id = t.id AND t.year = :year
        GROUP BY p.id, ra.jersey_number, t.team_name, player_name, pm.total_minutes;
        """;

        public static final String GET_PLAYER_SEASON_TYPE_STATS_BY_YEAR_AND_TEAM = """
        SELECT
            pl.id as player_id,
            ra.jersey_number,
            CONCAT(pl.first_name, ' ', pl.last_name) as player_name,
            t.team_name,
            COUNT(at.player_id) as games_played,
            COUNT(g.player_id) as goals,
            COUNT(a.player_id) as assists,
            (COUNT(g.player_id) + COUNT(a.player_id)) as points,
            COALESCE(SUM(p.minutes), 0) as penalty_minutes
        FROM madoc.players pl
                 FULL OUTER JOIN madoc.attendance at on pl.id = at.player_id
                 FULL OUTER JOIN madoc.goals g on pl.id = g.player_id
                 FULL OUTER JOIN madoc.assists a on pl.id = a.player_id
                 FULL OUTER JOIN madoc.penalties p on pl.id = p.player_id
                 INNER JOIN madoc.roster_assignments ra on pl.id = ra.player_id and ra.season_year = :year
                 INNER JOIN madoc.teams t on ra.team_id = t.id and t.team_name = :team_name
        GROUP BY pl.id, g.player_id, a.player_id, p.player_id, ra.player_id, ra.team_id, t.team_name, at.player_id, ra.jersey_number
        ORDER BY points desc;
        """;
    }
}
