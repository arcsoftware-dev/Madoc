package dev.arcsoftware.madoc.model;

import lombok.Data;

@Data
public class TeamStats {
    private Team team;

    private int wins;
    private int losses;
    private int ties;
    private int points;
    private int goalsFor;
    private int goalsAgainst;
    private int shutouts;
    private int penaltyMinutes;
}
