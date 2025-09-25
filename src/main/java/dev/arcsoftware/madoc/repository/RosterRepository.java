package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.Position;
import dev.arcsoftware.madoc.model.entity.PlayerEntity;
import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import dev.arcsoftware.madoc.model.entity.UploadFileData;
import dev.arcsoftware.madoc.model.payload.RosterAssignmentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class RosterRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public RosterRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void insertRosterAssignment(RosterAssignment rosterAssignment) {
        int id = jdbcClient
                .sql(RostersSql.INSERT_ROSTER_ASSIGNMENT)
                .params(rosterAssignment.toParameterMap())
                .query(Integer.class)
                .single();
        rosterAssignment.setId(id);
    }

    public List<PlayerEntity> getAllPlayers(){
        return jdbcClient
                .sql(RostersSql.GET_ALL_PLAYERS)
                .query((rs, num) -> {
                    PlayerEntity p = new PlayerEntity();
                    p.setId(rs.getInt("id"));
                    p.setFirstName(rs.getString("first_name"));
                    p.setLastName(rs.getString("last_name"));
                    p.setEmail(rs.getString("email"));
                    p.setPhoneNumber(rs.getString("phone_number"));
                    p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return p;
                })
                .list();
    }

    public void insertPlayer(PlayerEntity playerEntity) {
        int id = jdbcClient
                .sql(RostersSql.INSERT_PLAYER)
                .params(playerEntity.toParameterMap())
                .query(Integer.class)
                .single();
        playerEntity.setId(id);
    }

    public void uploadRosterFile(UploadFileData uploadFileData) {
        int id = jdbcClient
                .sql(RostersSql.INSERT_ROSTER_FILE)
                .params(uploadFileData.toParameterMap())
                .query(Integer.class)
                .single();
        uploadFileData.setId(id);
    }

    public List<RosterAssignmentDto> getAssignmentsByYearAndTeam(int year, String teamName) {
        return jdbcClient
                .sql(RostersSql.GET_ASSIGNMENTS_BY_TEAM_AND_YEAR)
                .param("season_year", year)
                .param("team_name", teamName)
                .query((rs, num) -> {
                    RosterAssignmentDto rosterAssignment = new RosterAssignmentDto();
                    rosterAssignment.setId(rs.getInt("id"));
                    rosterAssignment.setPlayerId(rs.getInt("player_id"));
                    rosterAssignment.setTeamId(rs.getInt("team_id"));
                    rosterAssignment.setSeasonYear(rs.getInt("season_year"));
                    rosterAssignment.setDraftPosition(DraftRank.valueOf(rs.getString("draft_position")));
                    rosterAssignment.setPosition(Position.valueOf(rs.getString("position")));
                    rosterAssignment.setJerseyNumber(rs.getInt("jersey_number"));
                    rosterAssignment.setRookie(rs.getBoolean("is_rookie"));
                    rosterAssignment.setFullName(rs.getString("full_name"));
                    rosterAssignment.setTeamName(rs.getString("team_name"));
                    rosterAssignment.setFirstName(rs.getString("first_name"));
                    rosterAssignment.setLastName(rs.getString("last_name"));
                    return rosterAssignment;
                })
                .list();
    }

    public void updateAssignment(RosterAssignment rosterAssignment) {
        jdbcClient
                .sql(RostersSql.UPDATE_ROSTER_ASSIGNMENT)
                .params(rosterAssignment.toParameterMap())
                .update();
    }

    public List<RosterAssignmentDto> getAssignmentsByYear(Integer year) {
        return jdbcClient
                .sql(RostersSql.GET_ASSIGNMENTS_BY_YEAR)
                .param("season_year", year)
                .query((rs, num) -> {
                    RosterAssignmentDto rosterAssignment = new RosterAssignmentDto();
                    rosterAssignment.setId(rs.getInt("id"));
                    rosterAssignment.setPlayerId(rs.getInt("player_id"));
                    rosterAssignment.setTeamId(rs.getInt("team_id"));
                    rosterAssignment.setSeasonYear(rs.getInt("season_year"));
                    rosterAssignment.setDraftPosition(DraftRank.valueOf(rs.getString("draft_position")));
                    rosterAssignment.setPosition(Position.valueOf(rs.getString("position")));
                    rosterAssignment.setJerseyNumber(rs.getInt("jersey_number"));
                    rosterAssignment.setRookie(rs.getBoolean("is_rookie"));
                    rosterAssignment.setFullName(rs.getString("full_name"));
                    rosterAssignment.setTeamName(rs.getString("team_name"));
                    rosterAssignment.setFirstName(rs.getString("first_name"));
                    rosterAssignment.setLastName(rs.getString("last_name"));
                    return rosterAssignment;
                })
                .list();
    }

    public static class RostersSql {
        public static final String GET_ASSIGNMENTS_BY_TEAM_AND_YEAR = """
        SELECT ra.id, ra.team_id, ra.player_id, ra.season_year, ra.draft_position, ra.position, ra.jersey_number, ra.is_rookie, CONCAT(p.first_name, ' ', p.last_name) as "full_name", p.first_name, p.last_name, t.team_name
            FROM madoc.roster_assignments ra
            JOIN madoc.players p ON ra.player_id = p.id
            JOIN madoc.teams t ON ra.team_id = t.id
        WHERE ra.season_year = :season_year
        AND t.team_name = :team_name;
        """;

        public static final String GET_ASSIGNMENTS_BY_YEAR = """
        SELECT ra.id, ra.team_id, ra.player_id, ra.season_year, ra.draft_position, ra.position, ra.jersey_number, ra.is_rookie, CONCAT(p.first_name, ' ', p.last_name) as "full_name", p.first_name, p.last_name, t.team_name
            FROM madoc.roster_assignments ra
            JOIN madoc.players p ON ra.player_id = p.id
            JOIN madoc.teams t ON ra.team_id = t.id
        WHERE ra.season_year = :season_year
        """;

        public static final String INSERT_ROSTER_ASSIGNMENT = """
        INSERT INTO madoc.roster_assignments (team_id, player_id, season_year, draft_position, position, jersey_number, is_rookie)
        VALUES (:team_id, :player_id, :season_year, :draft_position, :position, :jersey_number, :is_rookie)
        RETURNING id;
        """;

        public static final String INSERT_ROSTER_FILE = """
        INSERT INTO madoc.roster_uploads (year, file_name, file_content)
        VALUES (:year, :file_name, :file_content)
        RETURNING id;
        """;

        public static final String INSERT_PLAYER = """
        INSERT INTO madoc.players (first_name, last_name, email, phone_number)
        VALUES (:first_name, :last_name, :email, :phone_number)
        RETURNING id;
        """;

        public static final String GET_ALL_PLAYERS = """
        SELECT id, first_name, last_name, email, phone_number, created_at
        FROM madoc.players
        ORDER BY id ASC;
        """;
        public static final String UPDATE_ROSTER_ASSIGNMENT = """
        UPDATE madoc.roster_assignments SET
            team_id = :team_id,
            player_id = :player_id,
            season_year = :season_year,
            draft_position = :draft_position,
            position = :position,
            jersey_number = :jersey_number,
            is_rookie = :is_rookie
        WHERE id = :id
        """;
    }
}
