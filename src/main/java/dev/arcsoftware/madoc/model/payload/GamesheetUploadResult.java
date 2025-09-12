package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.enums.Arena;
import dev.arcsoftware.madoc.enums.SeasonType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class GamesheetUploadResult {
    private int gamesheetFileId;
    private String gamesheetFileName;

    private int seasonYear;
    private SeasonType seasonType;
    private Arena venue;
    private LocalDateTime gameTime;

    private String homeTeam;
    private int homeScore;
    private int homePenaltyMinutes;
    private String awayTeam;
    private int awayScore;
    private int awayPenaltyMinutes;

    private String refereeNameOne;
    private String refereeNameTwo;
    private String refereeNameThree;
    private List<String> refereeNotes;
}
