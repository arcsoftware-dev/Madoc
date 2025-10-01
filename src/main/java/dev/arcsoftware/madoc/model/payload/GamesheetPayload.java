package dev.arcsoftware.madoc.model.payload;

import dev.arcsoftware.madoc.enums.Arena;
import dev.arcsoftware.madoc.enums.SeasonType;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class GamesheetPayload {
    private int gameId;
    private boolean finalized;

    private LocalDateTime gameTime;
    private Arena venue;
    private int seasonYear;
    private SeasonType seasonType;

    private String homeTeam;
    private int homeTeamId;
    private List<GoalPayload> homeGoals;
    private List<PenaltyPayload> homePenalties;
    private List<AttendancePayload> homeAttendanceByPlayerId;

    private String awayTeam;
    private int awayTeamId;
    private List<GoalPayload> awayGoals;
    private List<PenaltyPayload> awayPenalties;
    private List<AttendancePayload> awayAttendanceByPlayerId;

    private String refereeOne;
    private String refereeTwo;
    private String refereeThree;
    private List<String> refereeNotes;

    @Transient
    private NextGoalPayload nextGoal;
    @Transient
    private NextPenaltyPayload nextPenalty;

    public boolean getFinalized() {
        return finalized;
    }

    public void addHomeGoal(GoalPayload goal) {
        if(CollectionUtils.isEmpty(homeGoals)){
            homeGoals = new ArrayList<>();
        }
        homeGoals.add(goal);
    }

    public void addAwayGoal(GoalPayload goal) {
        if(CollectionUtils.isEmpty(awayGoals)){
            awayGoals = new ArrayList<>();
        }
        awayGoals.add(goal);
    }

    public void addHomePenalty(PenaltyPayload penalty) {
        if(CollectionUtils.isEmpty(homePenalties)){
            homePenalties = new ArrayList<>();
        }
        homePenalties.add(penalty);
    }

    public void addAwayPenalty(PenaltyPayload penalty) {
        if(CollectionUtils.isEmpty(awayPenalties)){
            awayPenalties = new ArrayList<>();
        }
        awayPenalties.add(penalty);
    }
}
