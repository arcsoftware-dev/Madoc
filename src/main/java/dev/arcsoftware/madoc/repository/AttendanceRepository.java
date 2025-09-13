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

    public void insertAttendanceUpload(GameUploadData uploadFileData) {
        int id = jdbcClient
                .sql(AttendanceSql.INSERT_ATTENDANCE_UPLOAD)
                .params(uploadFileData.toParameterMap())
                .query(Integer.class)
                .single();
        uploadFileData.setId(id);
    }

    public static class AttendanceSql {
        public static final String INSERT = """
        INSERT INTO madoc.attendance (game_id, player_id, jersey_number, team_id, attended)
        VALUES (:game_id, :player_id, :jersey_number, :team_id, :attended)
        RETURNING id;
        """;

        public static final String INSERT_ATTENDANCE_UPLOAD = """
        INSERT INTO madoc.attendance_uploads (game_id, file_name, file_content)
        VALUES (:game_id, :file_name, :file_content)
        RETURNING id;
        """;
    }
}
