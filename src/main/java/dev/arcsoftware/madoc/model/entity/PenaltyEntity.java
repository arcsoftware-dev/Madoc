package dev.arcsoftware.madoc.model.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class PenaltyEntity {
    private int id;
    private GameEntity game;
    private PlayerEntity player;
    private String infraction;
    private int minutes;
    private int period;
    private LocalTime time;
    private LocalDateTime uploadedAt;
}
