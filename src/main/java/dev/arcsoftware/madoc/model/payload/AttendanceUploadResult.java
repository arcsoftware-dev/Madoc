package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.enums.SeasonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceUploadResult {
    private int attendanceFileId;
    private String attendanceFileName;
    private int seasonYear;
    private SeasonType seasonType;
    private LocalDateTime gameTime;
    private String teamName;
    private List<AttendanceDetail> attendanceDetails;

    public void addAttendanceDetail(AttendanceDetail attendanceDetail) {
        if(attendanceDetails == null) attendanceDetails = new ArrayList<>();
        attendanceDetails.add(attendanceDetail);
    }

    @Data
    public static class AttendanceDetail{
        private String playerName;
        private int jerseyNumber;
        private boolean attended;
    }
}
