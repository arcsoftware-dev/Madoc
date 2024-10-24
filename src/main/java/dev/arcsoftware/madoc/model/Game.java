package dev.arcsoftware.madoc.model;

import dev.arcsoftware.madoc.enums.Arena;

import java.time.LocalDateTime;

public class Game {
    private Integer gameId;
    private GameSheet gameSheet;
    private Arena arena;
    private LocalDateTime startTime;
    private Team homeTeam;
    private Team awayTeam;
    private boolean isComplete;
}
