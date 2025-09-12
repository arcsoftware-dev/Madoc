package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.model.entity.AttendanceEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AttendanceUploadResult {
    private int attendanceFileId;
    private String attendanceFileName;
    private int gameId;
    private AttendanceSummary homeTeam;
    private AttendanceSummary awayTeam;

    public static class AttendanceSummary{
        private String teamName;
        private List<AttendanceEntity> attendances;
    }

    public static class AttendanceDetail{
        private String playerName;
        private int jerseyNumber;
        private boolean attended;
    }
}
