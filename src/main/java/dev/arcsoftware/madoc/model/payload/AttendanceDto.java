package dev.arcsoftware.madoc.model.payload;

import lombok.Data;

import java.util.List;

@Data
public class AttendanceDto {
    private Integer gameId;
    private Integer teamId;
    private List<AttendanceUploadResult.AttendanceDetail> attendanceDetails;
}
