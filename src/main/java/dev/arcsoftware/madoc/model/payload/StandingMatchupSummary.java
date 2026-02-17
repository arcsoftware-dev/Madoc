package dev.arcsoftware.madoc.model.payload;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
public class StandingMatchupSummary {
    @Getter
    private String team;
    private String record;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private int ties;
    private int goalsFor;
    private int goalsAgainst;
    private int penaltyMinutes;

    public String getRecord() {
        return String.format("%d-%d-%d", wins, losses, ties);
    }

    public double getGoalsForPerGame() {
        return gamesPlayed > 0 ? (double) goalsFor / gamesPlayed : 0.0;
    }

    public double getGoalsAgainstPerGame() {
        return gamesPlayed > 0 ? (double) goalsAgainst / gamesPlayed : 0.0;
    }

    public double getPenaltyMinutesPerGame() {
        return gamesPlayed > 0 ? (double) penaltyMinutes / gamesPlayed : 0.0;
    }
}
