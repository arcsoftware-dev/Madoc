package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.enums.Arena;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ScheduleItemDto {
    private LocalDateTime startTime;
    private Integer gameId;
    private String homeTeam;
    private String awayTeam;
    private String homeScore;
    private String awayScore;
    private Arena arena;
}
