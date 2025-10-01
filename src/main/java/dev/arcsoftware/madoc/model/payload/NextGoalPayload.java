package dev.arcsoftware.madoc.model.payload;

import lombok.Data;

@Data
public class NextGoalPayload {
    private String team;
    private GoalPayload goal;
}
