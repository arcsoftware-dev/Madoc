package dev.arcsoftware.madoc.model.payload;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TeamStatsDto {
    private String teamName;

    private int gamesPlayed;
    private int wins;
    private int losses;
    private int ties;
    private int points;
    private int goalsFor;
    private int goalsAgainst;
    private int shutouts;
    private int penaltyMinutes;
}
