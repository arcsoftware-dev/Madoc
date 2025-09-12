package dev.arcsoftware.madoc.model.entity;

public class AttendanceEntity {
    private Integer id;
    private GameEntity game;
    private PlayerEntity player;
    private int jerseyNumber;
    private TeamEntity team;
    private boolean attended;
}
