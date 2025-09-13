package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.enums.Arena;
import dev.arcsoftware.madoc.enums.SeasonType;
import dev.arcsoftware.madoc.model.entity.GoalEntity;
import dev.arcsoftware.madoc.model.entity.PenaltyEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class GamesheetSummary {
    private int gamesheetFileId;
    private String gamesheetFileName;

    private LocalDateTime gameTime;
    private Arena venue;
    private int seasonYear;
    private SeasonType seasonType;

    private String homeTeam;
    private List<GoalEntity> homeGoals;
    private List<PenaltyEntity> homePenalties;
    private int homeScore;
    private int homePenaltyMinutes;

    private String awayTeam;
    private List<GoalEntity> awayGoals;
    private List<PenaltyEntity> awayPenalties;
    private int awayScore;
    private int awayPenaltyMinutes;

    private String refereeOne;
    private String refereeTwo;
    private String refereeThree;
    private List<String> refereeNotes;

    public void addHomeGoal(GoalEntity goal) {
        if(homeGoals == null) homeGoals = new ArrayList<>();
        homeGoals.add(goal);
    }

    public void addAwayGoal(GoalEntity goal) {
        if(awayGoals == null) awayGoals = new ArrayList<>();
        awayGoals.add(goal);
    }

    public void addHomePenalty(PenaltyEntity penalty) {
        if(homePenalties == null) homePenalties = new ArrayList<>();
        homePenalties.add(penalty);
    }

    public void addAwayPenalty(PenaltyEntity penalty) {
        if(awayPenalties == null) awayPenalties = new ArrayList<>();
        awayPenalties.add(penalty);
    }

    public void addRefNote(String note){
        if(refereeNotes == null) refereeNotes = new ArrayList<>();
        refereeNotes.add(note);
    }
}
