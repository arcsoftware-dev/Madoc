package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.Position;
import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import dev.arcsoftware.madoc.model.entity.RosterFileData;
import dev.arcsoftware.madoc.model.timesheet.RosterDto;
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
public class RosterRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public RosterRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private static List<RosterDto> static2024Rosters;

    @PostConstruct
    public void loadData(){
        static2024Rosters = new ArrayList<>();
        ClassPathResource rosterResource2024 = new ClassPathResource("data/rosters/2024.csv");
        try(BufferedReader rosters = new BufferedReader(new BufferedReader(new InputStreamReader(rosterResource2024.getInputStream())))) {
            rosters.lines()
                    .skip(1) // Skip header line
                    .forEach(line -> {
                        //#,Player,Team,Draft Rank,isRookie
                        String[] split = line.split(",");

                        DraftRank draftRank = DraftRank.valueOf(split[3]);
                        Position position = Position.fromCode(draftRank.name().substring(0, 1));
                        boolean rookieStatus = split[4].equalsIgnoreCase("true");
                        RosterDto rosterDto = RosterDto.builder()
                                .jerseyNumber(Integer.parseInt(split[0]))
                                .teamName(split[2])
                                .fullName(split[1])
                                .firstName(split[1].split(" ")[0])
                                .lastName(split[1].split(" ")[1])
                                .position(position)
                                .draftRank(draftRank)
                                .isRookie(rookieStatus)
                                .build();
                        static2024Rosters.add(rosterDto);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RosterDto> getRostersByYear(int year) {
        if(year == 2024) {
            return static2024Rosters;
        }
        return jdbcClient
                .sql(RostersSql.GET_ROSTERS_BY_YEAR)
                .param("year", year)
                .query((rs, num) -> RosterDto.builder()
                        .jerseyNumber(rs.getInt("jersey_number"))
                        .firstName(rs.getString("first_name"))
                        .lastName(rs.getString("last_name"))
                        .fullName(rs.getString("full_name"))
                        .position(Position.valueOf(rs.getString("position")))
                        .draftRank(DraftRank.valueOf(rs.getString("draft_position")))
                        .isRookie(rs.getBoolean("is_rookie"))
                        .teamName(rs.getString("team_name"))
                        .build()
                )
                .list();
    }

    public void insertRosterAssignment(RosterAssignment rosterAssignment) {
        int id = jdbcClient
                .sql(RostersSql.INSERT_ROSTER_ASSIGNMENT)
                .param("teamId", rosterAssignment.getTeamId())
                .param("playerId", rosterAssignment.getPlayerId())
                .param("seasonYear", rosterAssignment.getSeasonYear())
                .param("draftPosition", rosterAssignment.getDraftPosition().name())
                .param("position", rosterAssignment.getPosition().name())
                .param("jerseyNumber", rosterAssignment.getJerseyNumber())
                .param("isRookie", rosterAssignment.isRookie())
                .query(Integer.class)
                .single();
        rosterAssignment.setId(id);
    }

    public void uploadRosterFile(RosterFileData rosterFileData) {
        int id = jdbcClient
                .sql(RostersSql.INSERT_ROSTER_FILE)
                .param("year", rosterFileData.getYear())
                .param("fileName", rosterFileData.getFileName())
                .param("fileContent", rosterFileData.getFileContent())
                .query(Integer.class)
                .single();
        rosterFileData.setId(id);
    }

    public static class RostersSql {
        public static final String GET_ROSTERS_BY_YEAR = """
        SELECT r.jersey_number , p.first_name, p.last_name, CONCAT(p.first_name, ' ', p.last_name) as full_name, r.position, r.draft_position, r.is_rookie, t.team_name
            FROM "madoc".roster_assignments as r
            JOIN "madoc".players as p ON r.player_id = p.id
            JOIN "madoc".teams as t ON r.team_id = t.id
        where t.year = :year;
        """;

        public static final String INSERT_ROSTER_ASSIGNMENT = """
        INSERT INTO madoc.roster_assignments (team_id, player_id, season_year, draft_position, position, jersey_number, is_rookie)
        VALUES (:teamId, :playerId, :seasonYear, :draftPosition, :position, :jerseyNumber, :isRookie)
        RETURNING id;
        """;

        public static final String INSERT_ROSTER_FILE = """
        INSERT INTO madoc.roster_uploads (year, file_name, file_content)
        VALUES (:year, :fileName, :fileContent)
        RETURNING id;
        """;
    }
}
