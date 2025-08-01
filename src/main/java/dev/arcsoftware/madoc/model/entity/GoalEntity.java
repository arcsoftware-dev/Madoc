package dev.arcsoftware.madoc.model.entity;

import dev.arcsoftware.madoc.enums.GoalType;

import java.sql.Timestamp;


public class GoalEntity {
    private Integer goalId;
    private Timestamp timestamp;
    private int period;

    private Integer scorerId;
    private Integer primaryAssistPlayerId;
    private Integer secondaryAssistPlayerId;
    private GoalType goalType;
}