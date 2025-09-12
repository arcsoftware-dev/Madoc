package dev.arcsoftware.madoc.model.entity;

import dev.arcsoftware.madoc.enums.Arena;
import dev.arcsoftware.madoc.enums.SeasonType;

import java.time.LocalDateTime;
import java.util.List;

public class GameEntity {
    private Integer gameId;
    private TeamEntity homeTeam;
    private TeamEntity awayTeam;
    private int year;
    private SeasonType seasonType;
    private Arena venue;
    private LocalDateTime gameTime;

    private String refereeNameOne;
    private String refereeNameTwo;
    private String refereeNameThree;
    private List<String> refereeNotes;

    private boolean isFinalized;
    private LocalDateTime finalizedAt;
}
