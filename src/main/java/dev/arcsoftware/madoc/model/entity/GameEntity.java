package dev.arcsoftware.madoc.model.entity;

import dev.arcsoftware.madoc.enums.Arena;

import java.time.LocalDateTime;

public class GameEntity {
    private Integer gameId;
    private Arena arena;
    private LocalDateTime startTime;
    private Integer homeTeamId;
    private Integer awayTeamId;
    private Integer timeSheetId;
}
