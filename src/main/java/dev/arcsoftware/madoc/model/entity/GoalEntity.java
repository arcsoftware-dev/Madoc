package dev.arcsoftware.madoc.model.entity;

import dev.arcsoftware.madoc.enums.GoalType;

import java.time.LocalDateTime;
import java.time.LocalTime;


public class GoalEntity {
    private Integer id;
    private GameEntity game;
    private GoalType goalType;
    private PlayerEntity player;
    private PlayerEntity primaryAssistPlayer;
    private PlayerEntity secondaryAssistPlayer;
    private int period;
    private LocalTime time;
    private LocalDateTime uploadedAt;
}