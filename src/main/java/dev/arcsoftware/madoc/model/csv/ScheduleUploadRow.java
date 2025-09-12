package dev.arcsoftware.madoc.model.csv;

import dev.arcsoftware.madoc.enums.Arena;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleUploadRow {
    private LocalDateTime gameTime;
    private String homeTeam;
    private String awayTeam;
    private Arena arena = Arena.CENTURY;

    public ScheduleUploadRow(LocalDateTime gameTime, String homeTeam, String awayTeam) {
        this.gameTime = gameTime;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }
}
