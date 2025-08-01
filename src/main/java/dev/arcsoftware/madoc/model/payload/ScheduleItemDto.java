package dev.arcsoftware.madoc.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ScheduleItemDto {
    private LocalDateTime startTime;
    private String homeTeam;
    private String awayTeam;
}
