package dev.arcsoftware.madoc.model.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class StatsDto {
    //Shared fields
    private String playerName;
    private String teamName;
    private int gamesPlayed;
    private int goals;
    private int assists;
    private int points;
    private int penaltyMinutes;

    //Goalie specific fields
    private int wins;
    private int losses;
    private int ties;
    private int shutouts;
    private int goalsAgainst;

    @Setter
    int rank; // For convenience in displaying rankings
}