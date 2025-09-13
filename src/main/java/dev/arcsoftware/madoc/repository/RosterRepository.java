package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.enums.DraftRank;
import dev.arcsoftware.madoc.enums.Position;
import dev.arcsoftware.madoc.model.entity.PlayerEntity;
import dev.arcsoftware.madoc.model.entity.RosterAssignment;
import dev.arcsoftware.madoc.model.entity.UploadFileData;
import dev.arcsoftware.madoc.model.payload.RosterDto;
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

    public List<RosterDto> getRostersByYear(int year) {
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
    }
}
