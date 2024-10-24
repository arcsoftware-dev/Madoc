package dev.arcsoftware.madoc.model;

import lombok.Data;

@Data
public class PlayerStats {
    private Integer playerStatsId;

    private Player player;

    //Shared Stats
    private int gamesPlayed;
    private int goals;
    private int assists;
    private int points;
    private int penaltyMinutes;

    //Goalie Stats
    private int wins;
    private int losses;
    private int ties;
    private int shutouts;
    private int goalsAgainst;
    private double goalsAgainstAverage;
}
