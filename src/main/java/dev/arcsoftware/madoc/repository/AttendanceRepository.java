package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.model.entity.AttendanceEntity;
import dev.arcsoftware.madoc.model.entity.GameUploadData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class AttendanceRepository {
    private final JdbcClient jdbcClient;

    @Autowired
    public AttendanceRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void insertAttendanceEntity(AttendanceEntity attendanceEntity) {
        int id = this.jdbcClient
                .sql(AttendanceSql.INSERT)
                .params(attendanceEntity.toParameterMap())
                .query(Integer.class)
                .single();
        attendanceEntity.setId(id);
    }

    public void clearByGameId(int gameId) {
        int results = jdbcClient
                .sql(AttendanceSql.DELETE_BY_GAME_ID)
                .param("game_id", gameId)
                .update();
        log.info("{} attendance records deleted", results);
    }

    public static class AttendanceSql {
        public static final String INSERT = """
        INSERT INTO madoc.attendance (game_id, roster_assignment_id, jersey_number, attended)
        VALUES (:game_id, :roster_assignment_id, :jersey_number, :attended)
        RETURNING id;
        """;

        public static final String DELETE_BY_GAME_ID = """
        DELETE FROM madoc.attendance
        WHERE game_id = :game_id
        """;
    }
}
