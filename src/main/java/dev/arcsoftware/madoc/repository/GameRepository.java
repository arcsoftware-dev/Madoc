package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.Arena;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.entity.GameEntity;
import dev.arcsoftware.madoc.model.entity.GameUploadData;
import dev.arcsoftware.madoc.model.entity.TeamEntity;
import dev.arcsoftware.madoc.model.entity.UploadFileData;
import dev.arcsoftware.madoc.model.payload.ScheduleItemDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Array;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Repository
public class GameRepository {
    private final JdbcClient jdbcClient;

    public GameRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ScheduleItemDto> getUpcomingMatches(SeasonType seasonType, int year) {
        List<ScheduleItemDto> schedule = getSchedule(seasonType, year);

        LocalDateTime now = LocalDate.now().atStartOfDay();
        return schedule.stream()
                .filter(item -> item.getStartTime().isAfter(now))
                .limit(3) // Limit to 3 upcoming matches
                .toList();
    }

    public List<ScheduleItemDto> getSchedule(SeasonType seasonType, int year) {
        return jdbcClient
                .sql(GameSql.GET_SCHEDULE_BY_TYPE_AND_YEAR)
                .param("year", year)
                .param("season_type", seasonType.name())
                .query((rs, num) -> new ScheduleItemDto(
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getString("home_team"),
                        rs.getString("away_team")
                ))
                .list();
    }

    private List<ScheduleItemDto> loadScheduleFromCsv(SeasonType seasonType, int year) {
        List<ScheduleItemDto> schedule = new ArrayList<>();

        String scheduleFileName = String.format("data/schedule/%d/%s.csv", year, seasonType.name());
        ClassPathResource scheduleCsv = new ClassPathResource(scheduleFileName);

        final String[] HEADERS = {"Datetime","Home Team","Away Team"};
        try(Reader reader = new InputStreamReader(scheduleCsv.getInputStream())) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .get();

            Iterable<CSVRecord> records = csvFormat.parse(reader);
            for(CSVRecord record : records) {
                ScheduleItemDto scheduleItemDto = new ScheduleItemDto(
                        LocalDateTime.parse(record.get("Datetime")),
                        record.get("Home Team"),
                        record.get("Away Team")
                );
                schedule.add(scheduleItemDto);
            }
        } catch (IOException e) {
            log.error("Error loading schedule from CSV for {} {}", seasonType, year, e);
            return new ArrayList<>(); // Return empty list on error
        }
        return schedule;
    }

    public void uploadScheduleFile(UploadFileData uploadFileData) {
        int id = jdbcClient
                .sql(GameSql.INSERT_SCHEDULE_FILE)
                .params(uploadFileData.toParameterMap())
                .query(Integer.class)
                .single();
        uploadFileData.setId(id);
    }

    public void insertGame(GameEntity game) {
        int id = jdbcClient
                .sql(GameSql.INSERT_NEW_GAME)
                .params(game.toParameterMap())
                .query(Integer.class)
                .single();
        game.setId(id);
    }

    public void updateGame(GameEntity game) {
        jdbcClient
                .sql(GameSql.UPDATE_GAME)
                .params(game.toParameterMap())
                .update();
    }

    public GameEntity findByDateYearAndSeasonType(LocalDateTime gameTime, int seasonYear, SeasonType seasonType) {
        return jdbcClient
                .sql(GameSql.GET_GAME_BY_TIME_YEAR_SEASONTYPE)
                .param("game_time", gameTime)
                .param("year", seasonYear)
                .param("season_type", seasonType.name())
                .query((rs, num) -> {
                    TeamEntity homeTeam = new TeamEntity();
                    homeTeam.setId(rs.getInt("home_team"));
                    homeTeam.setTeamName(rs.getString("home_team_name"));
                    homeTeam.setYear(seasonYear);

                    TeamEntity awayTeam = new TeamEntity();
                    awayTeam.setId(rs.getInt("away_team"));
                    awayTeam.setTeamName(rs.getString("away_team_name"));
                    awayTeam.setYear(seasonYear);

                    GameEntity game = new GameEntity();
                    game.setId(rs.getInt("id"));
                    game.setHomeTeam(homeTeam);
                    game.setAwayTeam(awayTeam);
                    game.setYear(seasonYear);
                    game.setSeasonType(seasonType);
                    game.setVenue(Arena.valueOf(rs.getString("venue")));
                    game.setGameTime(gameTime);
                    game.setRefereeNameOne(rs.getString("referee_name_one"));
                    game.setRefereeNameTwo(rs.getString("referee_name_two"));
                    game.setRefereeNameThree(rs.getString("referee_name_three"));

                    Array rawArray = rs.getArray("referee_notes");
                    if(rawArray != null){
                        String[] refNotesArray = (String[]) rawArray.getArray();
                        game.setRefereeNotes(Arrays.stream(refNotesArray).toList());
                    }

                    game.setFinalized(rs.getBoolean("is_finalized"));
                    Timestamp finalizedTimestamp = rs.getTimestamp("finalized_at");
                    if(finalizedTimestamp != null){
                        game.setFinalizedAt(finalizedTimestamp.toLocalDateTime());
                    }

                    return game;
                })
                .single();
    }

    public void insertGamesheetUpload(GameUploadData uploadFileData) {
        int id = jdbcClient
                .sql(GameSql.INSERT_GAMESHEET_UPLOAD)
                .params(uploadFileData.toParameterMap())
                .query(Integer.class)
                .single();
        uploadFileData.setId(id);
    }

    public static class GameSql {
        public static final String GET_SCHEDULE_BY_TYPE_AND_YEAR = """
        SELECT g.game_time as start_time, ht.team_name as home_team, at.team_name as away_team
            FROM "madoc".games g
            JOIN "madoc".teams as ht ON g.home_team = ht.id AND g.year = ht.year
            JOIN "madoc".teams as at ON g.away_team = at.id AND g.year = at.year
        WHERE g.year = :year
        AND g.season_type = :season_type
        ORDER BY g.game_time ASC;
        """;

        public static final String INSERT_NEW_GAME = """
        INSERT INTO madoc.games (home_team, away_team, year, season_type, venue, game_time)
        VALUES (:home_team, :away_team, :year, :season_type, :venue, :game_time)
        RETURNING id;
        """;

        public static final String INSERT_SCHEDULE_FILE = """
        INSERT INTO madoc.schedule_uploads (year, file_name, file_content)
        VALUES (:year, :file_name, :file_content)
        RETURNING id;
        """;

        public static final String UPDATE_GAME = """
        UPDATE madoc.games SET
            home_team = :home_team,
            away_team = :away_team,
            year = :year,
            season_type = :season_type,
            venue = :venue,
            game_time = :game_time,
            referee_name_one = :referee_name_one,
            referee_name_two = :referee_name_two,
            referee_name_three = :referee_name_three,
            referee_notes = ARRAY[:referee_notes]::text[],
            is_finalized = :is_finalized,
            finalized_at = :finalized_at
        WHERE id = :id;
        """;

        public static final String INSERT_GAMESHEET_UPLOAD = """
        INSERT INTO madoc.gamesheet_uploads (game_id, file_name, file_content)
        VALUES (:game_id, :file_name, :file_content)
        RETURNING id;
        """;


        public static final String GET_GAME_BY_TIME_YEAR_SEASONTYPE = """
        SELECT g.id, g.home_team, g.away_team, g.venue, g.referee_name_one, g.referee_name_two, g.referee_name_three, g.referee_notes, g.is_finalized, g.finalized_at, ht.team_name as home_team_name, at.team_name as away_team_name
            FROM "madoc".games g
            JOIN "madoc".teams as ht ON g.home_team = ht.id AND g.year = ht.year
            JOIN "madoc".teams as at ON g.away_team = at.id AND g.year = at.year
        WHERE g.year = :year
        AND g.season_type = :season_type
        AND g.game_time = :game_time;
        """;
    }
}
