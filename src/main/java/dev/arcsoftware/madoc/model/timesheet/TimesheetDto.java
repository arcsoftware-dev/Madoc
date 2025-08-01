package dev.arcsoftware.madoc.model.timesheet;

import java.util.ArrayList;
import java.util.List;

public class TimesheetDto {
    private final String homeTeamName;
    private final String awayTeamName;

    private final List<String> officials;

    private final List<RosterDto> homeRoster;
    private final List<RosterDto> awayRoster;

    private List<GoalDto> homeGoals;
    private List<GoalDto> awayGoals;
    private List<PenaltyDto> homePenalties;
    private List<PenaltyDto> awayPenalties;

    public TimesheetDto(
            SignInSheet homeSignInSheet,
            SignInSheet awaySignInSheet,
            List<String> officials
    ) {
        this.homeTeamName = homeSignInSheet.getTeamName();
        this.awayTeamName = awaySignInSheet.getTeamName();
        this.officials = officials;

        this.homeRoster = initRoster(homeSignInSheet);
        this.awayRoster = initRoster(awaySignInSheet);
    }

    private List<RosterDto> initRoster(SignInSheet signInSheet) {
        List<RosterDto> roster = new ArrayList<>();
        roster.add(signInSheet.getGoalie());
        roster.addAll(signInSheet.getPlayers());

        for(RosterDto player : roster) {
            player.setFullName(
                player.getFirstName() + " " + player.getLastName()
            );
            player.setTeamName(signInSheet.getTeamName());
        }

        return roster;
    }
}
