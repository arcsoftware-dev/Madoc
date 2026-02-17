package dev.arcsoftware.madoc.model.payload;

import lombok.Data;

@Data
public class MatchupSummary {
    private String team1;
    private String team2;
    private int team1Wins;
    private int team2Wins;
    private int ties;
    private int team1Pims;
    private int team2Pims;
    private int team1Goals;
    private int team2Goals;
    private String team1Record;
    private String team2Record;

    public MatchupSummary(String team1, String team2) {
        assert team1 != null && team2 != null : "Team names cannot be null";

        this.team1 = team1;
        this.team2 = team2;
    }

    public void addGoals(String teamName, int goals) {
        if (team1.equals(teamName)) {
            team1Goals += goals;
        } else if (team2.equals(teamName)) {
            team2Goals += goals;
        }
    }

    public void addPims(String teamName, int pims) {
        if (team1.equals(teamName)) {
            team1Pims += pims;
        } else if (team2.equals(teamName)) {
            team2Pims += pims;
        }
    }

    public void addWin(String teamName) {
        if (team1.equals(teamName)) {
            team1Wins++;
        } else if (team2.equals(teamName)) {
            team2Wins++;
        }
    }

    public void addTie() {
        ties++;
    }

    public String getTeam1Record() {
        return String.format("%d-%d-%d", team1Wins, team2Wins, ties);
    }

    public String getTeam2Record() {
        return String.format("%d-%d-%d", team2Wins, team1Wins, ties);
    }

}
