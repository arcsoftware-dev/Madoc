package dev.arcsoftware.madoc.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ScheduleUploadResult {
    private List<ScheduleItemDto> scheduleItems;
    private int scheduleFileId;
    private int year;
    private String scheduleFileName;
}
